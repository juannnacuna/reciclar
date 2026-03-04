package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.local.dao.ConfiguracionDao
import edu.unlp.reciclar.data.local.entity.Configuracion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfiguracionRepository(private val configuracionDao: ConfiguracionDao) {

    companion object {
        const val CLAVE_BASE_URL = "base_url"
    }

    suspend fun getBaseUrl(): String? = withContext(Dispatchers.IO) {
        configuracionDao.getValor(CLAVE_BASE_URL)
    }

    suspend fun setBaseUrl(url: String) = withContext(Dispatchers.IO) {
        configuracionDao.setConfiguracion(Configuracion(CLAVE_BASE_URL, url))
    }
}

