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

            cachedUserRemoteId?.let { id ->
                val usuarioConPuntos = usuarioDao.getUsuarioConPuntosById(id.toString())
                if (usuarioConPuntos != null) {
                    return@withContext Result.success(usuarioConPuntos.toDomain())
                }
            }

            try {
                val response = apiService.getUserData()
                if (response.isSuccessful && response.body() != null) {
                    val apiUserData = response.body()!!

                    cachedUserRemoteId = apiUserData.id

                    val existeEnDb = usuarioDao.getUsuarioById(apiUserData.id.toString())
                    if (existeEnDb == null) {
                        usuarioDao.insertUsuario(apiUserData.toEntity())
                    }
                    val conPuntos = usuarioDao.getUsuarioConPuntosById(apiUserData.id.toString())
                    if (conPuntos != null) {
                        return@withContext Result.success(conPuntos.toDomain())
                    } else {
                        return@withContext Result.failure(Exception("No se pudo obtener el usuario de la base de datos"))
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
}
