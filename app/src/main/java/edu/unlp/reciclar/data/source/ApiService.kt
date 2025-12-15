package edu.unlp.reciclar.data.source

import edu.unlp.reciclar.data.model.LoginRequest
import edu.unlp.reciclar.data.model.PosicionResponse
import edu.unlp.reciclar.data.model.RankingUser
import edu.unlp.reciclar.data.model.ReclamarResiduoRequest
import edu.unlp.reciclar.data.model.RefreshTokenRequest
import edu.unlp.reciclar.data.model.SignupRequest
import edu.unlp.reciclar.data.model.TokenResponse
import edu.unlp.reciclar.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Autenticación
    @POST("api/signup/")
    suspend fun signup(@Body request: SignupRequest): Response<UserResponse>

    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("api/logout/")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>

    @POST("api/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @GET("api/datos_usuario/")
    suspend fun getUserData(): Response<UserResponse>

    // Residuos
    @POST("api/residuos/reclamar/")
    suspend fun reclamarResiduo(@Body request: ReclamarResiduoRequest): Response<Unit> // Ajustar Response si devuelve algo

    @GET("api/residuos/{id_usuario}")
    suspend fun getResiduosUsuario(@Path("id_usuario") idUsuario: Int): Response<Map<String, Int>>

    @GET("api/residuos/")
    suspend fun getResiduosTotal(): Response<Map<String, Int>>

    // Ranking
    @GET("api/ranking/")
    suspend fun getRanking(
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<List<RankingUser>>

    @GET("api/ranking/semanal/")
    suspend fun getRankingSemanal(
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<List<RankingUser>>

    @GET("api/ranking/posicion/")
    suspend fun getPosicionUsuario(
        @Query("id_usuario") idUsuario: Int,
        @Query("tipo_residuo") tipoResiduo: String? = null
    ): Response<PosicionResponse>
}
