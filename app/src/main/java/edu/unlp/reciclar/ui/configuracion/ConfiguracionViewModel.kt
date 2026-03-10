package edu.unlp.reciclar.ui.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.BuildConfig
import edu.unlp.reciclar.data.remote.DynamicBaseUrlInterceptor
import edu.unlp.reciclar.data.repository.ConfiguracionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val configuracionRepository: ConfiguracionRepository,
    private val dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
) : ViewModel() {

    private val _currentBaseUrl = MutableStateFlow(BuildConfig.BASE_URL)
    val currentBaseUrl: StateFlow<String> = _currentBaseUrl

    init {
        loadBaseUrl()
    }

    /**
     * Carga la URL guardada en la DB. Si no hay ninguna, usa la de BuildConfig.
     */
    private fun loadBaseUrl() {
        viewModelScope.launch {
            val savedUrl = configuracionRepository.getBaseUrl()
            if (savedUrl != null) {
                _currentBaseUrl.value = savedUrl
                dynamicBaseUrlInterceptor.customBaseUrl = savedUrl
            }
        }
    }

    /**
     * Guarda la nueva URL en la DB y actualiza el interceptor para que
     * todas las requests futuras apunten al nuevo servidor.
     */
    fun saveBaseUrl(url: String) {
        viewModelScope.launch {
            configuracionRepository.setBaseUrl(url)
            _currentBaseUrl.value = url
            dynamicBaseUrlInterceptor.customBaseUrl = url
        }
    }
}

