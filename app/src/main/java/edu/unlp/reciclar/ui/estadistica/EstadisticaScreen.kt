package edu.unlp.reciclar.ui.estadistica

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.ui.components.AppTopBar

private val estadisticaViewNames: List<String> = estadisticaViewKeys.sorted()

/**
 * Pantalla principal de Estadística.
 *
 * Para agregar una nueva vista:
 * 1. Crea la función Composable en EstadisticaViews.kt
 * 2. Añade la clave en [estadisticaViewKeys]
 * 3. Agrégala al mapa viewsMap de esta función
 */
@Composable
fun EstadisticaScreen(
    modifier: Modifier = Modifier,
    username: String? = null,
    puntosDisponibles: Int? = null,
    onLogout: () -> Unit = {},
    viewModel: EstadisticaViewModel = hiltViewModel()
) {
    val tiposStats by viewModel.tiposStats.collectAsStateWithLifecycle()
    val reciclajesHistorial by viewModel.reciclajesHistorial.collectAsStateWithLifecycle()
    val canjesHistorial by viewModel.canjesHistorial.collectAsStateWithLifecycle()
    val puntosStats by viewModel.puntosStats.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()

    // Mapa local para poder capturar el estado del ViewModel en cada composable
    val viewsMap: Map<String, @Composable () -> Unit> = mapOf(
        "Puntos Acumulados" to {
            PuntosAcumuladosView(
                puntosStats = puntosStats,
                startDateMillis = startDate,
                endDateMillis = endDate,
                onDateRangeChanged = { s, e -> viewModel.updateDateRange(s, e) }
            )
        },
        "Tipos de Residuos" to {
            TiposResiduosView(
                stats = tiposStats,
                startDateMillis = startDate,
                endDateMillis = endDate,
                onDateRangeChanged = { s, e -> viewModel.updateDateRange(s, e) }
            )
        },
        "Reciclajes" to {
            ReciclajesHistorialView(
                residuos = reciclajesHistorial,
                startDateMillis = startDate,
                endDateMillis = endDate,
                onDateRangeChanged = { s, e -> viewModel.updateDateRange(s, e) }
            )
        },
        "Canjes" to {
            CanjesHistorialView(
                canjes = canjesHistorial,
                startDateMillis = startDate,
                endDateMillis = endDate,
                onDateRangeChanged = { s, e -> viewModel.updateDateRange(s, e) }
            )
        }
    )

    var selectedViewName by remember { mutableStateOf(estadisticaViewNames.firstOrNull() ?: "") }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AppTopBar(
            username = username,
            puntosDisponibles = puntosDisponibles,
            onLogout = onLogout
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EstadisticaViewSelector(
                selected = selectedViewName,
                onSelected = { selectedViewName = it }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val viewComposable = viewsMap[selectedViewName]
            if (viewComposable != null) {
                viewComposable()
            }
        }
    }
}

/**
 * Selector de vista usando ExposedDropdownMenuBox.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstadisticaViewSelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Vista de Estadística") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .width(200.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            estadisticaViewNames.forEach { viewName ->
                DropdownMenuItem(
                    text = { Text(viewName) },
                    onClick = {
                        onSelected(viewName)
                        expanded = false
                    }
                )
            }
        }
    }
}
