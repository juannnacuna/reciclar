package edu.unlp.reciclar.ui.logros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogrosViewModel @Inject constructor(
    private val logroDao: LogroDao,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _logrosState = MutableStateFlow<List<LogroScreen>>(emptyList())
    val logrosState: StateFlow<List<LogroScreen>> = _logrosState.asStateFlow()

    init {
        loadLogros()
    }

    private fun loadLogros() {
        viewModelScope.launch {
            val usuarioId = userRepository.getUser().getOrNull()?.id ?: return@launch
            val todos = logroDao.getAllLogros()

            val logrosScreen = todos.map { logro ->
                val obtenido = logroDao.yaTieneLogro(usuarioId, logro.id)
                LogroScreen(logro, obtenido)
            }
            _logrosState.value = logrosScreen
        }
    }
}
