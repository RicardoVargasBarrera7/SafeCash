package com.project.safecash.ui.reporte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteMovimientosScreen(
    navController: NavController,
    agenteIdParam: String? = null
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUid = auth.currentUser?.uid ?: ""
    
    var solicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var targetName by remember { mutableStateOf("") }

    LaunchedEffect(currentUid, agenteIdParam) {
        if (currentUid.isEmpty()) return@LaunchedEffect
        
        // Determinar qué datos mostrar
        firestore.collection("usuarios").document(currentUid).get().addOnSuccessListener { currentDoc ->
            val myRole = currentDoc.getString("rol") ?: "USUARIO"
            
            // Si soy ADMIN y me pasaron un agenteIdParam, muestro lo de ese agente
            val targetUid = if (myRole == "ADMIN" && agenteIdParam != null) agenteIdParam else currentUid
            
            if (targetUid != currentUid) {
                firestore.collection("usuarios").document(targetUid).get().addOnSuccessListener {
                    targetName = it.getString("nombre") ?: "Agente"
                }
            }

            val query = firestore.collection("solicitudes")
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)

            val filteredQuery = when {
                myRole == "ADMIN" && agenteIdParam == null -> query // Admin ve todo
                myRole == "ADMIN" && agenteIdParam != null -> query.whereEqualTo("agenteId", agenteIdParam)
                myRole == "AGENTE" -> query.whereEqualTo("agenteId", currentUid)
                else -> query.whereEqualTo("usuarioId", currentUid)
            }

            filteredQuery.addSnapshotListener { snapshot, _ ->
                isLoading = false
                if (snapshot != null) {
                    solicitudes = snapshot.toObjects(Solicitud::class.java).mapIndexed { index, item ->
                        item.copy(id = snapshot.documents[index].id)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Historial de Movimientos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (targetName.isNotEmpty()) {
                            Text(targetName, style = MaterialTheme.typography.labelSmall, color = AccentBlue)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Filtros avanzados */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else if (solicitudes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay registros históricos", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(solicitudes) { solicitud ->
                    MovimientoItem(solicitud = solicitud)
                }
            }
        }
    }
}

@Composable
fun MovimientoItem(solicitud: Solicitud) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formatter = remember {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' }
        DecimalFormat("$ #,###", symbols)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed.copy(0.1f) else SuccessGreen.copy(0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (solicitud.tipoServicio == "RECOLECCION") Icons.Default.SouthWest else Icons.Default.NorthEast,
                    contentDescription = null,
                    tint = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed else SuccessGreen,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (solicitud.tipoServicio == "RECOLECCION") "Recaudo Digital" else "Pedido Efectivo",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = dateFormat.format(solicitud.fechaCreacion.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Text(
                    text = "Estado: ${solicitud.estado}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if(solicitud.estado == "FINALIZADA") SuccessGreen else AccentBlue
                )
            }
            Text(
                text = formatter.format(solicitud.monto),
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }
    }
}
