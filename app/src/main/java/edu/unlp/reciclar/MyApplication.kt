package edu.unlp.reciclar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import edu.unlp.reciclar.data.remote.OsmdroidInitializer

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializar OSMDroid para mapas
        OsmdroidInitializer.init(this)
    }
}