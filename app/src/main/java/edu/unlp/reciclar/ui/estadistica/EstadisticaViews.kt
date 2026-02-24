package edu.unlp.reciclar.ui.estadistica

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.unlp.reciclar.data.local.entity.Residuo
import edu.unlp.reciclar.data.local.relation.CanjeConDetalle
import edu.unlp.reciclar.ui.utils.DatePickerComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Claves del mapa de vistas (usadas también en el selector) ─────────────────
internal val estadisticaViewKeys = listOf("Puntos Acumulados", "Tipos de Residuos", "Reciclajes", "Canjes")

// ── Gráfico de barras horizontal compartido ───────────────────────────────────

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

// ── Layout de período ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodoEstadisticaLayout(
    title: String,
    subtitle: String,
    startDateMillis: Long,
    endDateMillis: Long,
    onDateRangeChanged: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val startText = formatter.format(Date(startDateMillis))
    val endText = formatter.format(Date(endDateMillis))

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

        content(Modifier.weight(1f))
    }

    DatePickerComponent(
        showDialog = showStartPicker,
        onDismiss = { showStartPicker = false },
        onDateSelected = { onDateRangeChanged(it, endDateMillis) },
        initialDateMillis = startDateMillis,
        maxDateMillis = endDateMillis
    )
    DatePickerComponent(
        showDialog = showEndPicker,
        onDismiss = { showEndPicker = false },
        onDateSelected = { onDateRangeChanged(startDateMillis, it) },
        initialDateMillis = endDateMillis,
        minDateMillis = startDateMillis
    )
}

// ── Puntos Acumulados ───────────────────────────────────────────────────
@Composable
fun PuntosAcumuladosView(
    puntosStats: PuntosStats,
    startDateMillis: Long,
    endDateMillis: Long,
    onDateRangeChanged: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    PeriodoEstadisticaLayout(
        title = "Puntos Acumulados",
        subtitle = "Balance de puntos en el período seleccionado",
        startDateMillis = startDateMillis,
        endDateMillis = endDateMillis,
        onDateRangeChanged = onDateRangeChanged,
        modifier = modifier
    ) { contentModifier ->
        Column(
            modifier = contentModifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Puntos ganados por reciclajes", style = MaterialTheme.typography.bodyLarge)
                Text("+${puntosStats.puntosGanados} pts", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF008000))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Puntos gastados en canjes", style = MaterialTheme.typography.bodyLarge)
                Text("-${puntosStats.puntosGastados} pts", style = MaterialTheme.typography.bodyLarge, color = Color.Red)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Balance neto del período", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${puntosStats.balanceNeto} pts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ── Tipos de Residuos ─────────────────────────────────────────────────────────

@Composable
fun TiposResiduosView(
    stats: List<BucketStats>,
    startDateMillis: Long,
    endDateMillis: Long,
    onDateRangeChanged: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalEntries = stats.sumOf { it.cantidad }

    PeriodoEstadisticaLayout(
        title = "Residuos por Tipo",
        subtitle = "$totalEntries reciclajes en el período",
        startDateMillis = startDateMillis,
        endDateMillis = endDateMillis,
        onDateRangeChanged = onDateRangeChanged,
        modifier = modifier
    ) { contentModifier ->
        if (stats.isEmpty()) {
            Box(
                modifier = contentModifier.fillMaxWidth(),
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
                modifier = contentModifier,
                labelWidth = 72,
                valueLabel = { "${it.cantidad} (${it.puntos} pts)" },
                valueOf = { it.cantidad }
            )
        }
    }
}

// ── Historial ─────────────────────────────────────────────────────────────────

@Composable
fun ReciclajesHistorialView(
    residuos: List<Residuo>,
    startDateMillis: Long,
    endDateMillis: Long,
    onDateRangeChanged: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalEntries = residuos.size
    val totalPuntos = residuos.sumOf { it.puntos }
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    PeriodoEstadisticaLayout(
        title = "Historial de Reciclajes",
        subtitle = "$totalEntries reciclajes en el período ($totalPuntos pts)",
        startDateMillis = startDateMillis,
        endDateMillis = endDateMillis,
        onDateRangeChanged = onDateRangeChanged,
        modifier = modifier
    ) { contentModifier ->
        if (residuos.isEmpty()) {
            Box(
                modifier = contentModifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin reciclajes en este período",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = contentModifier) {
                items(residuos) { residuo ->
                    val date = formatter.format(Date(residuo.timestamp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date,
                            modifier = Modifier.width(90.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = residuo.tipo,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${residuo.puntos} pts",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// ── Historial de Canjes ───────────────────────────────────────────────────

@Composable
fun CanjesHistorialView(
    canjes: List<CanjeConDetalle>,
    startDateMillis: Long,
    endDateMillis: Long,
    onDateRangeChanged: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalEntries = canjes.size
    val totalPuntos = canjes.sumOf { it.detalle.puntosNecesarios }
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    PeriodoEstadisticaLayout(
        title = "Historial de Canjes",
        subtitle = "$totalEntries canjes en el período ($totalPuntos pts)",
        startDateMillis = startDateMillis,
        endDateMillis = endDateMillis,
        onDateRangeChanged = onDateRangeChanged,
        modifier = modifier
    ) { contentModifier ->
        if (canjes.isEmpty()) {
            Box(
                modifier = contentModifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin canjes en este período",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = contentModifier) {
                items(canjes) { canjeConDetalle ->
                    val date = formatter.format(Date(canjeConDetalle.canje.fechaCanje))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date,
                            modifier = Modifier.width(90.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = canjeConDetalle.detalle.nombre,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${canjeConDetalle.detalle.puntosNecesarios} pts",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
