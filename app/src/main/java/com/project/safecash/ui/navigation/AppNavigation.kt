package com.project.safecash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.safecash.ui.admin.AdminAgentesScreen
import com.project.safecash.ui.admin.AdminDashboardScreen
import com.project.safecash.ui.admin.AdminMovimientosScreen
import com.project.safecash.ui.admin.AdminUsuariosScreen
import com.project.safecash.ui.agente.AgenteDashboardScreen
import com.project.safecash.ui.agente.CierreTurnoScreen
import com.project.safecash.ui.agente.DetalleServicioScreen
import com.project.safecash.ui.agente.TareasDisponiblesScreen
import com.project.safecash.ui.auth.LoginScreen
import com.project.safecash.ui.auth.RegisterScreen
import com.project.safecash.ui.auth.SplashScreen
import com.project.safecash.ui.user.CrearSolicitudScreen
import com.project.safecash.ui.user.UserDashboardScreen

/**
 * AppNavigation centraliza toda la navegación de SafeCash 100% en Jetpack Compose.
 * Sin XML, sin Fragments, sin SafeArgs — solo Compose Navigation.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── Autenticación ──────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        // ── Usuario / Cliente ──────────────────────────────────────────────────
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(navController)
        }
        composable(Screen.CrearSolicitud.route) {
            CrearSolicitudScreen(navController)
        }

        // ── Agente Operativo ───────────────────────────────────────────────────
        composable(Screen.AgenteDashboard.route) {
            AgenteDashboardScreen(navController)
        }
        composable(Screen.CierreTurno.route) {
            CierreTurnoScreen(navController)
        }
        composable(Screen.TareasDisponibles.route) {
            TareasDisponiblesScreen(navController)
        }
        composable(
            route = Screen.DetalleServicio.route,
            arguments = listOf(navArgument("solicitudId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("solicitudId") ?: ""
            DetalleServicioScreen(navController, id)
        }

        // ── Administrador ──────────────────────────────────────────────────────
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController)
        }
        composable(Screen.AdminAgentes.route) {
            AdminAgentesScreen(navController)
        }
        composable(Screen.AdminUsuarios.route) {
            AdminUsuariosScreen(navController)
        }
        composable(Screen.AdminMovimientos.route) {
            AdminMovimientosScreen(navController)
        }
    }
}
