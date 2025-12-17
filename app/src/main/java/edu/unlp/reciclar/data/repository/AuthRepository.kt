package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.source.ApiService
import edu.unlp.reciclar.data.source.SessionManager
import edu.unlp.reciclar.data.dto.LoginRequest
import edu.unlp.reciclar.data.dto.RefreshTokenRequest
import edu.unlp.reciclar.data.dto.SignupRequest
import edu.unlp.reciclar.data.dto.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(username: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val tokens = response.body()
                    // Validación explícita de los tokens
                    if (tokens != null && !tokens.accessToken.isNullOrBlank() && !tokens.refreshToken.isNullOrBlank()) {
                        sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                        Result.success(Unit)
                    } else {
                        // La respuesta fue exitosa pero el cuerpo es inválido o no contiene los tokens
                        Result.failure(Exception("Respuesta inválida del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error de inicio de sesión: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signup(username: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.signup(SignupRequest(username, password))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error al registrarse: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.getAccessToken() != null
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = sessionManager.getRefreshToken()
                if (refreshToken != null) {
                    apiService.logout(RefreshTokenRequest(refreshToken))
                }
                sessionManager.clearTokens()
                Result.success(Unit)
            } catch (e: Exception) {
                sessionManager.clearTokens()
                Result.failure(e)
            }
        }
    }

    suspend fun getUserData(): Result<UserData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserData()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error al obtener datos de usuario: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
