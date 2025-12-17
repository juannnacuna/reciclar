package edu.unlp.reciclar.ui.ranking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.unlp.reciclar.data.repository.RankingRepository
import edu.unlp.reciclar.domain.model.RankingEntry
import kotlinx.coroutines.launch

class RankingViewModel(private val rankingRepository: RankingRepository) : ViewModel() {

    private val _ranking = MutableLiveData<List<RankingEntry>>()
    val ranking: LiveData<List<RankingEntry>> = _ranking

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchRanking() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = rankingRepository.getRanking()

            result.onSuccess {
                _ranking.value = it
            }.onFailure {
                _error.value = it.message
            }

            _isLoading.value = false
        }
    }
}
