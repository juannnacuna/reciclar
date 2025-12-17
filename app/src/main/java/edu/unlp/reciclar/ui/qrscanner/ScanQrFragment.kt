package edu.unlp.reciclar.ui.qrscanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.repository.ResiduosRepository
import edu.unlp.reciclar.data.source.ApiClient
import edu.unlp.reciclar.ui.BaseFragment
import kotlinx.coroutines.launch

class ScanQrFragment : BaseFragment() {

    private lateinit var tvScanResult: TextView
    private lateinit var btnClaimPoints: Button
    private lateinit var btnScanQr: Button
    private lateinit var viewModel: ScanQrViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del botón de logout (heredado de BaseFragment)
        setupLogoutButton(view, R.id.action_scanQrFragment_to_loginFragment)

        tvScanResult = view.findViewById(R.id.tvScanResult)
        btnScanQr = view.findViewById(R.id.btnScanQr)
        btnClaimPoints = view.findViewById(R.id.btnClaimPoints)

        // Inicialización del ViewModel usando la Factory
        val apiService = ApiClient.getApiService(requireContext())
        val repository = ResiduosRepository(apiService)
        val factory = ScanQrViewModelFactory(repository)
        
        viewModel = ViewModelProvider(this, factory)[ScanQrViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.statusMessage.collect { message ->
                        tvScanResult.text = message
                    }
                }
                launch {
                    viewModel.isClaimButtonVisible.collect { isVisible ->
                        btnClaimPoints.visibility = if (isVisible) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        btnClaimPoints.isEnabled = !isLoading
                        btnScanQr.isEnabled = !isLoading
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        val scanner = GmsBarcodeScanning.getClient(requireContext())

        btnScanQr.setOnClickListener {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        viewModel.onQrScanned(rawValue)
                    } else {
                        viewModel.onScanError("Error: El código QR está vacío")
                    }
                }
                .addOnCanceledListener {
                    Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    viewModel.onScanError("Error al iniciar escáner: ${e.message}")
                }
        }

        btnClaimPoints.setOnClickListener {
            viewModel.reclamarPuntos()
        }
    }
}
