package com.project.safecash.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.ui.navigation.Screen
import kotlinx.coroutines.delay

/**
 * Pantalla de carga (Splash) que se muestra al iniciar la aplicación.
 * Realiza una comprobación de sesión activa para redirigir al usuario automáticamente
 * a su panel correspondiente según su rol almacenado en Firebase.
 */
@Composable
fun SplashScreen(navController: NavController) {
    // Repositorio para verificar el estado de la sesión y obtener roles de usuario
    val authRepository = AuthRepository()

    // Lógica de inicio de la aplicación que se ejecuta una sola vez al cargar la pantalla
    LaunchedEffect(key1 = true) {
        // Pausa de 2 segundos para mostrar el logo
        delay(2000)
        
        // Obtenemos el ID del usuario actual si existe sesión
        val uid = authRepository.getCurrentUserId()
        
        if (uid != null) {
            // Si el usuario ya está autenticado, buscamos su rol en la base de datos
            val role = authRepository.getUserRole(uid)
            
            // Determinamos la ruta de destino basada en el rol del usuario
            val route = when (role) {
                "ADMIN" -> Screen.AdminDashboard.route
                // Se sincronizó: ESCOLTA -> AgenteDashboard
                "ESCOLTA" -> Screen.AgenteDashboard.route
                // Se sincronizó: USUARIO -> UserDashboard
                else -> Screen.UserDashboard.route
            }
            
            // Navegamos al Dashboard correspondiente y eliminamos el Splash del historial
            navController.navigate(route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // Si no hay sesión iniciada, redirigimos a la pantalla de Login
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // Diseño visual de la pantalla Splash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icono central de seguridad
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Logo SafeCash",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Nombre de la aplicación en negrita
            Text(
                text = "SafeCash",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
