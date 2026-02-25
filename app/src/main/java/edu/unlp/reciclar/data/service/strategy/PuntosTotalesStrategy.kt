package edu.unlp.reciclar.data.service.strategy

import edu.unlp.reciclar.data.local.dao.ResiduoDao
import org.json.JSONObject
import javax.inject.Inject

/**
 * Evalúa si el usuario alcanzó cierta cantidad de puntos acumulados.
 *
 * JSON esperado: { "tipo": "puntos_totales", "valor": 200 }
 */
class PuntosTotalesStrategy @Inject constructor(
    private val residuoDao: ResiduoDao
) : CondicionStrategy {
    override suspend fun cumple(usuarioId: Int, params: JSONObject): Boolean {
        val objetivo = params.getInt("valor")
        return residuoDao.puntosTotales(usuarioId) >= objetivo
    }
}

