// Crear Activity para mostrar el mapa y manejar markers/rutas
package edu.unlp.reciclar.ui.maps

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import edu.unlp.reciclar.R
import dagger.hilt.android.AndroidEntryPoint
import edu.unlp.reciclar.data.remote.model.maps.RecyclingPoint
import edu.unlp.reciclar.data.remote.model.maps.RouteInfo
import edu.unlp.reciclar.ui.utils.LocationPermissionHelper
import java.util.Locale
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@AndroidEntryPoint
class RecyclingMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var routeInfoContainer: android.view.View
    private lateinit var tvRouteInfo: TextView
    private lateinit var permissionHelper: LocationPermissionHelper

    private val viewModel: RecyclingMapViewModel by viewModels()
    private val dataLoader by lazy { RecyclingMapDataLoader(this) }

    private val defaultCenter = GeoPoint(-34.9214, -57.9545) // La Plata

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_recycling_map)

        routeInfoContainer = findViewById(R.id.routeInfoContainer)
        tvRouteInfo = findViewById(R.id.tvRouteInfo)

        findViewById<Button?>(R.id.btnBack)?.setOnClickListener { finish() }

        setupMap()
        setupPermissionHelper()
        observeViewModel()

        // Cargar estaciones desde API
        viewModel.loadEstaciones()

        // Pedir permiso y habilitar ubicación si está
        permissionHelper.checkAndRequest()
    }

    private fun setupMap() {
        mapView = findViewById(R.id.map)
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(defaultCenter)
        }

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        myLocationOverlay.enableMyLocation()
        // NO usar enableFollowLocation() → evita que el mapa se recentre en la ubicación GPS del emulador
        mapView.overlays.add(myLocationOverlay)

        // Cerrar info windows al tocar el mapa
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                hideRouteInfo()
                // Limpiar rutas dibujadas
                mapView.overlays.removeAll { it is Polyline }
                mapView.overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
                viewModel.clearRouteInfo()
                mapView.invalidate()
                return false
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
        mapView.overlays.add(mapEventsOverlay)
    }

    private fun setupPermissionHelper() {
        permissionHelper = LocationPermissionHelper(
            caller = this,
            contextProvider = { this },
            onPermissionGranted = { enableUserLocation() },
            onPermissionDenied = { Toast.makeText(this, "Activá la ubicación para calcular rutas", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun enableUserLocation() {
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.runOnFirstFix {
            val loc = myLocationOverlay.myLocation ?: return@runOnFirstFix
            val gp = GeoPoint(loc.latitude, loc.longitude)
            runOnUiThread {
                // Solo guardar como origin para calcular rutas
                // NO mover el mapa — se queda centrado en los puntos de reciclaje
                viewModel.setOriginPoint(gp)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { // estaciones
                    viewModel.points.collect { list ->
                        // remover markers previos (excepto overlays de localización)
                        mapView.overlays.removeAll { it is Marker }
                        list.forEach { addMarkerForPoint(it) }
                        // Centrar en el primer punto de reciclaje o en La Plata
                        if (list.isNotEmpty()) {
                            mapView.controller.setCenter(list.first().toGeoPoint())
                        } else {
                            mapView.controller.setCenter(defaultCenter)
                        }
                        mapView.invalidate()
                    }
                }

                launch { // punto seleccionado
                    viewModel.selectedPoint.collect { point ->
                        point?.let { openMarkerInfo(it) }
                    }
                }

                launch { // ruta
                    viewModel.routeInfo.collect { info ->
                        if (info != null) showRouteInfo(info) else hideRouteInfo()
                    }
                }
            }
        }
    }

    private fun addMarkerForPoint(point: RecyclingPoint) {
        val marker = Marker(mapView).apply {
            position = point.toGeoPoint()
            title = point.name
            relatedObject = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        marker.setOnMarkerClickListener { m, _ ->
            val selected = m.relatedObject as? RecyclingPoint ?: return@setOnMarkerClickListener true

            // Cerrar info windows anteriores
            mapView.overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
            viewModel.selectPoint(selected)
            m.showInfoWindow()

            // Centrar mapa en el punto seleccionado
            mapView.controller.animateTo(selected.toGeoPoint())

            // Limpiar rutas previas
            mapView.overlays.removeAll { it is Polyline }
            mapView.invalidate()

            // Dibujar ruta si tenemos ubicación del usuario
            val origin = viewModel.originPoint.value
            if (origin != null) {
                dataLoader.drawWalkingRoute(mapView, origin, selected.toGeoPoint()) { routeInfo ->
                    viewModel.setRouteInfo(routeInfo)
                }
            } else {
                Toast.makeText(this@RecyclingMapActivity, "Esperando ubicación para calcular ruta...", Toast.LENGTH_SHORT).show()
            }
            true
        }

        mapView.overlays.add(marker)
    }

    private fun openMarkerInfo(point: RecyclingPoint) {
        mapView.overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
        mapView.overlays.filterIsInstance<Marker>()
            .find { (it.relatedObject as? RecyclingPoint)?.name == point.name }
            ?.showInfoWindow()
    }

    private fun showRouteInfo(routeInfo: RouteInfo) {
        val distance = if (routeInfo.distanceKm < 1) {
            "${(routeInfo.distanceKm * 1000).toInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", routeInfo.distanceKm)
        }

        val minutes = routeInfo.durationMinutes
        val time = if (minutes >= 60) {
            val h = minutes / 60
            val m = minutes % 60
            if (m > 0) "${h}h ${m}min" else "${h}h"
        } else {
            "${minutes} min"
        }

        tvRouteInfo.text = "🚶 A pie · $distance · ⏱ $time"

        if (!routeInfoContainer.isVisible) {
            routeInfoContainer.visibility = android.view.View.VISIBLE
        }
    }

    private fun hideRouteInfo() {
        if (routeInfoContainer.isVisible) routeInfoContainer.visibility = android.view.View.GONE
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
