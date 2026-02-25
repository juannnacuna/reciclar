package edu.unlp.reciclar.data.service

import edu.unlp.reciclar.data.local.dao.CanjeDao
import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.dao.ResiduoDao
import edu.unlp.reciclar.data.local.entity.UsuarioLogros
import org.json.JSONObject
import javax.inject.Inject

class LogroService @Inject constructor(
    private val residuoDao: ResiduoDao,
    private val logroDao: LogroDao,
    private val canjeDao: CanjeDao
) {
    suspend fun evaluarLogros(usuarioId: Int) {
        logroDao.getAllLogros().forEach { logro ->
            try {
                if (!logroDao.yaTieneLogro(usuarioId, logro.id)) {
                    if (cumpleCondicion(usuarioId, logro.condicion)) {
                        logroDao.insert(
                            UsuarioLogros(usuarioId, logro.id)
                        )
                    }
                }
            } catch (e: Exception) {
                // Si falla un logro individual, seguir con los demás
                e.printStackTrace()
            }
        }
    }

    private suspend fun cumpleCondicion(usuarioId: Int, condicionJson: String): Boolean {
        val json = JSONObject(condicionJson)

        return when (json.getString("tipo")) {

            "puntos_totales" -> {
                val objetivo = json.getInt("valor")
                residuoDao.puntosTotales(usuarioId) >= objetivo
            }

            "residuos_tipo" -> {
                val tipo = json.getString("residuo")
                val cantidad = json.getInt("cantidad")
                residuoDao.residuosTipo(usuarioId, tipo) >= cantidad
            }

            "canjes_totales" -> {
                val objetivo = json.getInt("valor")
                canjeDao.totalCanjes(usuarioId) >= objetivo
            }

            else -> false
        }
    }
}