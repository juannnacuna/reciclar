package edu.unlp.reciclar.ui.utils

import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable

/**
 * Componente reutilizable para seleccionar una fecha.
 * Muestra un diálogo con DatePicker cuando [showDialog] es true.
 *
 * @param showDialog Si el diálogo está visible
 * @param onDismiss Callback cuando se cierra el diálogo
 * @param onDateSelected Callback cuando se confirma una fecha (recibe milisegundos)
 * @param initialDateMillis Fecha inicial del picker (en milisegundos)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerComponent(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    initialDateMillis: Long
) {
    if (showDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

