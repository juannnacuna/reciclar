package edu.unlp.reciclar.ui.cupones

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import edu.unlp.reciclar.data.local.relation.CanjeConDetalle
import org.json.JSONObject

/**
 * Genera el contenido JSON del QR para un cupón canjeado.
 *
 * Campos incluidos:
 * - app: identificador de la aplicación ("reciclar")
 * - usuarioId: ID del usuario dueño del cupón
 * - cuponId: ID del cupón canjeado
 * - cuponNombre: nombre legible del cupón
 * - fechaCanje: timestamp epoch (ms) de cuando se canjeó
 * - generadoEn: timestamp epoch (ms) de cuando se generó el QR (frescura / anti-replay)
 */
private fun buildQrContent(canjeConDetalle: CanjeConDetalle): String {
    val json = JSONObject().apply {
        put("app", "reciclar")
        put("usuarioId", canjeConDetalle.canje.usuarioId)
        put("cuponId", canjeConDetalle.canje.cuponId)
        put("cuponNombre", canjeConDetalle.detalle.nombre)
        put("fechaCanje", canjeConDetalle.canje.fechaCanje)
        put("generadoEn", System.currentTimeMillis())
    }
    return json.toString()
}

/**
 * Genera un Bitmap con el código QR a partir de un texto.
 */
private fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

/**
 * Diálogo que muestra el QR de un cupón canjeado para que pueda ser escaneado
 * por un comercio y validar su consumo.
 */
@Composable
fun CuponQrDialog(
    canjeConDetalle: CanjeConDetalle,
    onDismiss: () -> Unit
) {
    val qrContent = remember(canjeConDetalle) { buildQrContent(canjeConDetalle) }
    val qrBitmap = remember(qrContent) { generateQrBitmap(qrContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Usar Cupón",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = canjeConDetalle.detalle.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mostrá este código QR en el comercio para validar tu cupón.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Código QR del cupón ${canjeConDetalle.detalle.nombre}",
                    modifier = Modifier.size(250.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
