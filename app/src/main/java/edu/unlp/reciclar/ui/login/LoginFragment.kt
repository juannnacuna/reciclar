package edu.unlp.reciclar.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.repository.AuthRepository
import edu.unlp.reciclar.data.source.ApiClient
import edu.unlp.reciclar.data.source.SessionManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var viewModel: LoginViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar dependencias y ViewModel
        val context = requireContext()
        val sessionManager = SessionManager(context)
        val apiService = ApiClient.getApiService(context)
        val authRepository = AuthRepository(apiService, sessionManager)
        val factory = LoginViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        val etUsername = view.findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvGoToSignup = view.findViewById<TextView>(R.id.tvGoToSignup)

        // Verificar sesión existente
        if (viewModel.isLoggedIn()) {
            findNavController().navigate(R.id.action_loginFragment_to_scanQrFragment)
            return
        }

        tvGoToSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        btnLogin.setOnClickListener {
            viewModel.login(
                etUsername.text.toString(),
                etPassword.text.toString()
            )
        }

        // Observar estado del ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginViewModel.LoginState.Idle -> {
                            btnLogin.isEnabled = true
                            tvStatus.text = ""
                        }
                        is LoginViewModel.LoginState.Loading -> {
                            btnLogin.isEnabled = false
                            tvStatus.text = "Iniciando sesión..."
                        }
                        is LoginViewModel.LoginState.Success -> {
                            btnLogin.isEnabled = true
                            Toast.makeText(context, "Bienvenido ${state.username}", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_loginFragment_to_scanQrFragment)
                            viewModel.resetState() // Resetear estado para evitar navegación doble al volver
                        }
                        is LoginViewModel.LoginState.Error -> {
                            btnLogin.isEnabled = true
                            tvStatus.text = "Error: ${state.message}"
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
