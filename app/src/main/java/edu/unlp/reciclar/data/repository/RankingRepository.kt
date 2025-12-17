package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.dto.toDomainModel
import edu.unlp.reciclar.data.source.ApiService
import edu.unlp.reciclar.domain.model.RankingEntry

class RankingRepository(private val apiService: ApiService) {

    suspend fun getRanking(tipoResiduo: String? = null): Result<List<RankingEntry>> {
        return try {
            // Pasamos el parámetro a la llamada de la API
            val response = apiService.getRanking(tipoResiduo = tipoResiduo)
            if (response.isSuccessful) {
                val rankingResponses = response.body()
                if (rankingResponses != null) {
                    val rankingEntries = rankingResponses.map { it.toDomainModel() }
                    Result.success(rankingEntries)
                } else {
                    Result.failure(Exception("La respuesta del ranking está vacía"))
                }
            } else {
                Result.failure(Exception("Error al obtener el ranking: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
