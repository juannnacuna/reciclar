package edu.unlp.reciclar.data.service.strategy

import edu.unlp.reciclar.data.local.dao.CanjeDao
import org.json.JSONObject
import javax.inject.Inject

/**
 * Evalúa si el usuario realizó cierta cantidad de canjes de cupones.
 *
 * JSON esperado: { "tipo": "canjes_totales", "valor": 10 }
 */
class CanjesTotalesStrategy @Inject constructor(
    private val canjeDao: CanjeDao
) : CondicionStrategy {
    override suspend fun cumple(usuarioId: Int, params: JSONObject): Boolean {
        val objetivo = params.getInt("valor")
        return canjeDao.totalCanjes(usuarioId) >= objetivo
    }
}

