package edu.unlp.reciclar.data.service.strategy

import edu.unlp.reciclar.data.local.dao.ResiduoDao
import org.json.JSONObject
import javax.inject.Inject

/**
 * Evalúa si el usuario recicló cierta cantidad de residuos de un tipo específico.
 *
 * JSON esperado: { "tipo": "residuos_tipo", "residuo": "Plastico", "cantidad": 10 }
 */
class ResiduosTipoStrategy @Inject constructor(
    private val residuoDao: ResiduoDao
) : CondicionStrategy {
    override suspend fun cumple(usuarioId: Int, params: JSONObject): Boolean {
        val tipo = params.getString("residuo")
        val cantidad = params.getInt("cantidad")
        return residuoDao.residuosTipo(usuarioId, tipo) >= cantidad
    }
}

