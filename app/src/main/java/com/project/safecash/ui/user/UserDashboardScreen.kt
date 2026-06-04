package com.project.safecash.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.navigation.Screen
import com.project.safecash.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(navController: NavController, viewModel: UserViewModel = viewModel()) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val solicitudes by viewModel.solicitudes.collectAsStateWithLifecycle()
    val notifLlegada by viewModel.notificacionLlegada.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notifLlegada) {
        notifLlegada?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long,
                actionLabel = "Ok"
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
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
                            color = AccentBlue
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hola, ${userData?.nombre ?: "Usuario"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Ver mi perfil",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentBlue
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { 
                            if (notifLlegada != null) {
                                // Podríamos navegar a una pantalla de notificaciones o mostrar un diálogo
                                viewModel.clearNotification()
                            }
                        }) {
                            Icon(
                                Icons.Default.Notifications, 
                                contentDescription = "Notificaciones", 
                                tint = if (notifLlegada != null) WarningOrange else TextSecondary
                            )
                        }
                        if (notifLlegada != null) {
                            Surface(
                                color = ErrorRed,
                                shape = CircleShape,
                                modifier = Modifier.size(8.dp).align(Alignment.TopEnd).offset(x = (-8).dp, y = (8).dp)
                            ) {}
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { BalanceCard(balance = userData?.saldo ?: 0.0) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        text = "Nueva Solicitud",
                        icon = Icons.Default.AddCard,
                        containerColor = AccentBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.CrearSolicitud.route) }
                    )
                    QuickActionButton(
                        text = "Historial Completo",
                        icon = Icons.Default.History,
                        containerColor = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.ReporteMovimientos.route) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividad de Hoy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { navController.navigate(Screen.ReporteMovimientos.route) }) {
                        Text("Ver historial", color = AccentBlue, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (solicitudes.isEmpty()) {
                item { EmptyActivityState() }
            } else {
                items(solicitudes) { solicitud ->
                    SolicitudItem(solicitud = solicitud) {
                        navController.navigate(Screen.DetalleServicio.createRoute(solicitud.id))
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    val formatter = remember {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("$ #,###.##", symbols)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(brush = Brush.horizontalGradient(GradientAccent))
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(text = "Tu Saldo Digital", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatter.format(balance),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp).align(Alignment.CenterEnd).offset(x = 20.dp)
            )
        }
    }
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, containerColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun SolicitudItem(solicitud: Solicitud, onClick: () -> Unit) {
    val formatter = remember {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("$ #,###", symbols)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape, 
                color = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed.copy(0.1f) else SuccessGreen.copy(0.1f), 
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (solicitud.tipoServicio == "RECOLECCION") Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade,
                    modifier = Modifier.padding(12.dp),
                    tint = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed else SuccessGreen,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if(solicitud.tipoServicio == "RECOLECCION") "Entregar Efectivo" else "Pedir Efectivo", 
                    fontWeight = FontWeight.Bold, 
                    color = TextPrimary
                )
                Text(text = solicitud.estado, style = MaterialTheme.typography.labelSmall, color = if(solicitud.estado == "FINALIZADA") SuccessGreen else TextTertiary)
            }
            Text(text = formatter.format(solicitud.monto), fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
fun EmptyActivityState() {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary.copy(alpha = 0.3f))
        Text(text = "Sin movimientos hoy", color = TextSecondary)
    }
}
