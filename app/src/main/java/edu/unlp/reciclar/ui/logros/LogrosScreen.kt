package edu.unlp.reciclar.ui.logros

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.R
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
    var showDialog by remember { mutableStateOf(false) }
    var selectedLogro by remember { mutableStateOf<Logro?>(null) }

    if (showDialog) {
        selectedLogro?.let {
            LogroDialog(logro = it) {
                showDialog = false
            }
        }
    }

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
                LogroItem(logroScreen = logroScreen, onLogroClick = {
                    selectedLogro = it
                    showDialog = true
                })
            }
        }
    }
}

@Composable
fun LogroItem(
    logroScreen: LogroScreen,
    modifier: Modifier = Modifier,
    onLogroClick: (Logro) -> Unit
) {
    val alpha = if (logroScreen.obtenido) 1f else 0.4f

    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(
        "logro_${logroScreen.logro.id}",
        "drawable",
        context.packageName
    )
    val finalResId = if (imageResId != 0) imageResId else R.drawable.logro_default

    Card(
        modifier = modifier
            .alpha(alpha)
            .aspectRatio(1f)
            .clickable { onLogroClick(logroScreen.logro) },
        elevation = CardDefaults.cardElevation(defaultElevation = if (logroScreen.obtenido) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = finalResId),
                contentDescription = logroScreen.logro.nombre,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = logroScreen.logro.nombre,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun LogroDialog(logro: Logro, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Logro",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = logro.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = logro.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
