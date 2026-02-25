package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.remote.ApiService
import edu.unlp.reciclar.data.remote.dto.Estacion
import javax.inject.Inject

class EstacionesRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchEstaciones(): Result<List<Estacion>> {
        return try {
            val response = apiService.getEstaciones()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error fetching estaciones: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

