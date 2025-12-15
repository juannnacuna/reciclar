package edu.unlp.reciclar.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.network.ApiClient
import edu.unlp.reciclar.data.network.model.SignupRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etUsername = findViewById<TextInputEditText>(R.id.etSignupUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etSignupPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val tvStatus = findViewById<TextView>(R.id.tvSignupStatus)

        btnSignup.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvStatus.text = "Registrando usuario..."
            btnSignup.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiService = ApiClient.getApiService(this@SignupActivity)
                    val response = apiService.signup(SignupRequest(username, password))

                    withContext(Dispatchers.Main) {
                        btnSignup.isEnabled = true
                        if (response.isSuccessful) {
                            Toast.makeText(this@SignupActivity, "Registro exitoso. Inicia sesión.", Toast.LENGTH_LONG).show()
                            finish() // Vuelve al Login
                        } else {
                            tvStatus.text = "Error al registrar: ${response.code()} - ${response.message()}"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnSignup.isEnabled = true
                        tvStatus.text = "Error de conexión: ${e.message}"
                    }
                }
            }
        }
    }
}
