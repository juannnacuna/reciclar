package edu.unlp.reciclar.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.source.ApiClient
import edu.unlp.reciclar.data.model.ReclamarResiduoRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanQrFragment : Fragment(R.layout.fragment_scan_qr) {

    private lateinit var tvScanResult: TextView
    private lateinit var btnClaimPoints: Button
    private var scannedResiduoId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvScanResult = view.findViewById(R.id.tvScanResult)
        val btnScanQr = view.findViewById<Button>(R.id.btnScanQr)
        btnClaimPoints = view.findViewById<Button>(R.id.btnClaimPoints)

        // Configurar el scanner de Google Play Services
        val scanner = GmsBarcodeScanning.getClient(requireContext())

        btnScanQr.setOnClickListener {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    // Éxito: Obtenemos el valor raw del QR
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        scannedResiduoId = rawValue
                        tvScanResult.text = "Código detectado: $rawValue"
                        btnClaimPoints.visibility = View.VISIBLE
                        Toast.makeText(context, "QR Escaneado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        tvScanResult.text = "Error: El código QR está vacío"
                    }
                }
                .addOnCanceledListener {
                    // El usuario canceló
                    Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
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
                val apiService = ApiClient.getApiService(requireContext())
                val response = apiService.reclamarResiduo(ReclamarResiduoRequest(idResiduo))

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        btnClaimPoints.isEnabled = true
                        if (response.isSuccessful) {
                            tvScanResult.text = "¡Éxito! Puntos sumados a tu cuenta."
                            btnClaimPoints.visibility = View.GONE
                            scannedResiduoId = null // Reset
                        } else {
                            tvScanResult.text = "Error al reclamar: ${response.code()}"
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        btnClaimPoints.isEnabled = true
                        tvScanResult.text = "Error de conexión: ${e.message}"
                    }
                }
            }
        }
    }
}
