package edu.unlp.reciclar.ui.estadistica

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.local.dao.ResiduoDao
import edu.unlp.reciclar.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BucketStats(
    val label: String,
    val cantidad: Int,
    val puntos: Int
)

@HiltViewModel
class EstadisticaViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val residuoDao: ResiduoDao
) : ViewModel() {

    // ── Tipos de Residuos ─────────────────────────────────────────────────────
    private val _tiposStart = MutableStateFlow(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    private val _tiposEnd   = MutableStateFlow(System.currentTimeMillis())
    val tiposStart: StateFlow<Long> = _tiposStart
    val tiposEnd:   StateFlow<Long> = _tiposEnd

    private val _tiposStats = MutableStateFlow<List<BucketStats>>(emptyList())
    val tiposStats: StateFlow<List<BucketStats>> = _tiposStats

    // ── Semanal ───────────────────────────────────────────────────────────────
    private val _semanalStats = MutableStateFlow<List<BucketStats>>(emptyList())
    val semanalStats: StateFlow<List<BucketStats>> = _semanalStats

    // ── Mensual ───────────────────────────────────────────────────────────────
    private val _mensualStats = MutableStateFlow<List<BucketStats>>(emptyList())
    val mensualStats: StateFlow<List<BucketStats>> = _mensualStats

    // ── Anual ─────────────────────────────────────────────────────────────────
    private val _anualStats = MutableStateFlow<List<BucketStats>>(emptyList())
    val anualStats: StateFlow<List<BucketStats>> = _anualStats

    init {
        // Carga inicial de las vistas de período fijo
        viewModelScope.launch {
            val uid = userRepository.getUser().getOrNull()?.id ?: return@launch
            loadSemanal(uid)
            loadMensual(uid)
            loadAnual(uid)
            loadTipos(uid, _tiposStart.value, _tiposEnd.value)
        }

        // Recarga de tipos cuando cambia el rango de fechas
        viewModelScope.launch {
            combine(_tiposStart, _tiposEnd) { s, e -> s to e }
                .collect { (s, e) ->
                    val uid = userRepository.getUser().getOrNull()?.id ?: return@collect
                    loadTipos(uid, s, e)
                }
        }
    }

    fun updateTiposDateRange(start: Long, end: Long) {
        _tiposStart.value = start
        _tiposEnd.value   = end
    }

    // ── Loaders ───────────────────────────────────────────────────────────────

    private suspend fun loadTipos(uid: Int, start: Long, end: Long) {
        val residuos = residuoDao.getReciclajesByUsuario(uid, start, end)
        _tiposStats.value = residuos
            .groupBy { it.tipo }
            .map { (tipo, list) ->
                BucketStats(
                    label    = tipo,
                    cantidad = list.size,
                    puntos   = list.sumOf { it.puntos }
                )
            }
            .sortedByDescending { it.cantidad }
    }

    private suspend fun loadSemanal(uid: Int) {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val weekStart = cal.timeInMillis
        val weekEnd   = weekStart + 7L * 24 * 60 * 60 * 1000 - 1

        val residuos = residuoDao.getReciclajesByUsuario(uid, weekStart, weekEnd)

        // slots[0]=Lun … slots[6]=Dom
        val slots = Array(7) { 0 to 0 }
        residuos.forEach { r ->
            val c   = Calendar.getInstance().apply { timeInMillis = r.timestamp }
            val dow = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
            slots[dow] = (slots[dow].first + 1) to (slots[dow].second + r.puntos)
        }
        val labels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        _semanalStats.value = labels.mapIndexed { i, label ->
            BucketStats(label = label, cantidad = slots[i].first, puntos = slots[i].second)
        }
    }

    private suspend fun loadMensual(uid: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        val monthStart = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59);      cal.set(Calendar.MILLISECOND, 999)
        val monthEnd = cal.timeInMillis

        val residuos = residuoDao.getReciclajesByUsuario(uid, monthStart, monthEnd)
        val weekMap  = mutableMapOf<Int, Pair<Int, Int>>()
        residuos.forEach { r ->
            val c    = Calendar.getInstance().apply { timeInMillis = r.timestamp }
            val week = c.get(Calendar.WEEK_OF_MONTH)
            val prev = weekMap[week] ?: (0 to 0)
            weekMap[week] = (prev.first + 1) to (prev.second + r.puntos)
        }
        _mensualStats.value = (1..5).map { w ->
            val (cnt, pts) = weekMap[w] ?: (0 to 0)
            BucketStats(label = "Sem $w", cantidad = cnt, puntos = pts)
        }
    }

    private suspend fun loadAnual(uid: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        val yearStart = cal.timeInMillis

        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59);      cal.set(Calendar.MILLISECOND, 999)
        val yearEnd = cal.timeInMillis

        val residuos   = residuoDao.getReciclajesByUsuario(uid, yearStart, yearEnd)
        val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val monthMap   = mutableMapOf<Int, Pair<Int, Int>>()
        residuos.forEach { r ->
            val c     = Calendar.getInstance().apply { timeInMillis = r.timestamp }
            val month = c.get(Calendar.MONTH)
            val prev  = monthMap[month] ?: (0 to 0)
            monthMap[month] = (prev.first + 1) to (prev.second + r.puntos)
        }
        _anualStats.value = (0..11).map { m ->
            val (cnt, pts) = monthMap[m] ?: (0 to 0)
            BucketStats(label = monthNames[m], cantidad = cnt, puntos = pts)
        }
    }
}
