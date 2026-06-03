package com.project.safecash.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMonitoreoScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val serviciosActivos by viewModel.serviciosActivos.collectAsStateWithLifecycle()
    val defaultPos = LatLng(4.6097, -74.0817) // Coordenadas por defecto (Bogotá)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitoreo en Tiempo Real", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                serviciosActivos.forEach { solicitud ->
                    val pos = LatLng(solicitud.latitudDestino, solicitud.longitudDestino)
                    Marker(
                        state = MarkerState(position = pos),
                        title = "Servicio: ${solicitud.tipoOperacion}",
                        snippet = "Monto: $${solicitud.monto} - Estado: ${solicitud.estado}",
                    )
                    
                    // Métrica adicional: Nombre del agente sobre el marcador si está asignado
                    if (solicitud.agenteId != null) {
                        Marker(
                            state = MarkerState(position = pos),
                            icon = null, // Usar marcador por defecto o personalizado
                            title = "Agente Asignado: ${solicitud.agenteId}", // Aquí podrías cargar el nombre real
                            alpha = 0.8f
                        )
                    }
                }
            }

            // Panel inferior con lista rápida
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Servicios en curso (${serviciosActivos.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp).background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp)).padding(4.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(serviciosActivos) { servicio ->
                        MonitoringMiniCard(servicio)
                    }
                }
            }
        }
    }
}

@Composable
fun MonitoringMiniCard(solicitud: Solicitud) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PersonPin, 
                    contentDescription = null, 
                    tint = if(solicitud.tipoOperacion == "RECOLECCION") ErrorRed else AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(solicitud.tipoOperacion, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text("Estado: ${solicitud.estado}", fontSize = 10.sp, color = TextLight)
            Text("Monto: $ ${solicitud.monto}", fontWeight = FontWeight.Bold, color = PrimaryBlue)
        }
    }
}
