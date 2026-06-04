package com.project.safecash.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.safecash.R
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
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
                title = { Text("Historial de Operaciones", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(solicitudes) { solicitud ->
                    MovimientoCard(solicitud = solicitud)
                }
            }
        }
    }
}

@Composable
private fun MovimientoCard(solicitud: Solicitud) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val symbols = DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' }
    val formatter = DecimalFormat("$ #,###", symbols)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed.copy(alpha = 0.1f) else AccentGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (solicitud.tipoServicio == "RECOLECCION") Icons.Default.SouthWest else Icons.Default.NorthEast,
                    contentDescription = null,
                    tint = if (solicitud.tipoServicio == "RECOLECCION") ErrorRed else AccentGreen,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (solicitud.tipoServicio == "RECOLECCION") "RECAUDO" else "ENTREGA BASE",
                    fontWeight = FontWeight.Bold, 
                    color = TextDark
                )
                Text(dateFormat.format(solicitud.fechaCreacion.toDate()), style = MaterialTheme.typography.labelSmall, color = TextLight)
            }
            Text(
                text = formatter.format(solicitud.monto),
                fontWeight = FontWeight.Black,
                color = PrimaryBlue
            )
        }
    }
}
