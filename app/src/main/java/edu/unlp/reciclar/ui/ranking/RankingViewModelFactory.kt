package edu.unlp.reciclar.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.unlp.reciclar.data.repository.RankingRepository

class RankingViewModelFactory(private val rankingRepository: RankingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingViewModel(rankingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
