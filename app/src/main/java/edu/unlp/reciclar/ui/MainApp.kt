package edu.unlp.reciclar.ui

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import edu.unlp.reciclar.ui.login.LoginScreen
import edu.unlp.reciclar.ui.login.LoginViewModel
import edu.unlp.reciclar.ui.qrscanner.ScanQrScreen
import edu.unlp.reciclar.ui.qrscanner.ScanQrViewModel
import edu.unlp.reciclar.ui.ranking.RankingScreen
import edu.unlp.reciclar.ui.ranking.RankingViewModel
import edu.unlp.reciclar.ui.signup.SignupScreen
import edu.unlp.reciclar.ui.signup.SignupViewModel
import edu.unlp.reciclar.ui.estadistica.EstadisticaScreen
import edu.unlp.reciclar.ui.maps.RecyclingMapScreen
import edu.unlp.reciclar.ui.maps.RecyclingMapViewModel
import edu.unlp.reciclar.ui.components.AppTopBar
import edu.unlp.reciclar.ui.cupones.CuponesScreen
import edu.unlp.reciclar.ui.cupones.CuponesViewModel
import edu.unlp.reciclar.ui.estadistica.EstadisticaViewModel
import edu.unlp.reciclar.ui.logros.LogrosScreen
import edu.unlp.reciclar.ui.logros.LogrosViewModel
import edu.unlp.reciclar.ui.trivia.TriviaScreen
import edu.unlp.reciclar.ui.trivia.TriviaViewModel

/**
 * Datos de cada pestaña del bottom nav.
 */
private data class BottomNavItem(
    val destination: AppDestination,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(AppDestination.ScanQr, Icons.Default.QrCodeScanner, ""),
    BottomNavItem(AppDestination.Ranking, Icons.Default.Leaderboard, ""),
    BottomNavItem(AppDestination.Estadistica, Icons.Default.Functions, ""),
    BottomNavItem(AppDestination.Map, Icons.Default.Place, ""),
    BottomNavItem(AppDestination.Cupones, Icons.Default.AttachMoney, ""),
    BottomNavItem(AppDestination.Logros, Icons.Default.Done, ""),
    BottomNavItem(AppDestination.Trivia, Icons.Default.Eco, "")
)

/**
 * Composable raíz de la aplicación.
 *
 * Conforma la vista base. Contiene:
 * - NavHost
 *   Cada `composable("ruta") { ... }` es un destino.
 * - NavigationBar
 * - Observador de logout.
 */
@Composable
fun MainApp() {
    // hiltViewModel() en el nivel de MainApp: AppViewModel dura mientras
    // MainActivity esté viva — equivalente a activityViewModels() en Fragments.
    val appViewModel: AppViewModel = hiltViewModel()
    val navController = rememberNavController()
    val context = LocalContext.current

    val userState by appViewModel.userState.collectAsStateWithLifecycle()

    // currentBackStackEntryAsState(): State<NavBackStackEntry?> que se actualiza
    // en cada navegación. Lo usamos para saber qué destino está activo y así
    // mostrar la NavigationBar solo en las pantallas correctas.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBars = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.destination.route } == true
    }

    // Observador de logout centralizado.
    // LaunchedEffect(Unit): corre indefinidamente mientras MainApp esté en composición,
    // coleccionando el Flow de eventos de logout del Channel del AppViewModel.
    // Al recibir un evento Success, navega a Login limpiando toda la back stack
    // (popUpTo(0) inclusive = true).
    LaunchedEffect(Unit) {
        appViewModel.logoutEvent.collect { result ->
            result.onSuccess {
                navController.navigate(AppDestination.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Error al cerrar sesión: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                if (showBars) {
                    AppTopBar(
                        username = userState?.username,
                        puntosDisponibles = userState?.puntosDisponibles,
                        onLogout = { appViewModel.onLogoutClicked() }
                    )
                }
            },
            bottomBar = {
                if (showBars) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == item.destination.route } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    // Patrón estándar para bottom nav en Compose:
                                    // - popUpTo(startDestination) limpia la pila hasta la
                                    //   raíz del grafo al cambiar pestaña.
                                    // - saveState/restoreState preserva el estado de scroll
                                    //   y datos de cada pestaña al volver a ella.
                                    // - launchSingleTop evita duplicar el destino si ya
                                    //   estás en esa pestaña.
                                    navController.navigate(item.destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            // Cada bloque composable { } es un destino.
            // hiltViewModel() inyecta ViewModels con Hilt sin necesidad de Fragments.
            NavHost(
                navController = navController,
                startDestination = AppDestination.Login.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(AppDestination.Login.route) {
                    val viewModel: LoginViewModel = hiltViewModel()
                    LoginScreen(
                        viewModel = viewModel,
                        isLoggedIn = { appViewModel.isLoggedIn() },
                        onLoginSuccess = {
                            Toast.makeText(context, "Bienvenido", Toast.LENGTH_LONG).show()
                            appViewModel.loadUser()
                            navController.navigate(AppDestination.ScanQr.route) {
                                // Equivalente a app:popUpTo="@id/loginFragment" popUpToInclusive="true"
                                // del nav_graph.xml — Login no queda en la back stack.
                                popUpTo(AppDestination.Login.route) { inclusive = true }
                            }
                        },
                        onAlreadyLoggedIn = {
                            navController.navigate(AppDestination.ScanQr.route) {
                                popUpTo(AppDestination.Login.route) { inclusive = true }
                            }
                        },
                        onGoToSignup = {
                            navController.navigate(AppDestination.Signup.route)
                        }
                    )
                }

                composable(AppDestination.Signup.route) {
                    val viewModel: SignupViewModel = hiltViewModel()
                    SignupScreen(
                        viewModel = viewModel,
                        onSignupSuccess = {
                            Toast.makeText(context, "Registro exitoso. Inicia sesión.", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    )
                }

                composable(AppDestination.ScanQr.route) {
                    val viewModel: ScanQrViewModel = hiltViewModel()
                    // LocalContext.current: reemplaza requireContext() de Fragment.
                    // Da acceso al Context de Android desde cualquier Composable sin
                    // necesidad de pasarlo como parámetro explícito.
                    val scanner = GmsBarcodeScanning.getClient(context)
                    ScanQrScreen(
                        viewModel = viewModel,
                        onStartScan = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val rawValue = barcode.rawValue
                                    if (rawValue != null) {
                                        viewModel.onQrScanned(rawValue)
                                    } else {
                                        viewModel.onScanError("Error: El código QR está vacío")
                                    }
                                }
                                .addOnCanceledListener {
                                    Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    viewModel.onScanError("Error al iniciar escáner: ${e.message}")
                                }
                        }
                    )
                }

                composable(AppDestination.Ranking.route) {
                    val viewModel: RankingViewModel = hiltViewModel()
                    RankingScreen(
                        viewModel = viewModel
                    )
                }

                composable(AppDestination.Estadistica.route) {
                    val viewModel: EstadisticaViewModel = hiltViewModel()
                    EstadisticaScreen(
                        viewModel = viewModel
                    )
                }

                composable(AppDestination.Map.route) {
                    val viewModel: RecyclingMapViewModel = hiltViewModel()
                    RecyclingMapScreen(
                        viewModel = viewModel
                    )
                }

                composable(AppDestination.Cupones.route) {
                    val viewModel: CuponesViewModel = hiltViewModel()
                    CuponesScreen(
                        viewModel = viewModel
                    )
                }

                composable(AppDestination.Logros.route) {
                    val viewModel: LogrosViewModel = hiltViewModel()
                    LogrosScreen(
                        viewModel = viewModel
                    )
                }

                composable(AppDestination.Trivia.route) {
                    val viewModel: TriviaViewModel = hiltViewModel()
                    TriviaScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
