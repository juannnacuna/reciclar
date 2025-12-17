package edu.unlp.reciclar.data.source

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // 10.0.2.2 es la dirección especial del emulador para acceder al localhost de tu PC
    // Configurar con la URL del codespace o del container corriendo localmente
    private const val BASE_URL = "https://supreme-acorn-6j54wpq969gfrg6p-8000.app.github.dev/"

    private var apiService: ApiService? = null

    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val sessionManager = SessionManager(context)
            val authInterceptor = AuthInterceptor(sessionManager)
            val authAuthenticator = AuthAuthenticator(SessionManager(context), apiServiceProvider = { getApiService(context) })

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .authenticator(authAuthenticator)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }
}
