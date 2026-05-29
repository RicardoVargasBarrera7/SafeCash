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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.ui.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val authRepository = AuthRepository()

    LaunchedEffect(key1 = true) {
        delay(2000)
        val uid = authRepository.getCurrentUserId()
        if (uid != null) {
            val role = authRepository.getUserRole(uid)
            val route = when (role) {
                "ADMIN" -> Screen.AdminDashboard.route
                "ESCOLTA" -> Screen.EscoltaDashboard.route
                else -> Screen.UserDashboard.route
            }
            navController.navigate(route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SafeCash",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
