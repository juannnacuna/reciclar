package edu.unlp.reciclar.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.repository.RankingRepository
import edu.unlp.reciclar.domain.model.RankingEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(private val rankingRepository: RankingRepository) : ViewModel() {

    // StateFlow en lugar de LiveData:
    // - Es nativo de corrutinas (no depende de androidx.lifecycle)
    // - Compose lo observa con collectAsStateWithLifecycle()
    // - Siempre tiene un valor inicial (no puede ser nulo implícitamente)
    private val _ranking = MutableStateFlow<List<RankingEntry>>(emptyList())
    val ranking: StateFlow<List<RankingEntry>> = _ranking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchRanking(tipoResiduo: String? = null, semanal: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = if (semanal) {
                rankingRepository.getRankingSemanal(tipoResiduo = tipoResiduo)
            } else {
                rankingRepository.getRanking(tipoResiduo = tipoResiduo)
            }

            result.onSuccess {
                _ranking.value = it
            }.onFailure {
                _error.value = it.message
            }

            _isLoading.value = false
        }
    }
}
