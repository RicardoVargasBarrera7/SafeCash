package com.project.safecash.ui.agente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.ui.navigation.Screen
import com.project.safecash.ui.theme.*
import com.project.safecash.ui.user.BalanceCard
import com.project.safecash.ui.user.QuickActionButton
import com.project.safecash.ui.user.SolicitudItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgenteDashboardScreen(
    navController: NavController,
    viewModel: AgenteViewModel = viewModel()
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val tareas by viewModel.tareasAsignadas.collectAsStateWithLifecycle()
    val authRepository = remember { AuthRepository() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Agente: ${userData?.nombre ?: ""}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Operaciones en curso",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Notificaciones */ },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            authRepository.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = ErrorRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                // Balance del efectivo que carga el agente
                BalanceCard(balance = userData?.saldo ?: 0.0)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        text = "Tareas Libres",
                        icon = Icons.Default.Task,
                        containerColor = AccentGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.TareasDisponibles.route) }
                    )
                    QuickActionButton(
                        text = "Cerrar Turno",
                        icon = Icons.Default.Assignment,
                        containerColor = SecondaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.CierreTurno.route) }
                    )
                }
            }

            item {
                Text(
                    text = "Mis Tareas Asignadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (tareas.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Task, contentDescription = null, tint = TextLight, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No tienes tareas pendientes", color = TextLight)
                        }
                    }
                }
            } else {
                items(tareas) { tarea ->
                    SolicitudItem(
                        solicitud = tarea,
                        onClick = {
                            navController.navigate(Screen.DetalleServicio.createRoute(tarea.id))
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
