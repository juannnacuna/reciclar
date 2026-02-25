package edu.unlp.reciclar.data.repository

import com.google.gson.Gson
import edu.unlp.reciclar.data.local.dao.ReporteDao
import edu.unlp.reciclar.data.local.entity.Reporte
import edu.unlp.reciclar.data.remote.dto.QrData

class ReportesRepository(
    private val usuarioRepository: UserRepository,
    private val reporteDao: ReporteDao
) {
    suspend fun reportarResiduo(
        rawJson: String,
        tipoSugerido: String,
        photoPath: String = ""
    ): Result<Unit> {
        return try {
            // Intenta parsear el QR
            val qrData = try {
                Gson().fromJson(rawJson, QrData::class.java)
            } catch (e: Exception) {
                return Result.failure(Exception("Error: El formato del QR no es válido\n${e.message}"))
            }

            // Obtener el ID del usuario actualmente logueado+
            val usuarioId = usuarioRepository.getUser().getOrNull()?.id
                ?: return Result.failure(Exception("No hay usuario logueado"))

            // Crear y guardar el reporte en la base de datos local
            val reporte = Reporte(
                usuarioId = usuarioId,
                timestamp = System.currentTimeMillis(),
                qrCode = qrData.id,
                photoPath = photoPath,
                tipoSugerido = tipoSugerido
            )
            reporteDao.insertReporte(reporte)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar el reporte: ${e.message}"))
        }
    }
}