package edu.unlp.reciclar.ui.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.domain.model.RankingEntry

/**
 * Pantalla principal del Ranking implementada íntegramente en Compose.
 *
 * Recibe el [viewModel] inyectado por Hilt desde el NavHost.
 *
 * Nota: AppTopBar se renderiza a nivel de Scaffold en MainApp.kt,
 * por lo que esta pantalla no necesita saber sobre username/puntos/logout.
 */
@Composable
fun RankingScreen(
    viewModel: RankingViewModel,
    modifier: Modifier = Modifier
) {
    // collectAsStateWithLifecycle(): convierte un StateFlow en State<T> de Compose.
    // "WithLifecycle" significa que solo colecciona cuando el Composable está activo
    // (similar a repeatOnLifecycle(STARTED) que usábamos antes con corrutinas).
    val ranking by viewModel.ranking.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val residueTypes = listOf("Todos", "Plastico", "Vidrio", "Papel", "Metal", "Carton")

    // remember: almacena un valor a través de recomposiciones.
    // Sin remember, el estado se resetearía cada vez que Compose re-ejecuta esta función.
    var selectedType by remember { mutableStateOf(residueTypes[0]) }

    // LaunchedEffect(key): lanza una corrutina cuando el Composable entra en composición,
    // y la relanza cada vez que [selectedType] cambia. Reemplaza al OnItemSelectedListener
    // del Spinner. Cuando se destruye el Composable, la corrutina se cancela automáticamente.
    LaunchedEffect(selectedType) {
        viewModel.fetchRanking(if (selectedType == "Todos") null else selectedType)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResiduoDropdown(
                options = residueTypes,
                selected = selectedType,
                onSelected = { selectedType = it }
            )
        }

        // Box permite apilar elementos. Usamos Alignment para centrar el indicador
        // de carga y el mensaje de error, mientras la lista ocupa todo el espacio.
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
                ranking.isEmpty() -> Text(
                    text = "No hay datos para mostrar",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> RankingList(entries = ranking)
            }
        }
    }
}

/**
 * Dropdown de tipo de residuo usando el componente ExposedDropdownMenuBox de Material3.
 * Es private porque es un detalle de implementación de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResiduoDropdown(
    options: List<String>,
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
            label = { Text("Tipo de residuo") },
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * LazyColumn: equivalente a RecyclerView. Solo renderiza los items visibles en pantalla.
 * No necesita Adapter ni ViewHolder — cada item es simplemente un @Composable.
 *
 * itemsIndexed provee el índice (0-based) y el item. position = index + 1.
 */
@Composable
private fun RankingList(entries: List<RankingEntry>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(entries) { index, entry ->
            RankingItem(position = index + 1, entry = entry)
        }
    }
}

/**
 * Item individual del ranking. Reemplaza a RankingAdapter.RankingViewHolder + item_ranking.xml.
 * Card aplica elevación y bordes redondeados automáticamente según el tema Material3.
 */
@Composable
private fun RankingItem(position: Int, entry: RankingEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position.",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = entry.username,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${entry.total_puntos} pts",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
