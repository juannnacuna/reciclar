package edu.unlp.reciclar.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Botón de "Cerrar Sesión" implementado como Composable.
 *
 * Recibe una lambda [onLogout] en lugar de depender directamente de un ViewModel
 * o Context. Esto lo hace reutilizable y fácil de previsualizar/testear.
 *
 * [modifier] se expone como parámetro para que el caller pueda controlar
 * posición y tamaño — patrón estándar en Compose.
 */
@Composable
fun LogoutButton(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onLogout,
        modifier = modifier.padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(text = "Cerrar Sesión")
    }
}

@Preview(showBackground = true)
@Composable
private fun LogoutButtonPreview() {
    MaterialTheme {
        LogoutButton(onLogout = {})
    }
}