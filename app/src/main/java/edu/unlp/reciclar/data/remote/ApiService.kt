package edu.unlp.reciclar.data.remote

import edu.unlp.reciclar.data.remote.dto.Estacion
import edu.unlp.reciclar.data.remote.dto.LoginRequest
import edu.unlp.reciclar.data.remote.dto.PosicionResponse
import edu.unlp.reciclar.data.remote.dto.PuntosUsuario
import edu.unlp.reciclar.data.remote.dto.RankingResponse
import edu.unlp.reciclar.data.remote.dto.RankingUser
import edu.unlp.reciclar.data.remote.dto.ReclamarResiduoRequest
import edu.unlp.reciclar.data.remote.dto.ReclamarResiduoResponse
import edu.unlp.reciclar.data.remote.dto.RefreshTokenRequest
import edu.unlp.reciclar.data.remote.dto.SignupRequest
import edu.unlp.reciclar.data.remote.dto.TokenResponse
import edu.unlp.reciclar.data.remote.dto.TotalResiduos
import edu.unlp.reciclar.data.remote.dto.UserData
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Autenticación

    @GET("api/datos_usuario/")
    suspend fun getUserData(): Response<UserData>

    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("api/logout/")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>

    @POST("api/signup/")
    suspend fun signup(@Body request: SignupRequest): Response<UserData>

    @POST("api/token/refresh/")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<TokenResponse>

    // Estaciones

    @GET("api/estaciones/")
    suspend fun getEstaciones(): Response<List<Estacion>>

    @GET("api/estaciones/{id_estacion}")
    suspend fun getEstacion(@Path("id_estacion") idEstacion: Int): Response<String>

    // Puntos

    @GET("api/puntos/")
    suspend fun getPuntos(
        @Query("id-user") idUsuario: Int? = null
    ): Response<List<PuntosUsuario>>

    // Ranking

    @GET("api/ranking/")
    suspend fun getRanking(
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<List<RankingResponse>>

    @GET("api/ranking/posicion/")
    suspend fun getPosicionUsuario(
        @Query("id_usuario") idUsuario: Int,
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<PosicionResponse>

    @GET("api/ranking/semanal/")
    suspend fun getRankingSemanal(
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<List<RankingUser>>

    // Residuos
    @POST("api/residuo/reclamar/")
    suspend fun reclamarResiduo(@Body request: ReclamarResiduoRequest): Response<ReclamarResiduoResponse>

    @GET("api/residuos/")
    suspend fun getResiduosTotal(): Response<List<TotalResiduos>>

    @GET("api/residuos/{id_usuario}")
    suspend fun getResiduosUsuario(@Path("id_usuario") idUsuario: Int): Response<List<TotalResiduos>>

}
