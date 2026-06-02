package com.project.safecash.ui.agente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
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
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.navigation.Screen
import com.project.safecash.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasDisponiblesScreen(
    navController: NavController,
    viewModel: AgenteViewModel = viewModel()
) {
    val tareasDisponibles by viewModel.tareasDisponibles.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios Disponibles", fontWeight = FontWeight.Bold) },
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
                    text = "${tareasDisponibles.size} Oportunidades cerca de ti",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextLight,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (tareasDisponibles.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = null, 
                                modifier = Modifier.padding(20.dp), 
                                tint = TextLight.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay servicios disponibles", 
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Vuelve a consultar más tarde", 
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight
                        )
                    }
                }
            }

            items(tareasDisponibles) { tarea ->
                TareaDisponibleCard(
                    solicitud = tarea,
                    onVerDetalle = {
                        navController.navigate(Screen.DetalleServicio.createRoute(tarea.id))
                    },
                    onAceptar = {
                        viewModel.aceptarTarea(tarea.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun TareaDisponibleCard(
    solicitud: Solicitud,
    onVerDetalle: () -> Unit,
    onAceptar: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm - dd MMM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (solicitud.tipoServicio == "RETIRO") ErrorRed.copy(alpha = 0.1f) else AccentGreen.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (solicitud.tipoServicio == "RETIRO") Icons.Default.SouthWest else Icons.Default.NorthEast,
                            contentDescription = null,
                            tint = if (solicitud.tipoServicio == "RETIRO") ErrorRed else AccentGreen,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = solicitud.tipoServicio,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                
                Text(
                    text = "$ %.2f".format(solicitud.monto),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = solicitud.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight,
                    maxLines = 1
                )
            }

            Text(
                text = "Publicado: ${dateFormat.format(solicitud.fechaCreacion.toDate())}",
                style = MaterialTheme.typography.labelSmall,
                color = TextLight.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, start = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onVerDetalle,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BackgroundGray)
                ) {
                    Text("Detalles", color = TextDark)
                }
                Button(
                    onClick = onAceptar,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
