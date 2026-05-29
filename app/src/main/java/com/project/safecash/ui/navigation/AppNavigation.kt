package com.project.safecash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.safecash.ui.auth.LoginScreen
import com.project.safecash.ui.auth.RegisterScreen
import com.project.safecash.ui.auth.SplashScreen
import com.project.safecash.ui.user.UserDashboardScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(navController)
        }
        // Add other screens as they are implemented
    }
}
