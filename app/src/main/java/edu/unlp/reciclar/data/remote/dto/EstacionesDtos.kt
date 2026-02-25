package edu.unlp.reciclar.data.remote.dto

import edu.unlp.reciclar.data.remote.model.maps.RecyclingPoint

data class Estacion(
    val id: Int,
    val nombre: String,
    val latitud: Double,
    val longitud: Double
)
