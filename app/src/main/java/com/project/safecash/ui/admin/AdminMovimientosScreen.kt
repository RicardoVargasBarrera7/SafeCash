package com.project.safecash.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
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
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMovimientosScreen(navController: NavController) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var solicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("solicitudes")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                isLoading = false
                solicitudes = snapshot?.toObjects(Solicitud::class.java) ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Movimientos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Filtrar */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
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
                        text = "${solicitudes.size} Registros encontrados",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextLight,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (solicitudes.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextLight.copy(alpha = 0.3f))
                            Text("No hay movimientos registrados", color = TextLight)
                        }
                    }
                }

                items(solicitudes) { solicitud ->
                    MovimientoCard(solicitud = solicitud)
                }
            }
        }
    }
}

@Composable
private fun MovimientoCard(solicitud: Solicitud) {
    val estadoColor = when (solicitud.estado) {
        "PENDIENTE"  -> Color(0xFFF59E0B)
        "ASIGNADA"   -> AccentBlue
        "EN_PROCESO" -> Color(0xFF6366F1)
        "FINALIZADA" -> AccentGreen
        "CANCELADA"  -> ErrorRed
        else         -> TextLight
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Column {
                        Text(
                            text = solicitud.tipoServicio,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = dateFormat.format(solicitud.fechaCreacion.toDate()),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLight
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = estadoColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = solicitud.estado,
                        style = MaterialTheme.typography.labelSmall,
                        color = estadoColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dirección",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLight
                    )
                    Text(
                        text = solicitud.direccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDark,
                        maxLines = 1
                    )
                }
                Text(
                    text = "$ %.2f".format(solicitud.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }
        }
    }
}
