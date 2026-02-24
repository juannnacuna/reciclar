package edu.unlp.reciclar.ui.estadistica

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// ── Claves del mapa de vistas (usadas también en el selector) ─────────────────
internal val estadisticaViewKeys = listOf("Anual", "Mensual", "Semanal", "Tipos de Residuos")

// ── Gráfico de barras horizontal compartido ───────────────────────────────────

/**
 * Gráfico de barras horizontal genérico.
 *
 * @param stats       Lista de buckets a representar.
 * @param valueLabel  Función que convierte un [BucketStats] en el texto de valor a mostrar.
 * @param valueOf     Función que extrae el valor numérico para calcular la longitud de la barra.
 */
@Composable
private fun BarraHorizontalChart(
    stats: List<BucketStats>,
    modifier: Modifier = Modifier,
    labelWidth: Int = 72,
    valueLabel: (BucketStats) -> String = { "${it.cantidad}" },
    valueOf: (BucketStats) -> Int = { it.cantidad }
) {
    val maxVal = (stats.maxOfOrNull { valueOf(it) } ?: 0).coerceAtLeast(1)

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(stats) { bucket ->
            val fraction = valueOf(bucket).toFloat() / maxVal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bucket.label,
                    modifier = Modifier.width(labelWidth.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
                Text(
                    text = valueLabel(bucket),
                    modifier = Modifier.width(52.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ── Tipos de Residuos ─────────────────────────────────────────────────────────

/**
 * Sub vista de Tipos de Residuos.
 * Muestra un gráfico de barras con la cantidad de reciclajes por tipo
 * en un período seleccionable por el usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiposResiduosView(
    stats: List<BucketStats>,
    startDateMillis: Long,
    endDateMillis: Long,
    onDateRangeChanged: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    val formatter    = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startText    = formatter.format(Date(startDateMillis))
    val endText      = formatter.format(Date(endDateMillis))
    val totalEntries = stats.sumOf { it.cantidad }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Residuos por Tipo",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "$totalEntries reciclajes en el período",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showStartPicker = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Desde: $startText")
            }
            OutlinedButton(
                onClick = { showEndPicker = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Hasta: $endText")
            }
        }

        if (stats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin reciclajes en este período",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            BarraHorizontalChart(
                stats = stats,
                modifier = Modifier.weight(1f),
                labelWidth = 72,
                valueLabel = { "${it.cantidad} (${it.puntos} pts)" },
                valueOf    = { it.cantidad }
            )
        }
    }

    DatePickerComponent(
        showDialog      = showStartPicker,
        onDismiss       = { showStartPicker = false },
        onDateSelected  = { onDateRangeChanged(it, endDateMillis) },
        initialDateMillis = startDateMillis
    )
    DatePickerComponent(
        showDialog      = showEndPicker,
        onDismiss       = { showEndPicker = false },
        onDateSelected  = { onDateRangeChanged(startDateMillis, it) },
        initialDateMillis = endDateMillis
    )
}

// ── Semanal ───────────────────────────────────────────────────────────────────

/**
 * Sub vista de Estadística Semanal.
 * Muestra los reciclajes de cada día de la semana actual (Lun–Dom).
 */
@Composable
fun SemanalEstadisticaView(
    stats: List<BucketStats>,
    modifier: Modifier = Modifier
) {
    PeriodoEstadisticaView(
        title    = "Esta Semana",
        subtitle = "${stats.sumOf { it.cantidad }} reciclajes · ${stats.sumOf { it.puntos }} pts",
        stats    = stats,
        modifier = modifier
    )
}

// ── Mensual ───────────────────────────────────────────────────────────────────

/**
 * Sub vista de Estadística Mensual.
 * Muestra los reciclajes agrupados por semana del mes actual.
 */
@Composable
fun MensualEstadisticaView(
    stats: List<BucketStats>,
    modifier: Modifier = Modifier
) {
    PeriodoEstadisticaView(
        title    = "Este Mes",
        subtitle = "${stats.sumOf { it.cantidad }} reciclajes · ${stats.sumOf { it.puntos }} pts",
        stats    = stats,
        modifier = modifier
    )
}

// ── Anual ─────────────────────────────────────────────────────────────────────

/**
 * Sub vista de Estadística Anual.
 * Muestra los reciclajes agrupados por mes del año actual.
 */
@Composable
fun AnualEstadisticaView(
    stats: List<BucketStats>,
    modifier: Modifier = Modifier
) {
    PeriodoEstadisticaView(
        title    = "Este Año",
        subtitle = "${stats.sumOf { it.cantidad }} reciclajes · ${stats.sumOf { it.puntos }} pts",
        stats    = stats,
        modifier = modifier
    )
}

// ── Layout compartido para vistas de período ──────────────────────────────────

@Composable
private fun PeriodoEstadisticaView(
    title: String,
    subtitle: String,
    stats: List<BucketStats>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (stats.all { it.cantidad == 0 }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin reciclajes en este período",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            BarraHorizontalChart(
                stats      = stats,
                modifier   = Modifier.weight(1f),
                labelWidth = 52,
                valueLabel = { if (it.cantidad > 0) "${it.cantidad}" else "" },
                valueOf    = { it.cantidad }
            )
        }
    }
}
