package edu.unlp.reciclar.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.unlp.reciclar.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Completa todos los campos")
            return@launch
        }

        _loginState.value = LoginState.Loading
        
        val result = repository.login(username, password)
        
        result.onSuccess {
             _loginState.value = LoginState.Success(username)
        }.onFailure { exception ->
             _loginState.value = LoginState.Error(exception.message ?: "Error desconocido")
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val username: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
