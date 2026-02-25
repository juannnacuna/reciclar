package edu.unlp.reciclar.ui.logros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.data.local.entity.Logro

data class LogroScreen(
    val logro: Logro,
    val obtenido: Boolean
)

@Composable
fun LogrosScreen (
    modifier: Modifier = Modifier,
    viewModel: LogrosViewModel = hiltViewModel()
){
    val logrosState by viewModel.logrosState.collectAsStateWithLifecycle()

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "Mis Logros",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(logrosState) { logroScreen ->
                LogroItem(logroScreen = logroScreen)
            }
        }
    }
}

@Composable
fun LogroItem(
    logroScreen: LogroScreen,
    modifier: Modifier = Modifier
) {
    val alpha = if (logroScreen.obtenido) 1f else 0.4f
    val color = if (logroScreen.obtenido) MaterialTheme.colorScheme.primary else Color.Gray

    Card(
        modifier = modifier
            .alpha(alpha)
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = if (logroScreen.obtenido) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = logroScreen.logro.nombre,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
