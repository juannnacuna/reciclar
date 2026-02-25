package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.remote.dto.toDomainModel
import edu.unlp.reciclar.data.remote.ApiService
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

    suspend fun getRankingSemanal(tipoResiduo: String? = null): Result<List<RankingEntry>> {
        return try {
            val response = apiService.getRankingSemanal(tipoResiduo = tipoResiduo)
            if (response.isSuccessful) {
                val rankingResponses = response.body()
                if (rankingResponses != null) {
                    val rankingEntries = rankingResponses.map { it.toDomainModel() }
                    Result.success(rankingEntries)
                } else {
                    Result.failure(Exception("La respuesta del ranking semanal está vacía"))
                }
            } else {
                Result.failure(Exception("Error al obtener el ranking semanal: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
