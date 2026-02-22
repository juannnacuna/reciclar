package edu.unlp.reciclar.data.remote

import android.content.Context
import org.osmdroid.config.Configuration

object OsmdroidInitializer {

    fun init(context: Context) {
        // Cargar configuración desde SharedPreferences
        val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, prefs)

        // Configurar propiedades globales
        Configuration.getInstance().apply {
            userAgentValue = context.packageName  // Requerido por OpenStreetMap
            isDebugTileProviders = false
        }
    }
}

