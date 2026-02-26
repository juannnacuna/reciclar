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

/** Evalúa si un usuario desbloqueó logros nuevos y emite los obtenidos a [logroObtenido]. */
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