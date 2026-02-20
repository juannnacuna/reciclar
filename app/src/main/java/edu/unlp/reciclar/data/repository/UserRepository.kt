package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.local.dao.UsuarioDao
import edu.unlp.reciclar.data.mapper.toEntity
import edu.unlp.reciclar.data.remote.ApiService
import edu.unlp.reciclar.data.remote.dto.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService,
    private val usuarioDao: UsuarioDao
) {
    private var cachedUserData: UserData? = null

    suspend fun getUserData(): Result<UserData> {
        return withContext(Dispatchers.IO) {

            // 1. devolver la userdata cacheada
            cachedUserData?.let {
                return@withContext Result.success(it)
            }

            // 2. si no está cacheada, obtenerla de la API (y guardarla en cache y db)
            try {
                val response = apiService.getUserData()
                if (response.isSuccessful && response.body() != null) {
                    val apiUserData = response.body()!!

                    usuarioDao.insertUsuario(apiUserData.toEntity())
                    cachedUserData = apiUserData

                    Result.success(apiUserData)
                } else {
                    Result.failure(Exception("Error al obtener datos de usuario: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Utilizado en el logout
    fun clearCache() : Result<Unit> {
        return try {
            cachedUserData = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}