package edu.unlp.reciclar.data.remote.model.maps

import org.osmdroid.util.GeoPoint

data class RecyclingPoint(
    val name: String,
    val latitude: Double,
    val longitude: Double
) {
    fun toGeoPoint() = GeoPoint(latitude, longitude)
}

