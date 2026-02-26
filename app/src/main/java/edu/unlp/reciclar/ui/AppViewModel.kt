package edu.unlp.reciclar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.repository.AuthRepository
import edu.unlp.reciclar.data.repository.UserRepository
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
/**
 * ViewModel scoped a la actividad (activity-scoped).
 *
 * Gestiona el estado compartido por todas las pantallas autenticadas:
 * - Datos del usuario logueado (username, puntosDisponibles).
 * - Evento de logout one-shot via Channel.
 * - isLoggedIn(): verificación de sesión activa (antes en LoginViewModel).
 *
 * Al estar instanciado en MainApp con hiltViewModel(), vive mientras
 * MainActivity esté viva, sin recrearse al navegar entre pantallas.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val logroDao: LogroDao
) : ViewModel() {

    // Channel en lugar de LiveData<Event<...>>:
    // Garantiza entrega exactamente una vez (one-shot) sin el wrapper Event<T>.
    private val _logoutEvent = Channel<Result<Unit>>(Channel.BUFFERED)
    val logoutEvent = _logoutEvent.receiveAsFlow()

    private val _userState = MutableStateFlow<Usuario?>(null)
    val userState: StateFlow<Usuario?> = _userState.asStateFlow()

    private val _logroState = MutableStateFlow<ProporcionLogros?>(null)
    val logroState: StateFlow<ProporcionLogros?> = _logroState.asStateFlow()

    init {
        // Si ya hay sesión activa al crear el ViewModel (re-entrada a la app),
        // cargamos los datos del usuario inmediatamente.
        if (authRepository.isLoggedIn()) loadUser()
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    /**
     * Carga los datos del usuario desde el repositorio.
     * Se llama en el init (si ya hay sesión) y desde MainApp tras un login exitoso.
     */
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
}
