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

class SignupFragment : Fragment(R.layout.fragment_signup) {

    private lateinit var authRepository: AuthRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inyección de dependencias manual
        val context = requireContext()
        val sessionManager = SessionManager(context)
        val apiService = ApiClient.getApiService(context)
        authRepository = AuthRepository(apiService, sessionManager)

        val etUsername = view.findViewById<TextInputEditText>(R.id.etSignupUsername)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etSignupPassword)
        val btnSignup = view.findViewById<Button>(R.id.btnSignup)
        val tvStatus = view.findViewById<TextView>(R.id.tvSignupStatus)

        btnSignup.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvStatus.text = "Registrando usuario..."
            btnSignup.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                val result = authRepository.signup(username, password)

                btnSignup.isEnabled = true

                result.onSuccess {
                    Toast.makeText(context, "Registro exitoso. Inicia sesión.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack() // Volver al Login
                }.onFailure { exception ->
                    tvStatus.text = "Error al registrar: ${exception.message}"
                }
            }
        }
    }
}
