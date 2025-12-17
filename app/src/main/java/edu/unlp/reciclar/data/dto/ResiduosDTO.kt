package edu.unlp.reciclar.data.dto

import com.google.gson.annotations.SerializedName

data class ReclamarResiduoRequest(
    val id_residuo: String
)

data class TotalResiduos(
    @SerializedName("nombre") val tipoResiduo: String,
    val cantidad: Int
)

data class QrData(
    @SerializedName("ID Residuo") val id: String,
    @SerializedName("Puntos") val puntos: Int,
    @SerializedName("Tipo Residuo") val tipo: String
)

data class ReclamarResiduoResponse(
    @SerializedName("message") val mensajeExito: String?,
    @SerializedName("error") val mensajeError: String?
)