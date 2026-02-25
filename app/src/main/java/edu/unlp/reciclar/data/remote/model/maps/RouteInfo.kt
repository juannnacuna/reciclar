package edu.unlp.reciclar.data.remote.model.maps

enum class TransportMode { WALKING, DRIVING }

data class RouteInfo(
    val distanceKm: Double,
    val durationMinutes: Int,
    val mode: TransportMode = TransportMode.WALKING
)

