package edu.unlp.reciclar.data.service

import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.entity.UsuarioLogros
import edu.unlp.reciclar.data.service.strategy.CondicionStrategyFactory
import org.json.JSONObject
import javax.inject.Inject

/**
 * Servicio que evalúa si un usuario desbloqueó logros nuevos.
 *
 * Usa el patrón Strategy (vía [CondicionStrategyFactory]) para delegar
 * la evaluación de cada tipo de condición a una clase especializada.
 * Para agregar un nuevo tipo de logro solo hay que:
 *   1. Crear una clase que implemente CondicionStrategy
 *   2. Registrarla en CondicionStrategyFactory
 */
class LogroService @Inject constructor(
    private val logroDao: LogroDao,
    private val strategyFactory: CondicionStrategyFactory
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
                e.printStackTrace()
            }
        }
    }

    private suspend fun cumpleCondicion(usuarioId: Int, condicionJson: String): Boolean {
        val json = JSONObject(condicionJson)
        val tipo = json.getString("tipo")
        val strategy = strategyFactory.get(tipo) ?: return false
        return strategy.cumple(usuarioId, json)
    }
}