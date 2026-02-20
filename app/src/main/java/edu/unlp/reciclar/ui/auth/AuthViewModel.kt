package edu.unlp.reciclar.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.repository.AuthRepository
import edu.unlp.reciclar.utils.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _logoutEvent = MutableLiveData<Event<Result<Unit>>>()
    val logoutEvent: LiveData<Event<Result<Unit>>> = _logoutEvent

    fun onLogoutClicked() {
        viewModelScope.launch {
            val result = authRepository.logout()
            _logoutEvent.value = Event(result) // Emitimos el resultado como un evento
        }
    }
}
