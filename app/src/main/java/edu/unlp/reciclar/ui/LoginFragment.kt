package edu.unlp.reciclar.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.source.ApiClient
import edu.unlp.reciclar.data.source.SessionManager
import edu.unlp.reciclar.data.model.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val etUsername = view.findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvGoToSignup = view.findViewById<TextView>(R.id.tvGoToSignup)

        // Verificar sesión existente
        if (sessionManager.getAccessToken() != null) {
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

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // MAL. NO HAY INYECCION DE DEPENDENCIAS
                    // LA VISTA NO PUEDE USAR LA API. DEBERIA SER ASI:
                    // Fragment -> ViewModel -> Repositorio -> ApiService
                    val apiService = ApiClient.getApiService(requireContext())
                    val response = apiService.login(LoginRequest(username, password))

                    withContext(Dispatchers.Main) {
                        btnLogin.isEnabled = true
                        if (response.isSuccessful && response.body() != null) {
                            val tokens = response.body()!!
                            sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                            Toast.makeText(context, "Bienvenido $username", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_loginFragment_to_scanQrFragment)
                        } else {
                            tvStatus.text = "Error: ${response.code()} - ${response.message()}"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) { // Verificar si el fragmento sigue activo
                            btnLogin.isEnabled = true
                            tvStatus.text = "Error de conexión: ${e.message}"
                        }
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
