package com.project.safecash.ui.agente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.safecash.ui.navigation.Screen
import com.project.safecash.ui.theme.*
import com.project.safecash.ui.user.SolicitudItem
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgenteDashboardScreen(
    navController: NavController,
    viewModel: AgenteViewModel = viewModel()
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val tareas by viewModel.tareasAsignadas.collectAsStateWithLifecycle()
    val showNotif by viewModel.showNotification.collectAsStateWithLifecycle()
    val notifMensaje by viewModel.notificacionMensaje.collectAsStateWithLifecycle()
    
    val enServicio = userData?.estado == "EN_SERVICIO"
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notifMensaje) {
        notifMensaje?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.navigate(Screen.Profile.route) }
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = PrimaryBlue
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = userData?.nombre ?: "Agente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Ver mi perfil", style = MaterialTheme.typography.labelSmall, color = AccentBlue)
                        }
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = { 
                            if (showNotif) {
                                // Al hacer clic limpiamos el indicador visual
                                viewModel.clearNotification()
                            }
                        }) {
                            Icon(
                                Icons.Default.Notifications, 
                                contentDescription = null, 
                                tint = if (showNotif) WarningOrange else TextSecondary
                            )
                        }
                        if (showNotif) {
                            Surface(
                                color = ErrorRed,
                                shape = CircleShape,
                                modifier = Modifier.size(10.dp).align(Alignment.TopEnd).offset(x = (-10).dp, y = (10).dp)
                            ) {}
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ServiceStatusCard(isOn = enServicio, onToggle = { viewModel.toggleDisponibilidad() })
            }

            item {
                AgenteStatusCard(efectivo = userData?.saldoActual ?: 0.0, base = userData?.baseAsignada ?: 0.0)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AgenteActionButton(
                        text = "Tareas Libres",
                        icon = Icons.Default.Explore,
                        containerColor = if (enServicio) AccentBlue else Color.Gray.copy(alpha = 0.5f),
                        badgeCount = null,
                        modifier = Modifier.weight(1f),
                        enabled = enServicio,
                        onClick = { navController.navigate(Screen.TareasDisponibles.route) }
                    )
                    AgenteActionButton(
                        text = "Mi Reporte",
                        icon = Icons.Default.Assessment,
                        containerColor = PrimaryBlue,
                        badgeCount = null,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.ReporteMovimientos.route) }
                    )
                }
            }
            
            item {
                 AgenteActionButton(
                    text = "Cerrar Jornada / Devolver Base",
                    icon = Icons.Default.LockClock,
                    containerColor = PrimaryDark,
                    badgeCount = null,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Screen.CierreTurno.route) }
                )
            }

            if (!enServicio && tareas.none { it.estado != "FINALIZADA" }) {
                item { InfoBox(text = "Actívate para recibir nuevas solicitudes de base.", icon = Icons.Default.Info) }
            }

            item {
                Text(text = "Actividad de Hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            if (tareas.isEmpty()) {
                item { EmptyTareasState() }
            } else {
                items(tareas, key = { it.id }) { tarea ->
                    SolicitudItem(solicitud = tarea, onClick = { navController.navigate(Screen.DetalleServicio.createRoute(tarea.id)) })
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun ServiceStatusCard(isOn: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isOn) SuccessGreen.copy(0.08f) else Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOn) SuccessGreen.copy(0.2f) else BorderLight)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = if (isOn) SuccessGreen else Color.Gray.copy(0.2f), modifier = Modifier.size(10.dp)) {}
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = if (isOn) "ESTÁS DISPONIBLE" else "FUERA DE SERVICIO", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = if (isOn) SuccessGreen else TextSecondary)
                    Text(text = if (isOn) "Recibiendo solicitudes" else "Activa tu servicio para trabajar", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            }
            Switch(checked = isOn, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen))
        }
    }
}

@Composable
fun AgenteStatusCard(efectivo: Double, base: Double) {
    val formatter = remember { DecimalFormat("$ #,###", DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' }) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.background(brush = Brush.verticalGradient(GradientPrimary)).padding(24.dp)) {
            Text("Efectivo en Mano", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            Text(formatter.format(efectivo), color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Base Asignada", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                    Text(formatter.format(base), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.Payments, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun AgenteActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color, badgeCount: Int?, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, disabledContainerColor = Color.Gray.copy(0.1f)),
        contentPadding = PaddingValues(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(icon, null, modifier = Modifier.size(28.dp).align(Alignment.TopStart), tint = if (enabled) Color.White else Color.Gray)
            Text(text = text, modifier = Modifier.align(Alignment.BottomStart), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
fun InfoBox(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(color = WarningOrange.copy(0.05f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(0.2f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
    }
}

@Composable
fun EmptyTareasState() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)) {
        Column(modifier = Modifier.padding(40.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.TaskAlt, null, tint = TextTertiary.copy(0.3f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No hay pendientes", fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Tus tareas activas de hoy aparecerán aquí", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
