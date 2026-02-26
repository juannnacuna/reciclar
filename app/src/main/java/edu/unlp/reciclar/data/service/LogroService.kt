package edu.unlp.reciclar.data.service

import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.local.entity.UsuarioLogros
import edu.unlp.reciclar.data.service.strategy.CondicionStrategyFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio que evalúa si un usuario desbloqueó logros nuevos.
 *
 * Usa el patrón Strategy (vía [CondicionStrategyFactory]) para delegar
 * la evaluación de cada tipo de condición a una clase especializada.
 * Para agregar un nuevo tipo de logro solo hay que:
 *   1. Crear una clase que implemente CondicionStrategy
 *   2. Registrarla en CondicionStrategyFactory
 *
 * Emite los logros recién obtenidos a través de [logroObtenido] (SharedFlow)
 * para que cualquier capa de UI pueda reaccionar mostrando un diálogo global.
 */
@Singleton
class LogroService @Inject constructor(
    private val logroDao: LogroDao,
    private val strategyFactory: CondicionStrategyFactory
) {
    private val _logroObtenido = MutableSharedFlow<Logro>(extraBufferCapacity = 5)
    val logroObtenido: SharedFlow<Logro> = _logroObtenido.asSharedFlow()

    suspend fun evaluarLogros(usuarioId: Int) {
        logroDao.getAllLogros().forEach { logro ->
            try {
                if (!logroDao.yaTieneLogro(usuarioId, logro.id)) {
                    if (cumpleCondicion(usuarioId, logro.condicion)) {
                        logroDao.insert(
                            UsuarioLogros(usuarioId, logro.id)
                        )
                        _logroObtenido.emit(logro)
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