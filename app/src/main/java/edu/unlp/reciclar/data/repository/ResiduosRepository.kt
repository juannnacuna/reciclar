package edu.unlp.reciclar.data.repository

import com.google.gson.Gson
import edu.unlp.reciclar.data.local.dao.ResiduoDao
import edu.unlp.reciclar.data.local.entity.Residuo
import edu.unlp.reciclar.data.remote.dto.QrData
import edu.unlp.reciclar.data.remote.dto.ReclamarResiduoRequest
import edu.unlp.reciclar.data.remote.dto.ReclamarResiduoResponse
import edu.unlp.reciclar.data.remote.ApiService
import edu.unlp.reciclar.data.service.LogroService
import edu.unlp.reciclar.domain.model.ResultadoReclamo
import edu.unlp.reciclar.ui.AppViewModel


class ResiduosRepository(
    private val apiService: ApiService,
    private val logroService: LogroService,
    private val usuarioRepository: UserRepository,
    private val residuosDao: ResiduoDao
) {

    suspend fun reclamarResiduo(rawJson: String): Result<ResultadoReclamo> {
        // Intenta parsear el QR
        val qrData = try {
            Gson().fromJson(rawJson, QrData::class.java)
        } catch (e: Exception) {
            return Result.failure(Exception("Error: El formato del QR no es válido\n${e.message}"))
        }

        val response = apiService.reclamarResiduo(ReclamarResiduoRequest(qrData.id))

        if (response.isSuccessful) {
            // Obtener el ID del usuario actualmente logueado
            val usuarioId = usuarioRepository.getUser().getOrNull()?.id
                ?: return Result.failure(Exception("No hay usuario logueado"))

            // Crear y guardar el residuo en la base de datos local
            val residuo = Residuo(
                usuarioId = usuarioId,
                timestamp = System.currentTimeMillis(),
                qrCode = qrData.id,

                tipo = qrData.tipo,
                puntos = qrData.puntos
            )
            residuosDao.insertReciclaje(residuo)
            logroService.evaluarLogros(usuarioId)

            // Los puntos se calculan automáticamente desde la vista usuarios_con_puntos
            // no es necesario actualizarlos manualmente en la entidad Usuario.

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
