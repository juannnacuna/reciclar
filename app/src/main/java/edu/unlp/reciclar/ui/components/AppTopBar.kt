package edu.unlp.reciclar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Barra superior compartida por todas las pantallas autenticadas.
 */
@Composable
fun AppTopBar(
    username: String?,
    puntosDisponibles: Int?,
    logrosObtenidos: Int?,
    logrosTotales: Int?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (puntosDisponibles != null) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = "Puntos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$puntosDisponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (logrosObtenidos != null && logrosTotales != null) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Logros",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$logrosObtenidos/$logrosTotales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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

@Preview(showBackground = true, name = "Usuario activo")
@Composable
private fun AppTopBarPreview() {
    MaterialTheme {
        AppTopBar(
            username = "reciclador_pro",
            puntosDisponibles = 320,
            logrosObtenidos = 5,
            logrosTotales = 10,
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, name = "Cargando usuario")
@Composable
private fun AppTopBarLoadingPreview() {
    MaterialTheme {
        AppTopBar(
            username = null,
            puntosDisponibles = null,
            logrosObtenidos = null,
            logrosTotales = null,
            onLogout = {}
        )
    }
}
