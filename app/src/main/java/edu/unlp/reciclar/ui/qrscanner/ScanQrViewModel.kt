package edu.unlp.reciclar.ui.qrscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.unlp.reciclar.data.repository.ResiduosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScanQrViewModel(private val repository: ResiduosRepository) : ViewModel() {

    private val _statusMessage = MutableStateFlow("Presiona el botón para escanear un código QR")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _isClaimButtonVisible = MutableStateFlow(false)
    val isClaimButtonVisible: StateFlow<Boolean> = _isClaimButtonVisible

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentQrJson: String? = null

    fun onQrScanned(rawJson: String) {
        currentQrJson = rawJson
        _statusMessage.value = "QR Detectado. Toca 'Reclamar Puntos' para procesar."
        _isClaimButtonVisible.value = true
    }

    fun onScanError(error: String) {
        _statusMessage.value = error
        _isClaimButtonVisible.value = false
    }

    fun reclamarPuntos() = viewModelScope.launch {
        val qrJson = currentQrJson
        if (qrJson == null) {
            _statusMessage.value = "No hay QR escaneado"
            return@launch
        }

        _isLoading.value = true
        _statusMessage.value = "Procesando..."
        
        try {
            val result = repository.reclamarResiduo(qrJson)
            if (result.isSuccess) {
                _statusMessage.value = (
                        "¡Puntos reclamados!\n" +
                        "Residuo de tipo ${result.getOrNull()?.tipoResiduo} valido por ${result.getOrNull()?.puntosGanados} puntos."
                )
                _isClaimButtonVisible.value = false
                currentQrJson = null
            } else {
                _statusMessage.value = result.exceptionOrNull()?.message ?: "Error desconocido"
                _isClaimButtonVisible.value = true
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error de conexión: ${e.message}"
            _isClaimButtonVisible.value = true
        } finally {
            _isLoading.value = false
        }
    }
}
