package edu.unlp.reciclar.data.remote

import android.content.Context
import org.osmdroid.config.Configuration

object OsmdroidInitializer {

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, prefs)

        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            isDebugTileProviders = false
        }
    }
}

