package edu.unlp.reciclar.ui.estadistica

import androidx.compose.foundation.layout.Arrangement
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
import edu.unlp.reciclar.ui.components.LogoutButton

/**
 * Lista de vistas de estadística ordenadas alfabéticamente.
 */
private val estadisticaViewNames: List<String> by lazy {
    estadisticaViewsMap.keys.sorted()
}

/**
 * Pantalla principal de Estadística.
 *
 * Para agregar una nueva vista:
 * 1. Crea una función Composable en EstadisticaViews.kt
 * 2. Agrégala al mapa estadisticaViewsMap
 */
@Composable
fun EstadisticaScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    var selectedViewName by remember { mutableStateOf(estadisticaViewNames.firstOrNull() ?: "") }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EstadisticaViewSelector(
                selected = selectedViewName,
                onSelected = { selectedViewName = it }
            )
            LogoutButton(onLogout = onLogout)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val viewComposable = estadisticaViewsMap[selectedViewName]
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
