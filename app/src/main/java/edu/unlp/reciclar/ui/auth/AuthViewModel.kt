package edu.unlp.reciclar.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    // Channel en lugar de LiveData<Event<...>>:
    // - Channel.BUFFERED garantiza que el evento no se pierda aunque nadie esté
    //   coleccionando en ese momento exacto.
    // - receiveAsFlow() lo expone como Flow — Compose lo consume con collect
    //   dentro de un LaunchedEffect. A diferencia de StateFlow, un Channel
    //   entrega cada evento exactamente una vez (one-shot), sin necesidad del
    //   wrapper Event<T> que usábamos antes.
    private val _logoutEvent = Channel<Result<Unit>>(Channel.BUFFERED)
    val logoutEvent = _logoutEvent.receiveAsFlow()

    fun onLogoutClicked() {
        viewModelScope.launch {
            val result = authRepository.logout()
            _logoutEvent.send(result)
        }
    }
}
