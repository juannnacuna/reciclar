package edu.unlp.reciclar.ui.maps

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import edu.unlp.reciclar.data.remote.model.maps.RouteInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class RecyclingMapDataLoader(private val lifecycleOwner: LifecycleOwner) {

    fun drawWalkingRoute(
        mapView: MapView,
        from: GeoPoint,
        to: GeoPoint,
        onRouteCalculated: (RouteInfo) -> Unit
    ) {
        val roadManager = OSRMRoadManager(mapView.context, mapView.context.packageName)
        roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)

        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val road = roadManager.getRoad(arrayListOf(from, to))
                if (road.mStatus == Road.STATUS_OK) {
                    withContext(Dispatchers.Main) {
                        val overlay = RoadManager.buildRoadOverlay(road)
                        mapView.overlays.add(overlay)
                        mapView.invalidate()
                        onRouteCalculated(RouteInfo(
                            distanceKm = road.mLength,
                            durationMinutes = (road.mDuration / 60).toInt()
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

