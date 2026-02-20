package edu.unlp.reciclar.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import edu.unlp.reciclar.R
import edu.unlp.reciclar.ui.auth.AuthViewModel

abstract class BaseFragment : Fragment() {

    protected val authViewModel: AuthViewModel by activityViewModels()
    private var logoutActionId: Int = R.id.loginFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // El botón de logout ahora pide al ViewModel que haga el trabajo
        view.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            authViewModel.onLogoutClicked()
        }

        // El observador vive en la clase base, reaccionará en cualquier pantalla
        observeLogout()
    }

    protected fun setupLogoutButton(view: View, actionId: Int) {
        logoutActionId = actionId
    }

    private fun observeLogout() {
        authViewModel.logoutEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    findNavController().navigate(logoutActionId)
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Error al cerrar sesión: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
