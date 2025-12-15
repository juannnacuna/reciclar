package edu.unlp.reciclar

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
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

        // Verificar si ya hay sesión
        if (sessionManager.getAccessToken() != null) {
            tvStatus.text = "Usuario ya logueado (Token existente)"
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
                    // Usamos ApiClient temporalmente (Singleton manual) o instanciamos aquí
                    // Como el Singleton ApiClient fue rechazado, lo crearé localmente para este ejemplo
                    val apiService = ApiClient.getApiService(this@MainActivity)
                    
                    val response = apiService.login(LoginRequest(username, password))

                    withContext(Dispatchers.Main) {
                        btnLogin.isEnabled = true
                        if (response.isSuccessful && response.body() != null) {
                            val tokens = response.body()!!
                            sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                            tvStatus.text = "¡Login Exitoso!"
                            Toast.makeText(this@MainActivity, "Bienvenido $username", Toast.LENGTH_LONG).show()
                            // Aquí navegarías a la siguiente pantalla
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
}
