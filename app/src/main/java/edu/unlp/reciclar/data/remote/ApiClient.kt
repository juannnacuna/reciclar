package edu.unlp.reciclar.data.remote

import android.content.Context
import edu.unlp.reciclar.data.source.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import edu.unlp.reciclar.BuildConfig

object ApiClient {
    private const val BASE_URL = BuildConfig.BASE_URL

    private var apiService: ApiService? = null

    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val sessionManager = SessionManager(context)
            val authInterceptor = AuthInterceptor(sessionManager)
            val authAuthenticator = AuthAuthenticator(
                SessionManager(context),
                apiServiceProvider = { getApiService(context) })

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
