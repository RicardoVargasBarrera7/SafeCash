package com.project.safecash.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.ui.navigation.Screen
import com.project.safecash.ui.theme.AccentGreen
import com.project.safecash.ui.theme.BackgroundGray
import com.project.safecash.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(navController: NavController, viewModel: UserViewModel = viewModel()) {
    val userData by viewModel.userData.collectAsState()
    val solicitudes by viewModel.solicitudes.collectAsState()
    val authRepository = remember { AuthRepository() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SafeCash", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        authRepository.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
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
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Hola, ${userData?.nombre ?: "Usuario"}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Bienvenido de nuevo a SafeCash",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            item {
                BalanceCard(balance = userData?.saldo ?: 0.0)
            }

            item {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionButton(
                        text = "Nueva Solicitud",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.CrearSolicitud.route) }
                    )
                    QuickActionButton(
                        text = "Reportes",
                        icon = Icons.Default.Assessment,
                        modifier = Modifier.weight(1f),
                        onClick = { /* Navegar a reportes */ }
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
                        text = "Actividad Reciente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* Ver todo */ }) {
                        Text("Ver todo", color = PrimaryBlue)
                    }
                }
            }

            items(solicitudes) { solicitud ->
                SolicitudItem(solicitud = solicitud)
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Tu saldo disponible",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = String.format(Locale.getDefault(), "$ %.2f", balance),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " Cuenta Protegida",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PrimaryBlue
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SolicitudItem(solicitud: Solicitud) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = solicitud.tipoServicio,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Text(
                    text = sdf.format(solicitud.fechaCreacion.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "$ %.2f", solicitud.monto),
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyLarge
                )
                Surface(
                    color = when(solicitud.estado) {
                        "PENDIENTE" -> Color(0xFFFFF3E0)
                        "FINALIZADA" -> Color(0xFFE8F5E9)
                        else -> Color(0xFFE3F2FD)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = solicitud.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when(solicitud.estado) {
                            "PENDIENTE" -> Color(0xFFE65100)
                            "FINALIZADA" -> AccentGreen
                            else -> PrimaryBlue
                        }
                    )
                }
            }
        }
    }
}
