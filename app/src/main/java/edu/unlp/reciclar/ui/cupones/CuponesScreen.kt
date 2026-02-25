package edu.unlp.reciclar.ui.cupones

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.data.local.entity.Cupon
import edu.unlp.reciclar.ui.AppViewModel

@Composable
fun CuponesScreen(
    modifier: Modifier = Modifier,
    viewModel: CuponesViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel()
) {
    val cuponesState by viewModel.cuponesState.collectAsStateWithLifecycle()
    val userState by appViewModel.userState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf<Cupon?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.canjeResult.collect { result ->
            result.onSuccess { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                appViewModel.loadUser()
            }.onFailure { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Canjear Cupones",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cuponesState) { cupon ->
                CuponItem(
                    cupon = cupon,
                    enabled = (userState?.puntosDisponibles ?: 0) >= cupon.puntosNecesarios,
                    onCanjearClicked = { showDialog = cupon }
                )
            }
        }
    }

    showDialog?.let {
        ConfirmacionCanjeDialog(
            cupon = it,
            onConfirm = {
                viewModel.canjearCupon(it)
                showDialog = null
            },
            onDismiss = { showDialog = null }
        )
    }
}

@Composable
private fun CuponItem(
    cupon: Cupon,
    enabled: Boolean,
    onCanjearClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = cupon.nombre,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = cupon.descripcion,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${cupon.puntosNecesarios} Puntos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = onCanjearClicked,
                    enabled = enabled
                ) {
                    Text("Canjear")
                }
            }
        }
    }
}

@Composable
private fun ConfirmacionCanjeDialog(
    cupon: Cupon,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Canje") },
        text = { Text("¿Estás seguro de que quieres canjear el cupón '${cupon.descripcion}' por ${cupon.puntosNecesarios} puntos?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
