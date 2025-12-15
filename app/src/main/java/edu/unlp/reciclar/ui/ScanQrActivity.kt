package edu.unlp.reciclar.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.network.ApiClient
import edu.unlp.reciclar.data.network.model.ReclamarResiduoRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanQrActivity : AppCompatActivity() {

    private lateinit var tvScanResult: TextView
    private lateinit var btnClaimPoints: Button
    private var scannedResiduoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr)

        tvScanResult = findViewById(R.id.tvScanResult)
        val btnScanQr = findViewById<Button>(R.id.btnScanQr)
        btnClaimPoints = findViewById(R.id.btnClaimPoints)

        // Configurar el scanner de Google Play Services
        val scanner = GmsBarcodeScanning.getClient(this)

        btnScanQr.setOnClickListener {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    // Éxito: Obtenemos el valor raw del QR
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        scannedResiduoId = rawValue
                        tvScanResult.text = "Código detectado: $rawValue"
                        btnClaimPoints.visibility = View.VISIBLE
                        Toast.makeText(this, "QR Escaneado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        tvScanResult.text = "Error: El código QR está vacío"
                    }
                }
                .addOnCanceledListener {
                    // El usuario canceló
                    Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Error general
                    tvScanResult.text = "Error al iniciar escáner: ${e.message}"
                }
        }

        btnClaimPoints.setOnClickListener {
            scannedResiduoId?.let { id ->
                reclamarPuntos(id)
            }
        }
    }

    private fun reclamarPuntos(idResiduo: String) {
        tvScanResult.text = "Reclamando puntos..."
        btnClaimPoints.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.getApiService(this@ScanQrActivity)
                val response = apiService.reclamarResiduo(ReclamarResiduoRequest(idResiduo))

                withContext(Dispatchers.Main) {
                    btnClaimPoints.isEnabled = true
                    if (response.isSuccessful) {
                        tvScanResult.text = "¡Éxito! Puntos sumados a tu cuenta."
                        btnClaimPoints.visibility = View.GONE
                        scannedResiduoId = null // Reset
                    } else {
                        tvScanResult.text = "Error al reclamar: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnClaimPoints.isEnabled = true
                    tvScanResult.text = "Error de conexión: ${e.message}"
                }
            }
        }
    }
}
