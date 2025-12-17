package edu.unlp.reciclar.data.dto

import com.google.gson.annotations.SerializedName
import edu.unlp.reciclar.domain.model.RankingEntry

data class RankingUser(
    val id: Int,
    val username: String,
    val puntaje: Int,
    val posicion: Int? = null
)

data class PosicionResponse(
    val posicion: Int
)

data class RankingResponse(
    @SerializedName("username") val username: String,
    @SerializedName("total_puntos") val totalPuntos: Int
)

fun RankingResponse.toDomainModel(): RankingEntry {
    return RankingEntry(
        username = this.username,
        total_puntos = this.totalPuntos
    )
}
