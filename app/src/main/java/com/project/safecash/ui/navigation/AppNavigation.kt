package com.project.safecash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.safecash.ui.auth.LoginScreen
import com.project.safecash.ui.auth.RegisterScreen
import com.project.safecash.ui.auth.SplashScreen
import com.project.safecash.ui.agente.AgenteDashboardScreen
import com.project.safecash.ui.user.UserDashboardScreen

/**
 * Grafo de navegación principal de la aplicación.
 * Define todas las rutas disponibles y asocia cada una con su pantalla (Composable).
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController, 
        startDestination = Screen.Splash.route
    ) {
        // Pantalla de carga inicial
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        // Flujo de Autenticación
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        // Panel para Usuarios (Antes llamado ClienteDashboard)
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(navController)
        }

        // Panel para Agentes Operativos
        composable(Screen.AgenteDashboard.route) {
            AgenteDashboardScreen(navController)
        }

        // Nota: El AdminDashboard se maneja mediante Fragment, 
        // por lo que su navegación se dispara desde la Activity principal 
        // o mediante un wrapper Composable si es necesario.
    }
}
