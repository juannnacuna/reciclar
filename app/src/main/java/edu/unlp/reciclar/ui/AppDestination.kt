package edu.unlp.reciclar.ui

/**
 * Rutas tipadas de la aplicación.
 *
 * En Navigation Compose cada destino se identifica por un String (route).
 * Usar una sealed class en lugar de strings sueltos previene typos en tiempo
 * de compilación — si refactorizás una ruta el compilador te avisa en todos
 * los lugares que la usan.
 */
sealed class AppDestination(val route: String) {
    object Login  : AppDestination("login")
    object Signup : AppDestination("signup")
    object ScanQr : AppDestination("scan_qr")
    object Ranking : AppDestination("ranking")
}
