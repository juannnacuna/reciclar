package edu.unlp.reciclar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Barra superior compartida por todas las pantallas autenticadas.
 *
 * Muestra el nombre del usuario y sus puntos disponibles a la izquierda,
 * y el botón de logout a la derecha.
 *
 * [username] y [puntosDisponibles] son nullable: mientras se carga el usuario
 * (estado inicial) simplemente no se renderizan esos textos.
 */
@Composable
fun AppTopBar(
    username: String?,
    puntosDisponibles: Int?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (username != null) {
                    Text(
                        text = "Hola, $username",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (puntosDisponibles != null) {
                    Text(
                        text = "$puntosDisponibles pts disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar Sesión")
            }
        }
        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    MaterialTheme {
        AppTopBar(
            username = "juanito",
            puntosDisponibles = 120,
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading state")
@Composable
private fun AppTopBarLoadingPreview() {
    MaterialTheme {
        AppTopBar(
            username = null,
            puntosDisponibles = null,
            onLogout = {}
        )
    }
}
