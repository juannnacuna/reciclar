package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.remote.ApiService
import edu.unlp.reciclar.data.source.SessionManager
import edu.unlp.reciclar.data.remote.dto.LoginRequest
import edu.unlp.reciclar.data.remote.dto.RefreshTokenRequest
import edu.unlp.reciclar.data.remote.dto.SignupRequest
import edu.unlp.reciclar.data.remote.dto.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) {

    suspend fun login(username: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val tokens = response.body()
                    if (tokens != null && !tokens.accessToken.isNullOrBlank() && !tokens.refreshToken.isNullOrBlank()) {
                        sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                        Result.success(Unit)
                    } else {
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
                userRepository.clearCache()
                Result.success(Unit)
            } catch (e: Exception) {
                sessionManager.clearTokens()
                Result.failure(e)
            }
        }
    }
}
