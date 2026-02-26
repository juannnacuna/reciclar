package edu.unlp.reciclar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.repository.AuthRepository
import edu.unlp.reciclar.data.repository.UserRepository
import edu.unlp.reciclar.data.service.LogroService
import edu.unlp.reciclar.domain.model.Usuario
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ProporcionLogros (
    val totales: Int,
    val obtenidos: Int
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val logroDao: LogroDao,
    private val logroService: LogroService
) : ViewModel() {

    private val _logoutEvent = Channel<Result<Unit>>(Channel.BUFFERED)
    val logoutEvent = _logoutEvent.receiveAsFlow()

    private val _userState = MutableStateFlow<Usuario?>(null)
    val userState: StateFlow<Usuario?> = _userState.asStateFlow()

    private val _logroState = MutableStateFlow<ProporcionLogros?>(null)
    val logroState: StateFlow<ProporcionLogros?> = _logroState.asStateFlow()

    private val _logrosObtenidos = MutableStateFlow<List<Logro>>(emptyList())
    val logrosObtenidos: StateFlow<List<Logro>> = _logrosObtenidos.asStateFlow()

    init {
        if (authRepository.isLoggedIn()) loadUser()

        viewModelScope.launch {
            logroService.logroObtenido.collect { logro ->
                _logrosObtenidos.value = _logrosObtenidos.value + logro
                loadUser()
            }
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    /** Carga los datos del usuario desde el repositorio. */
    fun loadUser() {
        viewModelScope.launch {
            userRepository.getUser().onSuccess { user ->
                _userState.value = user
                val total = logroDao.getAllLogros().size
                val obtenidos = logroDao.logrosObtenidos(user.id)
                _logroState.value = ProporcionLogros(totales = total, obtenidos = obtenidos)
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            val result = authRepository.logout()
            if (result.isSuccess) _userState.value = null
            _logoutEvent.send(result)
        }
    }

    /** Limpia la lista de logros mostrados en el diálogo global. */
    fun dismissLogrosObtenidos() {
        _logrosObtenidos.value = emptyList()
    }
}
