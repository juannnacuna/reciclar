package edu.unlp.reciclar.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Modal para reportar un residuo.
 * Permite al usuario ingresar un tipo sugerido y una descripción del problema.
 *
 * @param showDialog Si el modal está visible
 * @param onDismiss Callback cuando se cierra el modal
 * @param onSubmit Callback cuando se envía el reporte (tipoSugerido, descripcion)
 * @param tipoSugerido Valor actual del campo tipo sugerido
 * @param onTipoSugridoChange Callback cuando cambia el tipo sugerido
 * @param descripcion Valor actual del campo descripción
 * @param onDescripcionChange Callback cuando cambia la descripción
 * @param isLoading Si está procesando el envío
 */
@Composable
fun ReporteModal(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    tipoSugerido: String,
    onTipoSugridoChange: (String) -> Unit,
    isLoading: Boolean = false
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Reportar Residuo",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Si el residuo no fue reconocido correctamente, ayúdanos completando los siguientes campos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = tipoSugerido,
                        onValueChange = onTipoSugridoChange,
                        label = { Text("Tipo de Residuo") },
                        placeholder = { Text("Ej: Plástico, Vidrio, etc.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        enabled = !isLoading
                    )


                    Text(
                        text = "Tus reportes nos ayudan a mejorar la precisión de la aplicación.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tipoSugerido.isNotBlank()) {
                            onSubmit(tipoSugerido)
                        }
                    },
                    enabled = tipoSugerido.isNotBlank() && !isLoading
                ) {
                    Text("Enviar Reporte")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}


