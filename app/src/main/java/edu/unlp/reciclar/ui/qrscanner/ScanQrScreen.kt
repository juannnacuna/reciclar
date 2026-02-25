package edu.unlp.reciclar.ui.qrscanner

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.ui.utils.ReporteModal
import java.io.File

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
 * a través de un modal dedicado. Tras escanear se muestra la información del QR
 * para que el usuario pueda decidir si debe reportarlo o reclamar los puntos.
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
    val showReporteModal by viewModel.showReporteModal.collectAsStateWithLifecycle()
    val tipoSugerido by viewModel.tipoSugerido.collectAsStateWithLifecycle()
    val qrData by viewModel.qrData.collectAsStateWithLifecycle()
    val photoPath by viewModel.photoPath.collectAsStateWithLifecycle()

    // ── Cámara ────────────────────────────────────────────────────────────────
    val context = LocalContext.current
    // File creado justo antes de lanzar la cámara, para recuperar su path tras la captura.
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    // Flag que activa LaunchedEffect para lanzar la cámara FUERA del árbol del AlertDialog.
    // Necesario porque llamar launch() directamente desde el onClick de un AlertDialog
    // en Compose es poco fiable: el dialog está en un subtree de composición separado.
    var pendingLaunchCamera by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoFile?.absolutePath?.let { viewModel.setPhotoPath(it) }
        }
    }

    // Launcher para solicitar permiso de cámara en tiempo de ejecución.
    // Necesario porque el manifiesto declara CAMERA y algunos dispositivos
    // exigen el grant explícito antes de resolver ACTION_IMAGE_CAPTURE.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingLaunchCamera = true
        }
    }

    // Se ejecuta en el ámbito del Screen (fuera del dialog) cuando el flag se activa.
    LaunchedEffect(pendingLaunchCamera) {
        if (pendingLaunchCamera) {
            pendingLaunchCamera = false
            try {
                val photoFile = File.createTempFile(
                    "reporte_${System.currentTimeMillis()}",
                    ".jpg",
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                )
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                pendingPhotoFile = photoFile
                takePictureLauncher.launch(uri)
            } catch (_: Exception) { /* no-op */ }
        }
    }

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

            // ── Información del QR escaneado ──────────────────────────────────
            // Se muestra mientras hay un QR activo, para que el usuario pueda
            // evaluar si corresponde reclamar puntos o reportar el residuo.
            if (qrData != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Información del QR",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                        QrInfoRow(label = "Tipo", value = qrData!!.tipo)
                        QrInfoRow(label = "Puntos", value = "${qrData!!.puntos}")
                    }
                }
            }

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
        onSubmit = { tipo: String -> viewModel.enviarReporte(tipo) },
        tipoSugerido = tipoSugerido,
        onTipoSugridoChange = { valor: String -> viewModel.actualizarTipoSugerido(valor) },
        isLoading = isLoading,
        photoPath = photoPath,
        onTakePhoto = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pendingLaunchCamera = true
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    )
}

// ── Helper composable ─────────────────────────────────────────────────────────

@Composable
private fun QrInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}


