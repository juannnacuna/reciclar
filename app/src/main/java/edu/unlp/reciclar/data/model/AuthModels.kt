package edu.unlp.reciclar.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class SignupRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access") val accessToken: String,
    @SerializedName("refresh") val refreshToken: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh") val refreshToken: String
)

data class UserData(
    val id: Int,
    val username: String
)
