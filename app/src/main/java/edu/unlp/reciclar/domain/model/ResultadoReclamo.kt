package edu.unlp.reciclar.domain.model

data class ResultadoReclamo(
    val mensajeServidor: String,
    val puntosGanados: Int,
    val tipoResiduo: String
)