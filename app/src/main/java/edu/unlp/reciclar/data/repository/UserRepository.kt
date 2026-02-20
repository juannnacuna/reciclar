package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.local.dao.UsuarioDao
import edu.unlp.reciclar.data.mapper.toDomain
import edu.unlp.reciclar.data.mapper.toEntity
import edu.unlp.reciclar.data.remote.ApiService
import edu.unlp.reciclar.data.remote.dto.UserData
import edu.unlp.reciclar.domain.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService,
    private val usuarioDao: UsuarioDao
) {
    private var cachedUserRemoteId: Int? = null

    suspend fun getUser(): Result<Usuario> {
        return withContext(Dispatchers.IO) {

            // 1. Si ya tenemos el ID cacheado, buscamos en Room y devolvemos eso
            cachedUserRemoteId?.let { id ->
                val usuarioEntidad = usuarioDao.getUsuarioById(id.toString())
                if (usuarioEntidad != null) {
                    return@withContext Result.success(usuarioEntidad.toDomain())
                }
            }

            // 2. Si no hay ID cacheado, vamos a la API
            try {
                val response = apiService.getUserData()
                if (response.isSuccessful && response.body() != null) {
                    val apiUserData = response.body()!!

                    // A. Cacheamos para operaciones futuras
                    cachedUserRemoteId = apiUserData.id

                    // B. Si existe en la db lo recuperamos, si no lo insertamos (su primer login)
                    val usuarioEntidad = usuarioDao.getUsuarioById(apiUserData.id.toString())
                    if (usuarioEntidad != null) {
                        return@withContext Result.success(usuarioEntidad.toDomain())
                    } else {
                        usuarioDao.insertUsuario(apiUserData.toEntity())
                        return@withContext Result.success(apiUserData.toEntity().toDomain())
                    }
                } else {
                    Result.failure(Exception("Error API: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Utilizado en el logout
    fun clearCache() : Result<Unit> {
        return try {
            cachedUserRemoteId = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun agregarPuntos(puntosNuevos: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val id = cachedUserRemoteId ?: return@withContext Result.failure(Exception("No hay usuario logueado"))

            try {
                usuarioDao.agregarPuntos(id = id, puntosASumar = puntosNuevos)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
