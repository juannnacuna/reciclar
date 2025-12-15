package edu.unlp.reciclar.data.network.model

import com.google.gson.annotations.SerializedName

data class RankingUser(
    val id: Int,
    val username: String,
    val puntaje: Int,
    val posicion: Int? = null
)

data class PosicionResponse(
    val posicion: Int
)
