package com.project.safecash.ui.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.ui.solicitud.SolicitudViewModel
import com.project.safecash.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSolicitudScreen(navController: NavController, viewModel: SolicitudViewModel = viewModel()) {
    var monto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("RETIRO") }
    var direccion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val solicitudState by viewModel.solicitudState.collectAsState()
    val authRepository = remember { AuthRepository() }

    LaunchedEffect(solicitudState) {
        when (val state = solicitudState) {
            is SolicitudViewModel.SolicitudState.Success -> {
                Toast.makeText(context, "Solicitud creada con éxito", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is SolicitudViewModel.SolicitudState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Detalles del Servicio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Completa la información para que un agente pueda asistirte",
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selector de Tipo
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tipo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Servicio") },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("RETIRO (Recibir efectivo)") },
                                onClick = { tipo = "RETIRO"; expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("DEPÓSITO (Entregar efectivo)") },
                                onClick = { tipo = "DEPÓSITO"; expanded = false }
                            )
                        }
                    }

                    // Campo Monto
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text("Monto a transaccionar") },
                        placeholder = { Text("0.00") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Campo Dirección
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección de encuentro") },
                        placeholder = { Text("Ej: Calle Falsa 123, Edificio X") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    // Campo Observaciones
                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Indicaciones adicionales") },
                        placeholder = { Text("Ej: Portón negro, llamar al llegar...") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Alert
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = AccentBlue.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AccentBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Un agente operativo se asignará a tu ubicación tras confirmar la solicitud.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val montoVal = monto.toDoubleOrNull() ?: 0.0
                    val uid = authRepository.getCurrentUserId()
                    if (uid != null && montoVal > 0 && direccion.isNotEmpty()) {
                        val solicitud = Solicitud(
                            usuarioId = uid,
                            tipoServicio = tipo,
                            monto = montoVal,
                            direccion = direccion,
                            observaciones = observaciones
                        )
                        viewModel.crearSolicitud(solicitud)
                    } else {
                        Toast.makeText(context, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = solicitudState !is SolicitudViewModel.SolicitudState.Loading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (solicitudState is SolicitudViewModel.SolicitudState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Confirmar Solicitud", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
