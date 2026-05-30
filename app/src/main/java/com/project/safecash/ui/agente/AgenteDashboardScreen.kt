package com.project.safecash.ui.agente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.ui.navigation.Screen
import com.project.safecash.ui.theme.BackgroundGray
import com.project.safecash.ui.user.BalanceCard
import com.project.safecash.ui.user.QuickActionButton
import com.project.safecash.ui.user.SolicitudItem

/**
 * Pantalla principal del Agente Operativo (anteriormente Escolta).
 * Muestra el balance acumulado y la lista de servicios asignados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgenteDashboardScreen(
    navController: NavController, 
    viewModel: AgenteViewModel = viewModel() // Se actualizó de EscoltaViewModel a AgenteViewModel
) {
    // Se corrigió la observación de datos para usar el nuevo AgenteViewModel
    val agenteData by viewModel.agenteData.collectAsStateWithLifecycle()
    val tareas by viewModel.tareasAsignadas.collectAsStateWithLifecycle()
    val authRepository = remember { AuthRepository() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel Agente Operativo", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        authRepository.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Hola, ${agenteData?.nombre ?: "Agente"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            item {
                BalanceCard(balance = agenteData?.saldoActual ?: 0.0)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionButton(
                        text = "Cierre Turno",
                        icon = Icons.Default.Assignment,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.CierreTurno.route) }
                    )
                    QuickActionButton(
                        text = "Historial",
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO: Implementar navegación a historial */ }
                    )
                }
            }

            item {
                Text(
                    text = "Servicios Asignados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (tareas.isEmpty()) {
                item {
                    Text(
                        "No tienes tareas pendientes",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }

            items(tareas) { tarea ->
                SolicitudItem(solicitud = tarea)
            }
        }
    }
}
