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
        Log.d(TAG, "Intentando refreshear el token ${response.request().url()}")

        val refreshToken = tokenManager.getRefreshToken()
        val accessToken = tokenManager.getAccessToken()

        // Si no hay tokens, la sesión fue limpiada (logout exitoso)
        // Retornar null para evitar bucle infinito
        if (refreshToken.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            Log.d(TAG, "No hay tokens. Sesión probablemente limpiada por logout.")
            return null
        }

        return try {
            val refreshResponse = apiServiceProvider().refreshToken(RefreshTokenRequest(refreshToken)).execute()

            Log.d(TAG, "Respuesta de refresh: ${refreshResponse.code()}")

            if (refreshResponse.isSuccessful) {
                val newAccessToken = refreshResponse.body()?.accessToken
                if (!newAccessToken.isNullOrEmpty()) {
                    tokenManager.saveAccessToken(newAccessToken)
                    Log.d(TAG, "Token actualizado exitosamente")

                    // Recrear la request con el nuevo token
                    response.request().newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    Log.d(TAG, "Respuesta exitosa pero token vacío")
                    null
                }
            } else {
                Log.d(TAG, "Refresh fallido: ${refreshResponse.code()}")
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error al refrescar el token: ${e.message}")
            null
        }
    }
}