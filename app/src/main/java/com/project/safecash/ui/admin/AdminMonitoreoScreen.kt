package com.project.safecash.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMonitoreoScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val serviciosActivos by viewModel.serviciosActivos.collectAsStateWithLifecycle()
    val agentesMap by viewModel.agentesMap.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitoreo Operativo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Servicios en curso",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (serviciosActivos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay actividad en tiempo real", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(serviciosActivos, key = { it.id }) { solicitud ->
                        MonitoringCard(solicitud, agentesMap)
                    }
                }
            }
        }
    }
}

@Composable
fun MonitoringCard(solicitud: Solicitud, agentesMap: Map<String, String>) {
    val formatter = remember { DecimalFormat("$ #,###", DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' }) }
    val agenteNombre = agentesMap[solicitud.agenteId] ?: "Pendiente"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when(solicitud.estado) {
                        "EN_PROCESO" -> AccentBlue.copy(alpha = 0.1f)
                        "EN_CAMINO" -> SuccessGreen.copy(alpha = 0.1f)
                        else -> BorderLight
                    }
                ) {
                    Text(
                        text = solicitud.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when(solicitud.estado) {
                            "EN_PROCESO" -> AccentBlue
                            "EN_CAMINO" -> SuccessGreen
                            else -> TextSecondary
                        }
                    )
                }
                Text(
                    text = formatter.format(solicitud.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Agente", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(agenteNombre, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Icon(
                    imageVector = if (solicitud.tipoServicio == "RECOLECCION") Icons.Default.SwapHoriz else Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp).padding(horizontal = 4.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Cliente", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(solicitud.usuarioNombre ?: "Usuario", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, modifier = Modifier.size(16.dp), tint = TextTertiary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = solicitud.direccion, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
        }
    }
}
