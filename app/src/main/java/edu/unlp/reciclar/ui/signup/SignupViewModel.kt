package edu.unlp.reciclar.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.unlp.reciclar.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)
    val signupState: StateFlow<SignupState> = _signupState

    fun signup(username: String, password: String) = viewModelScope.launch {
        if (username.isBlank() || password.isBlank()) {
            _signupState.value = SignupState.Error("Por favor completa todos los campos")
            return@launch
        }

        _signupState.value = SignupState.Loading

        val result = repository.signup(username, password)

        result.onSuccess {
            _signupState.value = SignupState.Success
        }.onFailure { exception ->
            _signupState.value = SignupState.Error(exception.message ?: "Error desconocido")
        }
    }

    fun resetState() {
        _signupState.value = SignupState.Idle
    }

    sealed class SignupState {
        object Idle : SignupState()
        object Loading : SignupState()
        object Success : SignupState()
        data class Error(val message: String) : SignupState()
    }
}
