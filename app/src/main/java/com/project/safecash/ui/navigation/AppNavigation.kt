package com.project.safecash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.safecash.ui.auth.LoginScreen
import com.project.safecash.ui.auth.RegisterScreen
import com.project.safecash.ui.auth.SplashScreen
import com.project.safecash.ui.agente.AgenteDashboardScreen
import com.project.safecash.ui.agente.CierreTurnoScreen
import com.project.safecash.ui.agente.DetalleServicioScreen
import com.project.safecash.ui.agente.TareasDisponiblesScreen
import com.project.safecash.ui.user.UserDashboardScreen
import com.project.safecash.ui.user.CrearSolicitudScreen
import com.project.safecash.ui.admin.AdminDashboardScreen
import com.project.safecash.ui.admin.AdminAgentesScreen
import com.project.safecash.ui.admin.AdminUsuariosScreen
import com.project.safecash.ui.admin.AdminMovimientosScreen
import com.project.safecash.ui.admin.AdminMonitoreoScreen
import com.project.safecash.ui.profile.ProfileScreen
import com.project.safecash.ui.reporte.ReporteMovimientosScreen
import com.project.safecash.ui.notificaciones.NotificacionesScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }

        // --- GENERAL ---
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        
        composable(
            route = Screen.ReporteMovimientos.route,
            arguments = listOf(navArgument("agenteId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val agenteId = backStackEntry.arguments?.getString("agenteId")
            ReporteMovimientosScreen(navController, agenteId)
        }
        
        composable(Screen.Notificaciones.route) { NotificacionesScreen(navController) }

        // --- USUARIO ---
        composable(Screen.UserDashboard.route) { UserDashboardScreen(navController) }
        composable(Screen.CrearSolicitud.route) { CrearSolicitudScreen(navController) }

        // --- AGENTE ---
        composable(Screen.AgenteDashboard.route) { AgenteDashboardScreen(navController) }
        composable(Screen.CierreTurno.route) { CierreTurnoScreen(navController) }
        composable(Screen.TareasDisponibles.route) { TareasDisponiblesScreen(navController) }
        composable(
            route = Screen.DetalleServicio.route,
            arguments = listOf(navArgument("solicitudId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("solicitudId") ?: ""
            DetalleServicioScreen(navController, id)
        }

        // --- ADMIN ---
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen(navController) }
        composable(Screen.AdminAgentes.route) { AdminAgentesScreen(navController) }
        composable(Screen.AdminUsuarios.route) { AdminUsuariosScreen(navController) }
        composable(Screen.AdminMovimientos.route) { AdminMovimientosScreen(navController) }
        composable(Screen.AdminMonitoreo.route) { AdminMonitoreoScreen(navController) }
    }
}
