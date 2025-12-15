package edu.unlp.reciclar.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.network.ApiClient
import edu.unlp.reciclar.data.network.SessionManager
import edu.unlp.reciclar.data.network.model.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        
        // Configurar UI
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvGoToSignup = findViewById<TextView>(R.id.tvGoToSignup)

        // Verificar si ya hay sesión y navegar directamente
        if (sessionManager.getAccessToken() != null) {
            tvStatus.text = "Usuario ya logueado (Token existente)"
            navigateToScanQr()
        }
        
        // Navegación a pantalla de registro
        tvGoToSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvStatus.text = "Iniciando sesión..."
            btnLogin.isEnabled = false

            // Llamada asíncrona (esto debería ir en un ViewModel)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiService = ApiClient.getApiService(this@MainActivity)
                    
                    val response = apiService.login(LoginRequest(username, password))

                    withContext(Dispatchers.Main) {
                        btnLogin.isEnabled = true
                        if (response.isSuccessful && response.body() != null) {
                            val tokens = response.body()!!
                            sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                            tvStatus.text = "¡Login Exitoso!"
                            Toast.makeText(this@MainActivity, "Bienvenido $username", Toast.LENGTH_LONG).show()
                            
                            // Navegar a la pantalla de ScanQR
                            navigateToScanQr()
                        } else {
                            tvStatus.text = "Error: ${response.code()} - ${response.message()}"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnLogin.isEnabled = true
                        tvStatus.text = "Error de conexión: ${e.message}"
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun navigateToScanQr() {
        val intent = Intent(this, ScanQrActivity::class.java)
        startActivity(intent)
        finish() // Cierra la pantalla de Login para que no se pueda volver atrás
    }
}
