package edu.unlp.reciclar.data.repository

import com.google.gson.Gson
import edu.unlp.reciclar.data.dto.QrData
import edu.unlp.reciclar.data.dto.ReclamarResiduoRequest
import edu.unlp.reciclar.data.dto.ReclamarResiduoResponse
import edu.unlp.reciclar.data.source.ApiService
import edu.unlp.reciclar.domain.model.ResultadoReclamo

class ResiduosRepository(private val apiService: ApiService) {

    suspend fun reclamarResiduo(rawJson: String): Result<ResultadoReclamo> {
        // Intenta parsear el QR
        val qrData = try {
            Gson().fromJson(rawJson, QrData::class.java)
        } catch (e: Exception) {
            return Result.failure(Exception("Error: El formato del QR no es válido\n${e.message}"))
        }

        val response = apiService.reclamarResiduo(ReclamarResiduoRequest(qrData.id))

        if (response.isSuccessful) {
            return Result.success(
                ResultadoReclamo(
                    mensajeServidor = response.body()?.mensajeExito ?: "Residuo reclamado exitosamente",
                    puntosGanados = qrData.puntos,
                    tipoResiduo = qrData.tipo
                )
            )
        } else {
            val errorJson = response.errorBody()?.string()
            val errorObj = Gson().fromJson(errorJson, ReclamarResiduoResponse::class.java)
            return Result.failure(Exception(errorObj.mensajeError ?: "Error al reclamar el residuo"))
        }
    }
}
