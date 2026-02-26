package edu.unlp.reciclar.ui.maps

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.data.remote.model.maps.RecyclingPoint
import edu.unlp.reciclar.data.remote.model.maps.RouteInfo
import edu.unlp.reciclar.data.remote.model.maps.TransportMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

/** Pantalla de Mapa de Reciclaje. */
@Composable
fun RecyclingMapScreen(
    viewModel: RecyclingMapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val points by viewModel.points.collectAsStateWithLifecycle()
    val routeInfo by viewModel.routeInfo.collectAsStateWithLifecycle()
    val transportMode by viewModel.transportMode.collectAsStateWithLifecycle()
    val defaultCenter = remember { GeoPoint(-34.9214, -57.9545) }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    var locationPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            locationOverlay?.enableMyLocation()
            locationOverlay?.runOnFirstFix {
                val loc = locationOverlay?.myLocation ?: return@runOnFirstFix
                val gp = GeoPoint(loc.latitude, loc.longitude)
                viewModel.setOriginPoint(gp)
            }
        } else {
            Toast.makeText(context, "Activá la ubicación para calcular rutas", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            locationPermissionGranted = true
            locationOverlay?.enableMyLocation()
            locationOverlay?.runOnFirstFix {
                val loc = locationOverlay?.myLocation ?: return@runOnFirstFix
                viewModel.setOriginPoint(GeoPoint(loc.latitude, loc.longitude))
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun drawRoute(from: GeoPoint, to: GeoPoint, mode: TransportMode) {
        val mv = mapView ?: return

        mv.overlays.removeAll { it is Polyline }
        viewModel.clearRouteInfo()
        mv.invalidate()

        val mean = when (mode) {
            TransportMode.WALKING -> OSRMRoadManager.MEAN_BY_FOOT
            TransportMode.DRIVING -> OSRMRoadManager.MEAN_BY_CAR
        }
        val color = when (mode) {
            TransportMode.WALKING -> Color.parseColor("#1976D2")
            TransportMode.DRIVING -> Color.parseColor("#D32F2F")
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val roadManager = OSRMRoadManager(context, context.packageName)
                roadManager.setMean(mean)
                val road = roadManager.getRoad(arrayListOf(from, to))

                if (road.mStatus == Road.STATUS_OK) {
                    withContext(Dispatchers.Main) {
                        val overlay = RoadManager.buildRoadOverlay(road).apply {
                            outlinePaint.color = color
                            outlinePaint.strokeWidth = 12f
                        }
                        mv.overlays.add(overlay)
                        mv.invalidate()
                        viewModel.setRouteInfo(
                            RouteInfo(
                                distanceKm = road.mLength,
                                durationMinutes = (road.mDuration / 60).toInt(),
                                mode = mode
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(points, mapView) {
        val mv = mapView ?: return@LaunchedEffect

        mv.overlays.removeAll { it is Marker }

        points.forEach { point ->
            val marker = Marker(mv).apply {
                position = point.toGeoPoint()
                title = point.name
                relatedObject = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            marker.setOnMarkerClickListener { m, _ ->
                val selected = m.relatedObject as? RecyclingPoint
                    ?: return@setOnMarkerClickListener true

                mv.overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
                viewModel.selectPoint(selected)
                m.showInfoWindow()

                mv.controller.animateTo(selected.toGeoPoint())

                mv.overlays.removeAll { it is Polyline }
                viewModel.clearRouteInfo()
                mv.invalidate()

                val origin = viewModel.originPoint.value
                if (origin != null) {
                    drawRoute(origin, selected.toGeoPoint(), viewModel.transportMode.value)
                } else {
                    Toast.makeText(context, "Esperando ubicación para calcular ruta...", Toast.LENGTH_SHORT).show()
                }
                true
            }

            mv.overlays.add(marker)
        }

        if (points.isNotEmpty()) {
            mv.controller.setCenter(points.first().toGeoPoint())
        } else {
            mv.controller.setCenter(defaultCenter)
        }
        mv.invalidate()
    }

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(defaultCenter)

                        val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        val icon = createGreenLocationBitmap()
                        locOverlay.setPersonIcon(icon)
                        locOverlay.setDirectionIcon(icon)
                        @Suppress("DEPRECATION")
                        locOverlay.setPersonHotspot(icon.width / 2f, icon.height / 2f)
                        locOverlay.enableMyLocation()
                        overlays.add(locOverlay)
                        locationOverlay = locOverlay

                        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                overlays.removeAll { it is Polyline }
                                overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
                                viewModel.clearRouteInfo()
                                invalidate()
                                return false
                            }
                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        })
                        overlays.add(eventsOverlay)

                        if (locationPermissionGranted) {
                            locOverlay.enableMyLocation()
                            locOverlay.runOnFirstFix {
                                val loc = locOverlay.myLocation ?: return@runOnFirstFix
                                viewModel.setOriginPoint(GeoPoint(loc.latitude, loc.longitude))
                            }
                        }

                        mapView = this
                    }
                }
            )

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                        Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    TransportModeButton(
                        text = "🚶 A pie",
                        isSelected = transportMode == TransportMode.WALKING,
                        onClick = {
                            viewModel.setTransportMode(TransportMode.WALKING)
                            val origin = viewModel.originPoint.value
                            val selected = viewModel.selectedPoint.value
                            if (origin != null && selected != null) {
                                drawRoute(origin, selected.toGeoPoint(), TransportMode.WALKING)
                            }
                        }
                    )
                    TransportModeButton(
                        text = "🚗 Auto",
                        isSelected = transportMode == TransportMode.DRIVING,
                        onClick = {
                            viewModel.setTransportMode(TransportMode.DRIVING)
                            val origin = viewModel.originPoint.value
                            val selected = viewModel.selectedPoint.value
                            if (origin != null && selected != null) {
                                drawRoute(origin, selected.toGeoPoint(), TransportMode.DRIVING)
                            }
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 36.dp, end = 36.dp, bottom = 24.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = routeInfo != null,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    routeInfo?.let { info ->
                        RouteInfoPanel(info)
                    }
                }
            }
        }
}

@Composable
private fun TransportModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Panel inferior con información de la ruta calculada. */
@Composable
private fun RouteInfoPanel(routeInfo: RouteInfo) {
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
        "$minutes min"
    }

    val modeLabel = when (routeInfo.mode) {
        TransportMode.WALKING -> "🚶 A pie"
        TransportMode.DRIVING -> "🚗 En auto"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = "$modeLabel · $distance · ⏱ $time",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Crea un bitmap circular verde para el marcador de ubicación. */
private fun createGreenLocationBitmap(): Bitmap {
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Borde blanco
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint)

    // Círculo verde interior
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, fillPaint)

    // Punto blanco central
    val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, 6f, centerPaint)

    return bitmap
}

