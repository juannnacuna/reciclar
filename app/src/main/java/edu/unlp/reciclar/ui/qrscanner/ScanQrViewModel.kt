package edu.unlp.reciclar.ui.qrscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.remote.dto.QrData
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

    /** Datos parseados del QR escaneado — null mientras no hay QR activo. */
    private val _qrData = MutableStateFlow<QrData?>(null)
    val qrData: StateFlow<QrData?> = _qrData

    /** Path absoluto de la foto de evidencia adjunta al reporte (vacío si no hay foto). */
    private val _photoPath = MutableStateFlow("")
    val photoPath: StateFlow<String> = _photoPath

    private var currentQrJson: String? = null

    fun onQrScanned(rawJson: String) {
        currentQrJson = rawJson
        _qrData.value = try {
            Gson().fromJson(rawJson, QrData::class.java)
        } catch (e: Exception) {
            null
        }
        _statusMessage.value = "QR Detectado. Toca 'Reclamar Puntos' para procesar."
        _isClaimButtonVisible.value = true
        _isReportButtonVisible.value = true
    }

    fun onScanError(error: String) {
        _statusMessage.value = error
        _isClaimButtonVisible.value = false
        _isReportButtonVisible.value = false
    }

    /** Guarda el path absoluto de la foto tomada para adjuntarla al reporte. */
    fun setPhotoPath(path: String) {
        _photoPath.value = path
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
                _qrData.value = null
                _photoPath.value = ""
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
        // Pre-poblar el tipo desde el QR para que el usuario no tenga que buscarlo
        _tipoSugerido.value = _qrData.value?.tipo ?: ""
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
            val result = reportesRepository.reportarResiduo(qrJson, tipoSugerido, _photoPath.value)
            if (result.isSuccess) {
                _statusMessage.value = "¡Reporte enviado exitosamente! Gracias por ayudarnos a mejorar."
                _isReportButtonVisible.value = false
                _isClaimButtonVisible.value = false
                currentQrJson = null
                _qrData.value = null
                _photoPath.value = ""
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
