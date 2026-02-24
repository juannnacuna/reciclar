package edu.unlp.reciclar.ui.estadistica

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.local.dao.CanjeDao
import edu.unlp.reciclar.data.local.dao.ResiduoDao
import edu.unlp.reciclar.data.local.entity.Residuo
import edu.unlp.reciclar.data.local.relation.CanjeConDetalle
import edu.unlp.reciclar.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BucketStats(
    val label: String,
    val cantidad: Int,
    val puntos: Int
)

data class PuntosStats(
    val puntosGanados: Int,
    val puntosGastados: Int,
    val balanceNeto: Int
)

@HiltViewModel
class EstadisticaViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val residuoDao: ResiduoDao,
    private val canjeDao: CanjeDao
) : ViewModel() {

    // ── Estado compartido ───────────────────────────────────────────────────
    private val _startDate = MutableStateFlow(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    private val _endDate = MutableStateFlow(System.currentTimeMillis())
    val startDate: StateFlow<Long> = _startDate
    val endDate: StateFlow<Long> = _endDate

    private val _tiposStats = MutableStateFlow<List<BucketStats>>(emptyList())
    val tiposStats: StateFlow<List<BucketStats>> = _tiposStats

    private val _reciclajesHistorial = MutableStateFlow<List<Residuo>>(emptyList())
    val reciclajesHistorial: StateFlow<List<Residuo>> = _reciclajesHistorial

    private val _canjesHistorial = MutableStateFlow<List<CanjeConDetalle>>(emptyList())
    val canjesHistorial: StateFlow<List<CanjeConDetalle>> = _canjesHistorial

    private val _puntosStats = MutableStateFlow(PuntosStats(0, 0, 0))
    val puntosStats: StateFlow<PuntosStats> = _puntosStats

    init {
        viewModelScope.launch {
            combine(_startDate, _endDate) { s, e -> s to e }
                .collect { (s, e) ->
                    val uid = userRepository.getUser().getOrNull()?.id ?: return@collect
                    loadTipos(uid, s, e)
                    loadReciclajesHistorial(uid, s, e)
                    loadCanjesHistorial(uid, s, e)
                    loadPuntosStats(uid, s, e)
                }
        }
    }

    fun updateDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
    }

    // ── Loaders ───────────────────────────────────────────────────────────────

    private suspend fun loadTipos(uid: Int, start: Long, end: Long) {
        val residuos = residuoDao.getReciclajesByUsuario(uid, start, end)
        _tiposStats.value = residuos
            .groupBy { it.tipo }
            .map { (tipo, list) ->
                BucketStats(
                    label = tipo,
                    cantidad = list.size,
                    puntos = list.sumOf { it.puntos }
                )
            }
            .sortedByDescending { it.cantidad }
    }

    private suspend fun loadReciclajesHistorial(uid: Int, start: Long, end: Long) {
        _reciclajesHistorial.value = residuoDao.getReciclajesByUsuario(uid, start, end)
            .sortedByDescending { it.timestamp }
    }

    private suspend fun loadCanjesHistorial(uid: Int, start: Long, end: Long) {
        _canjesHistorial.value = canjeDao.getCanjesConDetallePorUsuarioYPeriodo(uid, start, end)
            .sortedByDescending { it.canje.fechaCanje }
    }

    private suspend fun loadPuntosStats(uid: Int, start: Long, end: Long) {
        val puntosGanados = residuoDao.getReciclajesByUsuario(uid, start, end).sumOf { it.puntos }
        val puntosGastados = canjeDao.getCanjesConDetallePorUsuarioYPeriodo(uid, start, end).sumOf { it.detalle.puntosNecesarios }
        _puntosStats.value = PuntosStats(
            puntosGanados = puntosGanados,
            puntosGastados = puntosGastados,
            balanceNeto = puntosGanados - puntosGastados
        )
    }
}
