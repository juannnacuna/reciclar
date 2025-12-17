package edu.unlp.reciclar.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.repository.AuthRepository
import edu.unlp.reciclar.data.source.ApiClient
import edu.unlp.reciclar.data.source.SessionManager
import kotlinx.coroutines.launch

abstract class BaseFragment : Fragment() {

    private lateinit var authRepository: AuthRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // La inicialización del repositorio ahora vive aquí, en un solo lugar.
        val apiService = ApiClient.getApiService(requireContext())
        val sessionManager = SessionManager(requireContext())
        authRepository = AuthRepository(apiService, sessionManager)
    }

    /**
     * Configura la lógica del botón de cierre de sesión.
     * Debe ser llamado desde el onViewCreated de las subclases.
     * @param view El View del fragmento, necesario para encontrar el botón.
     * @param navActionId El ID de la acción de navegación a ejecutar al cerrar sesión.
     */
    protected fun setupLogoutButton(view: View, @IdRes navActionId: Int) {
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            lifecycleScope.launch {
                val result = authRepository.logout()
                result.onSuccess {
                    if (isAdded) { // Buena práctica: asegurarse de que el fragmento sigue activo
                        findNavController().navigate(navActionId)
                    }
                }.onFailure {
                    if (isAdded) {
                        Toast.makeText(context, "Error al cerrar sesión: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
