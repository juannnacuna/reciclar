package edu.unlp.reciclar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint


/**
 * Main activity de la aplicación.
 *
 * Es el punto de entrada de Android — la "puerta" del sistema operativo hacia la app.
 * La activity es instanciada por el Sistema Operativo cuando el usuario abre la app.
 * Llama a setContent { MainApp() }, que es el "puente" entre el mundo Android y el mundo Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp()
        }
    }
}
