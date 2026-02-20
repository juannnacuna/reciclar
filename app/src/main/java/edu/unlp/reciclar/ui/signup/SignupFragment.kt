package edu.unlp.reciclar.ui.signup

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import edu.unlp.reciclar.R
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_signup) {

    private val viewModel: SignupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsername = view.findViewById<TextInputEditText>(R.id.etSignupUsername)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etSignupPassword)
        val btnSignup = view.findViewById<Button>(R.id.btnSignup)
        val tvStatus = view.findViewById<TextView>(R.id.tvSignupStatus)

        btnSignup.setOnClickListener {
            viewModel.signup(
                etUsername.text.toString(),
                etPassword.text.toString()
            )
        }

        // Observar estado del ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signupState.collect { state ->
                    when (state) {
                        is SignupViewModel.SignupState.Idle -> {
                            btnSignup.isEnabled = true
                            tvStatus.text = ""
                        }
                        is SignupViewModel.SignupState.Loading -> {
                            btnSignup.isEnabled = false
                            tvStatus.text = "Registrando usuario..."
                        }
                        is SignupViewModel.SignupState.Success -> {
                            btnSignup.isEnabled = true
                            Toast.makeText(context, "Registro exitoso. Inicia sesión.", Toast.LENGTH_LONG).show()
                            findNavController().popBackStack() // Volver al Login
                            viewModel.resetState()
                        }
                        is SignupViewModel.SignupState.Error -> {
                            btnSignup.isEnabled = true
                            tvStatus.text = "Error al registrar: ${state.message}"
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
