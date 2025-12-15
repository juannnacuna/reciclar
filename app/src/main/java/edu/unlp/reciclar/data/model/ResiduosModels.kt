package edu.unlp.reciclar.data.model

import com.google.gson.annotations.SerializedName

data class ReclamarResiduoRequest(
    @SerializedName("id_residuo") val idResiduo: String
)

data class TotalResiduos(
    @SerializedName("nombre") val tipoResiduo: String,
    val cantidad: Int
)