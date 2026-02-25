package edu.unlp.reciclar.ui.qrscanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.ui.utils.ReporteModal

/**
 * Pantalla de escaneo QR implementada en Compose.
 *
 * [onStartScan]: lambda que lanza el scanner de GmsBarcodeScanning.
 *   Se implementa en el Fragment, que tiene el Context de ciclo de vida correcto.
 *   El Composable no sabe cómo funciona el scanner — solo sabe que hay que
 *   llamar a este lambda cuando el usuario toca el botón. Esto se llama
 *   "inversión de control" y es la forma idiomática de integrar APIs
 *   imperativas de Android con Compose.
 *
 * onClaimPoints: lambda que llama a viewModel.reclamarPuntos().
 *   También se podría llamar directamente desde el Screen, pero separarlo
 *   mantiene la pantalla desacoplada del ViewModel si fuera necesario testearla.
 * El Screen permite que el usuario escanee QR, reclame puntos, o reporte residuos
 * a través de un modal dedicado.
 */
@Composable
fun ScanQrScreen(
    viewModel: ScanQrViewModel,
    onStartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isClaimButtonVisible by viewModel.isClaimButtonVisible.collectAsStateWithLifecycle()
    val isReportButtonVisible by viewModel.isReportButtonVisible.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val showReporteModal by viewModel.showReporteModal.collectAsStateWithLifecycle()
    val tipoSugerido by viewModel.tipoSugerido.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Escaneo de Puntos de Reciclaje",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Icon de Material — reemplaza el ImageView con ic_menu_camera.
            // Material Icons incluye iconos vectoriales listos, sin necesidad de
            // archivos drawable separados.
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Icono QR",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = statusMessage,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            Button(
                onClick = onStartScan,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    // El indicador de carga reemplaza el texto del botón
                    // mientras se procesa la solicitud — sin visibility = GONE/VISIBLE.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Text("Procesando...")
                    }
                } else {
                    Text("Escanear QR")
                }
            }

            // El botón "Reclamar Puntos" solo se emite al árbol de Compose
            // cuando isClaimButtonVisible == true. No existe en el árbol cuando
            // es false — no hay visibility = GONE, simplemente no se declara.
            if (isClaimButtonVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.reclamarPuntos() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reclamar Puntos")
                }
            }

            if (isReportButtonVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.reportarResiduo() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reportar Residuo")
                }
            }
        }
    }

    // Modal para reportar residuo
    ReporteModal(
        showDialog = showReporteModal,
        onDismiss = { viewModel.cerrarReporteModal() },
        onSubmit = { tipo -> viewModel.enviarReporte(tipo) },
        tipoSugerido = tipoSugerido,
        onTipoSugridoChange = { viewModel.actualizarTipoSugerido(it) },
        isLoading = isLoading
    )
}
