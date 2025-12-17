package edu.unlp.reciclar.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.source.ApiClient
import edu.unlp.reciclar.data.source.SessionManager
import edu.unlp.reciclar.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var authRepository: AuthRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inyección de dependencias manual (Idealmente esto vendría de un ViewModelFactory)
        val context = requireContext()
        val sessionManager = SessionManager(context)
        val apiService = ApiClient.getApiService(context)
        authRepository = AuthRepository(apiService, sessionManager)

        val etUsername = view.findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvGoToSignup = view.findViewById<TextView>(R.id.tvGoToSignup)

        // Verificar sesión existente usando el repositorio
        if (authRepository.isLoggedIn()) {
            findNavController().navigate(R.id.action_loginFragment_to_scanQrFragment)
            return
        }

        tvGoToSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvStatus.text = "Iniciando sesión..."
            btnLogin.isEnabled = false

            // Usamos lifecycleScope para corrutinas ligadas al ciclo de vida del fragmento
            viewLifecycleOwner.lifecycleScope.launch {
                val result = authRepository.login(username, password)

                btnLogin.isEnabled = true // Reactivar botón siempre

                result.onSuccess {
                    Toast.makeText(context, "Bienvenido $username", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_loginFragment_to_scanQrFragment)
                }.onFailure { exception ->
                    tvStatus.text = "Error: ${exception.message}"
                }
            }
        }
    }
}
