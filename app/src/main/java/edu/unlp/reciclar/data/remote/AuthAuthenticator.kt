package edu.unlp.reciclar.data.remote

import okhttp3.*
import edu.unlp.reciclar.data.remote.dto.RefreshTokenRequest
import android.util.Log
import edu.unlp.reciclar.data.source.SessionManager

private const val TAG = "AuthAuthenticatorDebug"
class AuthAuthenticator(
    private val tokenManager: SessionManager,
    private val apiServiceProvider: () -> ApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        // Evitar loop infinito: contar reintentos previos
        if (responseCount(response) >= 2) {
            Log.d(TAG, "Demasiados reintentos de refresh, abortando")
            return null
        }

        Log.d(TAG, "Intentando refreshear el token (intento ${responseCount(response) + 1})")

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return try {
            val refreshResponse = apiServiceProvider().refreshToken(RefreshTokenRequest(refreshToken)).execute()

            Log.d(TAG, "Refresh response: ${refreshResponse.code()}")

            if (refreshResponse.isSuccessful) {
                val newAccessToken = refreshResponse.body()!!.accessToken
                tokenManager.saveAccessToken(newAccessToken)

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                Log.d(TAG, "Refresh fallido: ${refreshResponse.code()}")
                // Si el refresh falla, limpiar tokens para no reintentar
                tokenManager.clearTokens()
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error al refrescar el token: ${e.message}")
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResp = response.priorResponse
        while (priorResp != null) {
            result++
            priorResp = priorResp.priorResponse
        }
        return result
    }
}