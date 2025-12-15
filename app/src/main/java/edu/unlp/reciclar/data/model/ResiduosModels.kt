package edu.unlp.reciclar.data.model

import com.google.gson.annotations.SerializedName

data class ReclamarResiduoRequest(
    @SerializedName("id_residuo") val idResiduo: String
)

// Asumiendo que la respuesta de cantidad de residuos es un mapa o una lista.
// Ajustar según la respuesta real del backend.
data class ResiduoStat(
    @SerializedName("tipo_residuo") val tipoResiduo: String,
    @SerializedName("cantidad") val cantidad: Int
)

data class ResiduosResponse(
    val residuos: List<ResiduoStat> // O Map<String, Int> si el JSON es un objeto plano
)
