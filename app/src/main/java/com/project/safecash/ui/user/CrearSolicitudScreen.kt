package com.project.safecash.ui.user

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSolicitudScreen(navController: NavController, viewModel: SolicitudViewModel = viewModel()) {
    var monto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("ENTREGA") }
    var direccion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val solicitudState by viewModel.solicitudState.collectAsState()
    val authRepository = remember { AuthRepository() }

    LaunchedEffect(solicitudState) {
        if (solicitudState is SolicitudViewModel.SolicitudState.Success) {
            Toast.makeText(context, "Solicitud creada con éxito", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } else if (solicitudState is SolicitudViewModel.SolicitudState.Error) {
            Toast.makeText(context, (solicitudState as SolicitudViewModel.SolicitudState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Solicitud", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("ENTREGA") },
                        onClick = {
                            tipo = "ENTREGA"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("RECOLECCION") },
                        onClick = {
                            tipo = "RECOLECCION"
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección de destino") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

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
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = solicitudState !is SolicitudViewModel.SolicitudState.Loading
            ) {
                if (solicitudState is SolicitudViewModel.SolicitudState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Enviar Solicitud", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
