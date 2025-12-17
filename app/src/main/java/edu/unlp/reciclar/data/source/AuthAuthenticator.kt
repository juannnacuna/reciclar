package edu.unlp.reciclar.data.source

import okhttp3.*
import edu.unlp.reciclar.data.dto.RefreshTokenRequest
import android.util.Log

private const val TAG = "AuthAuthenticatorDebug"
class AuthAuthenticator(
    private val tokenManager: SessionManager, // Tu clase que guarda tokens
    // OJO: No uses el mismo ApiService general aquí para evitar dependencias circulares.
    // Lo ideal es tener un servicio separado o inyectarlo de forma Lazy.
    private val apiServiceProvider: () -> ApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        Log.d(TAG, "Intentando refreshear el token ${response.request().url()} ${tokenManager.getRefreshToken()} ${tokenManager.getAccessToken()}")

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return try {
            val refreshResponse = apiServiceProvider().refreshToken(RefreshTokenRequest(refreshToken)).execute()

            Log.d(TAG, "Se obtuvo el nuevo token: ${refreshResponse.code()} ${refreshResponse.body()}")

            if (refreshResponse.isSuccessful) {
                val newAccessToken = refreshResponse.body()!!.accessToken
                tokenManager.saveAccessToken(newAccessToken)

                // Recreo la request, con el access token actualizado
                response.request().newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                Log.d(TAG, "Refresh fallido: ${refreshResponse.code()} ${refreshResponse.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error al refrescar el token: ${e.message}")
            null
        }
    }
}