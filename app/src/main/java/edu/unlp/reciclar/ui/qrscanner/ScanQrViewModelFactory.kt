package edu.unlp.reciclar.ui.qrscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.unlp.reciclar.data.repository.ResiduosRepository

class ScanQrViewModelFactory(private val repository: ResiduosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create (modelClass: Class< T>): T {
        if (modelClass.isAssignableFrom(ScanQrViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanQrViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}