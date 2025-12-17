package edu.unlp.reciclar.data.dto

import com.google.gson.annotations.SerializedName
import edu.unlp.reciclar.domain.model.RankingEntry

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
