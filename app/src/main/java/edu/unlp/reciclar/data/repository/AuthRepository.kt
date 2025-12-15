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
                if (response.isSuccessful && response.body() != null) {
                    val tokens = response.body()!!
                    sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error Login: ${response.code()} ${response.message()}"))
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
                    Result.failure(Exception("Error Signup: ${response.code()} ${response.message()}"))
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
                    // Intentamos avisar al backend
                    apiService.logout(RefreshTokenRequest(refreshToken))
                }
                // Independientemente del resultado, borramos los tokens locales
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
                    Result.failure(Exception("Error fetching user data: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
