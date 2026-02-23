package edu.unlp.reciclar.ui.qrscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.repository.ReportesRepository
import edu.unlp.reciclar.data.repository.ResiduosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanQrViewModel @Inject constructor(private val residuosRepository: ResiduosRepository, private val reportesRepository: ReportesRepository) : ViewModel() {

    private val _statusMessage = MutableStateFlow("Presiona el botón para escanear un código QR")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _isClaimButtonVisible = MutableStateFlow(false)
    val isClaimButtonVisible: StateFlow<Boolean> = _isClaimButtonVisible

    private val _isReportButtonVisible = MutableStateFlow(false)
    val isReportButtonVisible: StateFlow<Boolean> = _isReportButtonVisible

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showReporteModal = MutableStateFlow(false)
    val showReporteModal: StateFlow<Boolean> = _showReporteModal

    private val _tipoSugerido = MutableStateFlow("")
    val tipoSugerido: StateFlow<String> = _tipoSugerido

    private var currentQrJson: String? = null

    fun onQrScanned(rawJson: String) {
        currentQrJson = rawJson
        _statusMessage.value = "QR Detectado. Toca 'Reclamar Puntos' para procesar."
        _isClaimButtonVisible.value = true
        _isReportButtonVisible.value = true
    }

    fun onScanError(error: String) {
        _statusMessage.value = error
        _isClaimButtonVisible.value = false
        _isReportButtonVisible.value = false
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
            val result = residuosRepository.reclamarResiduo(qrJson)
            if (result.isSuccess) {
                _statusMessage.value = (
                        "¡Puntos reclamados!\n" +
                        "Residuo de tipo ${result.getOrNull()?.tipoResiduo} valido por ${result.getOrNull()?.puntosGanados} puntos."
                )
                _isClaimButtonVisible.value = false
                _isReportButtonVisible.value = false
                currentQrJson = null
            } else {
                _statusMessage.value = result.exceptionOrNull()?.message ?: "Error desconocido"
                _isClaimButtonVisible.value = true
                if (_statusMessage.value.contains("Este residuo ya fue reclamado", ignoreCase = true)) {
                    _isReportButtonVisible.value = false
                }
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error de conexión: ${e.message}"
            _isClaimButtonVisible.value = true
        } finally {
            _isLoading.value = false
        }
    }

    fun reportarResiduo() {
        _showReporteModal.value = true
    }

    fun cerrarReporteModal() {
        _showReporteModal.value = false
        _tipoSugerido.value = ""
    }

    fun actualizarTipoSugerido(valor: String) {
        _tipoSugerido.value = valor
    }

    fun enviarReporte(tipoSugerido: String) = viewModelScope.launch {
        val qrJson = currentQrJson
        if (qrJson == null) {
            _statusMessage.value = "No hay QR escaneado"
            return@launch
        }

        _isLoading.value = true
        _statusMessage.value = "Enviando reporte..."

        try {
            val result = reportesRepository.reportarResiduo(qrJson, tipoSugerido)
            if (result.isSuccess) {
                _statusMessage.value = "¡Reporte enviado exitosamente! Gracias por ayudarnos a mejorar."
                _isReportButtonVisible.value = false
                currentQrJson = null
                cerrarReporteModal()
            } else {
                _statusMessage.value = result.exceptionOrNull()?.message ?: "Error al enviar el reporte"
                _isReportButtonVisible.value = true
                _isClaimButtonVisible.value = false
                cerrarReporteModal()
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error de conexión: ${e.message}"
            _isReportButtonVisible.value = true
            cerrarReporteModal()
        } finally {
            _isLoading.value = false
        }

    }
}
