package edu.unlp.reciclar.ui.maps

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import edu.unlp.reciclar.data.remote.model.maps.RouteInfo
import edu.unlp.reciclar.data.remote.model.maps.TransportMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class RecyclingMapDataLoader(private val lifecycleOwner: LifecycleOwner) {

    companion object {
        private const val ROUTE_WIDTH = 12f
    }

    fun drawWalkingRoute(
        mapView: MapView,
        from: GeoPoint,
        to: GeoPoint,
        onRouteCalculated: (RouteInfo) -> Unit
    ) {
        drawRoute(mapView, from, to, OSRMRoadManager.MEAN_BY_FOOT, Color.parseColor("#1976D2"), TransportMode.WALKING, onRouteCalculated)
    }

    fun drawDrivingRoute(
        mapView: MapView,
        from: GeoPoint,
        to: GeoPoint,
        onRouteCalculated: (RouteInfo) -> Unit
    ) {
        drawRoute(mapView, from, to, OSRMRoadManager.MEAN_BY_CAR, Color.parseColor("#D32F2F"), TransportMode.DRIVING, onRouteCalculated)
    }

    private fun drawRoute(
        mapView: MapView,
        from: GeoPoint,
        to: GeoPoint,
        mean: String,
        color: Int,
        mode: TransportMode,
        onRouteCalculated: (RouteInfo) -> Unit
    ) {
        val roadManager = OSRMRoadManager(mapView.context, mapView.context.packageName)
        roadManager.setMean(mean)

        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val road = roadManager.getRoad(arrayListOf(from, to))
                if (road.mStatus == Road.STATUS_OK) {
                    withContext(Dispatchers.Main) {
                        val overlay = RoadManager.buildRoadOverlay(road).apply {
                            outlinePaint.color = color
                            outlinePaint.strokeWidth = ROUTE_WIDTH
                        }
                        mapView.overlays.add(overlay)
                        mapView.invalidate()
                        onRouteCalculated(RouteInfo(
                            distanceKm = road.mLength,
                            durationMinutes = (road.mDuration / 60).toInt(),
                            mode = mode
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

