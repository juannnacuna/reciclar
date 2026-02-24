package edu.unlp.reciclar.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.remote.model.maps.RecyclingPoint
import edu.unlp.reciclar.data.remote.model.maps.RouteInfo
import edu.unlp.reciclar.data.remote.dto.Estacion
import edu.unlp.reciclar.data.repository.EstacionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class RecyclingMapViewModel @Inject constructor(
    private val estacionesRepository: EstacionesRepository
) : ViewModel() {

    // Punto de reciclaje seleccionado actualmente
    private val _selectedPoint = MutableStateFlow<RecyclingPoint?>(null)
    val selectedPoint: StateFlow<RecyclingPoint?> = _selectedPoint

    // Lista de puntos traídos desde la API
    private val _points = MutableStateFlow<List<RecyclingPoint>>(emptyList())
    val points: StateFlow<List<RecyclingPoint>> = _points

    // Información de la ruta calculada
    private val _routeInfo = MutableStateFlow<RouteInfo?>(null)
    val routeInfo: StateFlow<RouteInfo?> = _routeInfo

    // Ubicación actual del usuario (punto de origen)
    private val _originPoint = MutableStateFlow<GeoPoint?>(null)
    val originPoint: StateFlow<GeoPoint?> = _originPoint

    init {
        loadEstaciones()
    }

    private fun toRecyclingPoint(estacion: Estacion) = RecyclingPoint(
        name = estacion.nombre,
        latitude = estacion.latitud,
        longitude = estacion.longitud
    )

    fun loadEstaciones() {
        viewModelScope.launch {
            val result = estacionesRepository.fetchEstaciones()
            result.onSuccess { estaciones ->
                _points.value = estaciones.map { toRecyclingPoint(it) }
            }
            result.onFailure { error ->
                _points.value = emptyList()
            }
        }
    }

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
