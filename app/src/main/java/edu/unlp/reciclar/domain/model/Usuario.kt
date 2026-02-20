package edu.unlp.reciclar.domain.model

data class Usuario(
    val id: Int,
    val username: String,
    val puntosTotales: Int,
    val puntosDisponibles: Int,
)