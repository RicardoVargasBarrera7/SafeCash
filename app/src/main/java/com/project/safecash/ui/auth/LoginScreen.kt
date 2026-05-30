package com.project.safecash.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.safecash.ui.navigation.Screen

/**
 * LoginScreen es la pantalla principal de autenticación de SafeCash.
 * Permite a los usuarios ingresar con su correo y contraseña, manejando diferentes roles
 * de usuario (Admin, Agente Operativo y Usuario).
 *
 * @param navController Controlador de navegación para redirigir tras un login exitoso.
 * @param viewModel ViewModel encargado de la lógica de autenticación y manejo de estados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    // Estados locales para los campos de entrada
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Observación del estado de autenticación global
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Efecto secundario que reacciona a los cambios en el estado de autenticación
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            val role = (authState as AuthViewModel.AuthState.Success).role
            
            // Determinamos la ruta de destino basada en el rol del usuario autenticado
            val route = when (role) {
                "ADMIN" -> Screen.AdminDashboard.route
                // Se corrigió de EscoltaDashboard a AgenteDashboard para coincidir con Screen.kt
                "ESCOLTA" -> Screen.AgenteDashboard.route
                // Se corrigió de ClienteDashboard a UserDashboard (Usuario) para coincidir con Screen.kt
                else -> Screen.UserDashboard.route
            }
            
            // Navegamos al dashboard correspondiente y limpiamos el historial de navegación
            navController.navigate(route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else if (authState is AuthViewModel.AuthState.Error) {
            // Muestra un mensaje de error si las credenciales son incorrectas o hay fallos de red
            Toast.makeText(context, (authState as AuthViewModel.AuthState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SafeCash", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sección de bienvenida
            Text(
                text = "Bienvenido",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Inicia sesión para continuar",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo de entrada para el correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de entrada para la contraseña con opción de ocultar/mostrar texto
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar/Ocultar contraseña")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de acción principal para iniciar sesión
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = authState !is AuthViewModel.AuthState.Loading
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    // Muestra un indicador de carga mientras se procesa la solicitud
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Opción secundaria para usuarios que no tienen cuenta
            TextButton(
                onClick = { navController.navigate(Screen.Register.route) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}
