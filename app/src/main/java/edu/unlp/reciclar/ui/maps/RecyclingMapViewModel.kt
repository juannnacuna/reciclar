package edu.unlp.reciclar.ui.maps

import androidx.lifecycle.ViewModel
import edu.unlp.reciclar.data.remote.model.maps.RecyclingPoint
import edu.unlp.reciclar.data.remote.model.maps.RouteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class RecyclingMapViewModel : ViewModel() {

    // Punto de reciclaje seleccionado actualmente
    private val _selectedPoint = MutableStateFlow<RecyclingPoint?>(null)
    val selectedPoint: StateFlow<RecyclingPoint?> = _selectedPoint

    // Información de la ruta calculada
    private val _routeInfo = MutableStateFlow<RouteInfo?>(null)
    val routeInfo: StateFlow<RouteInfo?> = _routeInfo

    // Ubicación actual del usuario (punto de origen)
    private val _originPoint = MutableStateFlow<GeoPoint?>(null)
    val originPoint: StateFlow<GeoPoint?> = _originPoint

    // Actualizar punto de origen
    fun setOriginPoint(origin: GeoPoint) {
        _originPoint.value = origin
    }

    // Seleccionar punto de reciclaje
    fun selectPoint(point: RecyclingPoint) {
        _selectedPoint.value = point
    }

    // Actualizar información de ruta
    fun setRouteInfo(info: RouteInfo) {
        _routeInfo.value = info
    }

    // Limpiar información de ruta
    fun clearRouteInfo() {
        _routeInfo.value = null
    }
}

