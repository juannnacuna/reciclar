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

        Log.d(TAG, "Se intentó refreshear el token ${response.request().url()} ${tokenManager.getRefreshToken()} ${tokenManager.getAccessToken()}")

        // 1. Evitar bucles infinitos: Si ya intentamos más de una vez, nos rendimos.
        /*
        if (response.responseCount >= 2) {
            return null // Esto devuelve el error 401 original a la UI para que desloguee
        }
         */

        // 2. Obtener el refresh token guardado
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        // 3. Llamada SINCRÓNICA para renovar (usando .execute() de Retrofit)
        // No uses corrutinas (suspend) aquí directamente si puedes evitarlo,
        // .execute() es más seguro dentro de este bloque de OkHttp.
        return try {
            val refreshResponse = apiServiceProvider().refreshToken(RefreshTokenRequest(refreshToken)).execute()

            Log.d(TAG, "Se obtuvo el nuevo token: ${refreshResponse.code()} ${refreshResponse.body()}")

            if (refreshResponse.isSuccessful) {
                val newAccessToken = refreshResponse.body()!!.accessToken

                // 4. Guardar el nuevo token
                tokenManager.saveAccessToken(newAccessToken)

                // 5. IMPORTANTE: Crear una nueva request idéntica a la anterior
                // pero con el header actualizado.
                response.request().newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                Log.d(TAG, "Error al refrescar el token: ${refreshResponse.code()} ${refreshResponse.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error al refrescar el token: ${e.message}")
            null
        }
    }
}