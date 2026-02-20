package edu.unlp.reciclar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent reemplaza setContentView(R.layout.activity_main).
        // Todo el árbol de UI es Compose desde aquí.
        // ComponentActivity en lugar de AppCompatActivity — suficiente para Compose.
        setContent {
            MainApp()
        }
    }
}
