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
import edu.unlp.reciclar.data.model.SignupRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupFragment : Fragment(R.layout.fragment_signup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiService = ApiClient.getApiService(requireContext())
                    val response = apiService.signup(SignupRequest(username, password))

                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            btnSignup.isEnabled = true
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Registro exitoso. Inicia sesión.", Toast.LENGTH_LONG).show()
                                findNavController().popBackStack() // Volver al Login
                            } else {
                                tvStatus.text = "Error al registrar: ${response.code()} - ${response.message()}"
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            btnSignup.isEnabled = true
                            tvStatus.text = "Error de conexión: ${e.message}"
                        }
                    }
                }
            }
        }
    }
}
