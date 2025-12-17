package edu.unlp.reciclar.data.dto

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access") val accessToken: String?,
    @SerializedName("refresh") val refreshToken: String?
)
