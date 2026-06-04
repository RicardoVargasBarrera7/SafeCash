package com.project.safecash.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.project.safecash.data.model.User
import com.project.safecash.ui.navigation.Screen
import com.project.safecash.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAgentesScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var agentes by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("usuarios")
            .whereEqualTo("rol", "AGENTE")
            .addSnapshotListener { snapshot, _ ->
                isLoading = false
                if (snapshot != null) {
                    agentes = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Agentes", fontWeight = FontWeight.Bold) },
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
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(agentes) { agente ->
                    AgenteCard(
                        agente = agente,
                        navController = navController,
                        onAsignarBase = { monto ->
                            viewModel.asignarBaseAAgente(
                                agenteId = agente.id,
                                monto = monto,
                                onSuccess = { },
                                onError = { }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AgenteCard(agente: User, navController: NavController, onAsignarBase: (Double) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var rawMonto by remember { mutableStateOf("") }
    
    val formatter = remember {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' }
        DecimalFormat("#,###", symbols)
    }

    val visualMonto = remember(rawMonto) {
        if (rawMonto.isEmpty()) "" 
        else try { formatter.format(rawMonto.toLong()) } catch (e: Exception) { rawMonto }
    }

    val balanceFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("$ #,###.##", symbols)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Asignar Base a ${agente.nombre}", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = visualMonto,
                    onValueChange = { input ->
                        val clean = input.replace(".", "").filter { it.isDigit() }
                        if (clean.length <= 12) rawMonto = clean
                    },
                    label = { Text("Monto COP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("$ ") },
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val monto = rawMonto.toDoubleOrNull()
                        if (monto != null && monto > 0) {
                            onAsignarBase(monto)
                            showDialog = false
                            rawMonto = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Confirmar") }
            },
            dismissButton = { 
                TextButton(onClick = { showDialog = false }) { 
                    Text("Cancelar", color = TextSecondary) 
                } 
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = SurfaceWhite
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = PrimaryBlue.copy(alpha = 0.05f), modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(14.dp), tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(agente.nombre, fontWeight = FontWeight.Bold, color = TextPrimary)
                    
                    val estadoColor = when(agente.estado) {
                        "EN_SERVICIO" -> SuccessGreen
                        "ACTIVO" -> Color.Gray
                        else -> ErrorRed
                    }
                    val estadoText = when(agente.estado) {
                        "EN_SERVICIO" -> "Disponible"
                        "ACTIVO" -> "Fuera de Servicio"
                        else -> "Inactivo"
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = estadoColor, modifier = Modifier.size(8.dp)) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(estadoText, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Efectivo", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(balanceFormatter.format(agente.saldoActual), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryDark)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cargar Base", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = { navController.navigate(Screen.ReporteMovimientos.createRoute(agente.id)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Text("Ver Reporte", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}
