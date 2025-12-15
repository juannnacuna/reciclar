package edu.unlp.reciclar.data.network.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class SignupRequest(
    val username: String,
    val password: String,
    val email: String // Ajustar según los campos requeridos por el backend
)

data class TokenResponse(
    @SerializedName("access") val accessToken: String,
    @SerializedName("refresh") val refreshToken: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh") val refreshToken: String
)

data class UserResponse(
    val id: Int,
    val username: String
)
