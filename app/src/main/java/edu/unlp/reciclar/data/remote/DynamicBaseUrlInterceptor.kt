package edu.unlp.reciclar.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que reemplaza la baseUrl de cada request si se configuró una URL personalizada.
 * Permite cambiar dinámicamente el servidor de la API sin reiniciar la app.
 */
class DynamicBaseUrlInterceptor : Interceptor {

    @Volatile
    var customBaseUrl: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val newBaseUrl = customBaseUrl?.toHttpUrlOrNull()
        if (newBaseUrl != null) {
            val newUrl = request.url.newBuilder()
                .scheme(newBaseUrl.scheme)
                .host(newBaseUrl.host)
                .port(newBaseUrl.port)
                .build()

            request = request.newBuilder()
                .url(newUrl)
                .build()
        }

        return chain.proceed(request)
    }
}

