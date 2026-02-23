package edu.unlp.reciclar.ui.estadistica

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.unlp.reciclar.ui.utils.DatePickerComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Sub vista de Tipos de Residuos.
 * Muestra un gráfico de barras con la cantidad de residuos por tipo
 * en un período seleccionable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiposResiduosView(modifier: Modifier = Modifier) {
    val today = System.currentTimeMillis()
    val thirtyDaysAgo = today - (30L * 24 * 60 * 60 * 1000)

    var startDateMillis by remember { mutableStateOf(thirtyDaysAgo) }
    var endDateMillis by remember { mutableStateOf(today) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startDateText = formatter.format(Date(startDateMillis))
    val endDateText = formatter.format(Date(endDateMillis))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Residuos por Tipo",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Desde: $startDateText")
            }

            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Hasta: $endDateText")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Plástico" to 45,
                    "Vidrio" to 32,
                    "Papel" to 58,
                    "Metal" to 23,
                    "Cartón" to 41
                ).forEach { (tipo, cantidad) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tipo,
                            modifier = Modifier.width(80.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        // Barra de progreso visual
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "$cantidad",
                            modifier = Modifier.width(40.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    DatePickerComponent(
        showDialog = showStartDatePicker,
        onDismiss = { showStartDatePicker = false },
        onDateSelected = { startDateMillis = it },
        initialDateMillis = startDateMillis
    )

    DatePickerComponent(
        showDialog = showEndDatePicker,
        onDismiss = { showEndDatePicker = false },
        onDateSelected = { endDateMillis = it },
        initialDateMillis = endDateMillis
    )
}

/**
 * Sub vista de Estadística Semanal.
 * Placeholder para desarrollo posterior.
 */
@Composable
fun SemanalEstadisticaView(modifier: Modifier = Modifier) {
    PlaceholderEstadisticaView(
        title = "Estadística Semanal",
        description = "Vista de estadísticas de esta semana",
        modifier = modifier
    )
}

/**
 * Sub vista de Estadística Mensual.
 * Placeholder para desarrollo posterior.
 */
@Composable
fun MensualEstadisticaView(modifier: Modifier = Modifier) {
    PlaceholderEstadisticaView(
        title = "Estadística Mensual",
        description = "Vista de estadísticas de este mes",
        modifier = modifier
    )
}

/**
 * Sub vista de Estadística Anual.
 * Placeholder para desarrollo posterior.
 */
@Composable
fun AnualEstadisticaView(modifier: Modifier = Modifier) {
    PlaceholderEstadisticaView(
        title = "Estadística Anual",
        description = "Vista de estadísticas de este año",
        modifier = modifier
    )
}

/**
 * Componente placeholder para las sub vistas.
 * Muestra un título y descripción centrados.
 * Reemplaza este contenido con la UI real cuando esté listo.
 */
@Composable
private fun PlaceholderEstadisticaView(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Mapa de vistas de estadística.
 *
 * Para agregar una nueva vista:
 * 1. Crea una función Composable en este archivo
 * 2. Agrégala AQUÍ en el mapa como: "NombreVista" to { TuFuncion() }
 */
internal val estadisticaViewsMap: Map<String, @Composable () -> Unit> = mapOf(
    "Anual" to { AnualEstadisticaView() },
    "Mensual" to { MensualEstadisticaView() },
    "Semanal" to { SemanalEstadisticaView() },
    "Tipos de Residuos" to { TiposResiduosView() }
)



