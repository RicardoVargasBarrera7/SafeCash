package com.project.safecash.ui.agente

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project.safecash.R
import com.project.safecash.data.model.Solicitud
import com.google.firebase.firestore.FirebaseFirestore
import com.project.safecash.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleServicioScreen(
    navController: NavController,
    solicitudId: String,
    viewModel: AgenteViewModel = viewModel()
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    var solicitud by remember { mutableStateOf<Solicitud?>(null) }

    LaunchedEffect(solicitudId) {
        firestore.collection("solicitudes").document(solicitudId)
            .addSnapshotListener { snapshot, _ ->
                solicitud = snapshot?.toObject(Solicitud::class.java)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Servicio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        solicitud?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Indicator Card
                val statusColor = when (item.estado) {
                    "PENDIENTE" -> Color(0xFFF59E0B)
                    "ASIGNADA" -> AccentBlue
                    "EN_PROCESO" -> Color(0xFF6366F1)
                    "FINALIZADA" -> AccentGreen
                    else -> TextLight
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(shape = CircleShape, color = statusColor, modifier = Modifier.size(8.dp)) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ESTADO: ${item.estado}",
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        DetailItem(
                            icon = if (item.tipoServicio == "RETIRO") Icons.Default.SouthWest else Icons.Default.NorthEast,
                            iconColor = if (item.tipoServicio == "RETIRO") ErrorRed else AccentGreen,
                            label = "Tipo de Servicio",
                            value = item.tipoServicio
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundGray)
                        
                        DetailItem(
                            icon = Icons.Default.Payments,
                            iconColor = PrimaryBlue,
                            label = "Monto a Procesar",
                            value = stringResource(R.string.balance_format, item.monto),
                            isAmount = true
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundGray)

                        DetailItem(
                            icon = Icons.Default.LocationOn,
                            iconColor = ErrorRed,
                            label = "Dirección de Encuentro",
                            value = item.direccion
                        )

                        if (item.observaciones.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BackgroundGray)
                            DetailItem(
                                icon = Icons.Default.Notes,
                                iconColor = TextLight,
                                label = "Instrucciones",
                                value = item.observaciones
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Action Buttons
                when (item.estado) {
                    "PENDIENTE" -> {
                        ActionButton(
                            text = "Aceptar Servicio",
                            containerColor = AccentBlue,
                            onClick = { viewModel.actualizarEstado(solicitudId, "ASIGNADA") }
                        )
                    }
                    "ASIGNADA" -> {
                        ActionButton(
                            text = "Iniciar Recorrido",
                            containerColor = Color(0xFF6366F1),
                            onClick = { viewModel.actualizarEstado(solicitudId, "EN_PROCESO") }
                        )
                    }
                    "EN_PROCESO" -> {
                        ActionButton(
                            text = "Confirmar Finalización",
                            containerColor = AccentGreen,
                            onClick = { viewModel.actualizarEstado(solicitudId, "FINALIZADA") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, iconColor: Color, label: String, value: String, isAmount: Boolean = false) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextLight)
            Text(
                text = value,
                style = if (isAmount) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                fontWeight = if (isAmount) FontWeight.Bold else FontWeight.SemiBold,
                color = TextDark
            )
        }
    }
}

@Composable
fun ActionButton(text: String, containerColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
