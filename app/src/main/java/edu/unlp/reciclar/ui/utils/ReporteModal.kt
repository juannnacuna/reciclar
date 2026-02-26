package edu.unlp.reciclar.ui.utils

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/** Opciones fijas del desplegable de tipo de residuo. */
private val TIPOS_RESIDUO = listOf(
    "Plástico",
    "Papel",
    "Cartón",
    "Vidrio",
    "Metal",
    "Basura"
)

/**
 * Modal para reportar un residuo.
 * Permite al usuario seleccionar un tipo sugerido desde un menú desplegable
 * y opcionalmente adjuntar una foto de evidencia.
 *
 * @param showDialog Si el modal está visible
 * @param onDismiss Callback cuando se cierra el modal
 * @param onSubmit Callback cuando se envía el reporte
 * @param tipoSugerido Valor seleccionado actualmente
 * @param onTipoSugridoChange Callback cuando cambia la selección
 * @param isLoading Si está procesando el envío
 * @param photoPath Path absoluto de la foto ya tomada (vacío si ninguna)
 * @param onTakePhoto Lambda que solicita capturar una foto;
 *   si es null, el botón de cámara no se muestra
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteModal(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    tipoSugerido: String,
    onTipoSugridoChange: (String) -> Unit,
    isLoading: Boolean = false,
    photoPath: String = "",
    onTakePhoto: (() -> Unit)? = null
) {
    if (showDialog) {
        var expanded by remember { mutableStateOf(false) }

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
                        text = "Si el residuo no fue reconocido correctamente, ayúdanos seleccionando el tipo correcto:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (!isLoading) expanded = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = tipoSugerido.ifBlank { "Seleccionar tipo…" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Residuo") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            enabled = !isLoading
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            TIPOS_RESIDUO.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        onTipoSugridoChange(tipo)
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    if (onTakePhoto != null) {
                        Spacer(modifier = Modifier.height(4.dp))

                        if (photoPath.isNotBlank()) {
                            val bitmap = remember(photoPath) {
                                BitmapFactory.decodeFile(photoPath)?.asImageBitmap()
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Foto adjunta",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Foto adjunta",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onTakePhoto,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (photoPath.isBlank()) "Adjuntar foto" else "Cambiar foto"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

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
