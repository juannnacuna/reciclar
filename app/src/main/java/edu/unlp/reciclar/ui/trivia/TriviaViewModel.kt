package edu.unlp.reciclar.ui.trivia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.local.entity.Consejo
import edu.unlp.reciclar.data.local.entity.Trivia
import edu.unlp.reciclar.data.repository.TriviaEstadisticas
import edu.unlp.reciclar.data.repository.TriviaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de trivias.
 */
data class TriviaUiState(
    val triviaActual: Trivia? = null,
    val consejoActual: Consejo? = null,
    val estadisticas: TriviaEstadisticas = TriviaEstadisticas(),
    val respuestaSeleccionada: Int? = null,
    val mostrandoResultado: Boolean = false,
    val esCorrecta: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class TriviaViewModel @Inject constructor(
    private val triviaRepository: TriviaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriviaUiState())
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()

    init {
        cargarContenido()
    }

    private fun cargarContenido() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val trivia = triviaRepository.getSiguienteTrivia()
            val consejo = triviaRepository.getConsejoRandom()
            val stats = triviaRepository.getEstadisticas()

            _uiState.value = TriviaUiState(
                triviaActual = trivia,
                consejoActual = consejo,
                estadisticas = stats,
                isLoading = false
            )
        }
    }

    fun seleccionarRespuesta(index: Int) {
        val trivia = _uiState.value.triviaActual ?: return
        if (_uiState.value.mostrandoResultado) return

        viewModelScope.launch {
            val esCorrecta = triviaRepository.responderTrivia(trivia, index)
            val stats = triviaRepository.getEstadisticas()

            _uiState.value = _uiState.value.copy(
                respuestaSeleccionada = index,
                mostrandoResultado = true,
                esCorrecta = esCorrecta,
                estadisticas = stats
            )
        }
    }

    fun siguienteTrivia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val trivia = triviaRepository.getSiguienteTrivia()
            val consejo = triviaRepository.getConsejoRandom()
            val stats = triviaRepository.getEstadisticas()

            _uiState.value = TriviaUiState(
                triviaActual = trivia,
                consejoActual = consejo,
                estadisticas = stats,
                isLoading = false
            )
        }
    }
}
