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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.data.local.entity.Cupon
import edu.unlp.reciclar.data.local.relation.CanjeConDetalle
import edu.unlp.reciclar.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CuponesScreen(
    modifier: Modifier = Modifier,
    viewModel: CuponesViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel()
) {
    val cuponesState by viewModel.cuponesState.collectAsStateWithLifecycle()
    val misCupones by viewModel.misCupones.collectAsStateWithLifecycle()
    val userState by appViewModel.userState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf<Cupon?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.canjeResult.collect { result ->
            result.onSuccess { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
        // ── Toggle de pestañas ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "Mis Cupones",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Canjear",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Contenido según pestaña ──
        when (selectedTab) {
            0 -> MisCuponesTab(misCupones)
            1 -> CanjearCuponesTab(
                cupones = cuponesState,
                puntosDisponibles = userState?.puntosDisponibles ?: 0,
                onCanjearClicked = { showDialog = it }
            )
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

// ──────────────────────────────────────────
// Pestañas
// ──────────────────────────────────────────

@Composable
private fun MisCuponesTab(misCupones: List<CanjeConDetalle>) {
    if (misCupones.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Todavía no canjeaste ningún cupón",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(misCupones) { canjeConDetalle ->
                MiCuponItem(canjeConDetalle)
            }
        }
    }
}

@Composable
private fun CanjearCuponesTab(
    cupones: List<Cupon>,
    puntosDisponibles: Int,
    onCanjearClicked: (Cupon) -> Unit
) {
    if (cupones.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No hay cupones disponibles para canjear",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cupones) { cupon ->
                CuponItem(
                    cupon = cupon,
                    enabled = puntosDisponibles >= cupon.puntosNecesarios,
                    onCanjearClicked = { onCanjearClicked(cupon) }
                )
            }
        }
    }
}

// ──────────────────────────────────────────
// Componentes
// ──────────────────────────────────────────

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text = text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun MiCuponItem(canjeConDetalle: CanjeConDetalle) {
    val cupon = canjeConDetalle.detalle
    val canje = canjeConDetalle.canje
    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(canje.fechaCanje))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cupon.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (canje.fueUsado) "Usado" else "Disponible",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canje.fueUsado)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = cupon.descripcion,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Canjeado el $fechaFormateada",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                fontWeight = FontWeight.SemiBold,
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
            TextButton(onClick = onConfirm) {
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
