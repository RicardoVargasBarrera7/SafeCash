package com.project.safecash.ui.agente

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun TareasDisponiblesScreen(
    navController: NavController,
    viewModel: AgenteViewModel = viewModel()
) {
    // Usamos un nombre diferente para la lista local para evitar shadowing
    val listaTareasDisponibles by viewModel.tareasDisponibles.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("¡Servicio Asignado!", fontWeight = FontWeight.Bold) },
            text = { Text("Has aceptado el servicio con éxito. Ahora aparecerá en tu lista de tareas asignadas.") },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ir a mi Dashboard")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = SurfaceWhite
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios en la Zona", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Hay ${listaTareasDisponibles.size} solicitudes esperando",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (listaTareasDisponibles.isEmpty()) {
                item {
                    EmptyDiscoveryState()
                }
            }

            items(listaTareasDisponibles, key = { it.id }) { solicitudItem ->
                TareaDisponibleCard(
                    solicitud = solicitudItem,
                    onAceptar = {
                        // Llamada explícita al ViewModel pasando el objeto Solicitud completo
                        viewModel.aceptarTarea(
                            solicitud = solicitudItem,
                            onSuccess = { showSuccessDialog = true },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun TareaDisponibleCard(solicitud: Solicitud, onAceptar: () -> Unit) {
    val formatter = remember {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' }
        DecimalFormat("$ #,###", symbols)
    }

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
                    shape = RoundedCornerShape(12.dp),
                    color = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (solicitud.tipoServicio == "RECOLECCION") "RECAUDO" else "BASE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed else SuccessGreen
                    )
                }
                Text(
                    text = formatter.format(solicitud.monto),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = solicitud.direccion, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAceptar,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Aceptar Servicio", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyDiscoveryState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(80.dp), tint = TextTertiary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Buscando solicitudes...", fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("No hay pendientes en este momento", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
