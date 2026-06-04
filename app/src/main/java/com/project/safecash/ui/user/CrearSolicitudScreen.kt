package com.project.safecash.ui.user

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.ui.solicitud.SolicitudViewModel
import com.project.safecash.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSolicitudScreen(
    navController: NavController, 
    viewModel: SolicitudViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val userData by userViewModel.userData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    var rawMonto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("ENTREGA") }
    var direccion by remember(userData) { mutableStateOf(userData?.direccionPrincipal ?: "") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Estado del Mapa
    val bogota = LatLng(4.6097, -74.0817)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 14f)
    }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    // Launcher de Permisos de Ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    selectedLocation = userLatLng
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 16f)
                    
                    // Geocoding reverso para obtener dirección automáticamente
                    obtenerDireccion(context, userLatLng) { addr ->
                        direccion = addr
                    }
                }
            }
        } else {
            Toast.makeText(context, "Permisos de ubicación necesarios para verificar tu posición", Toast.LENGTH_SHORT).show()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(64.dp)) },
            title = { Text("Solicitud Exitosa", fontWeight = FontWeight.Bold) },
            text = { Text("Tu pedido de ${if(tipo=="ENTREGA") "efectivo" else "recaudo"} ha sido enviado. Un agente llegará pronto.") },
            confirmButton = {
                Button(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Entendido") }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Pedido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Mapa interactivo
            Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                        obtenerDireccion(context, latLng) { addr ->
                            direccion = addr
                        }
                    }
                ) {
                    selectedLocation?.let {
                        Marker(state = MarkerState(position = it), title = "Punto de encuentro")
                    }
                }

                // Botón Flotante para Ubicación Real (GPS)
                FloatingActionButton(
                    onClick = { 
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = Color.White,
                    contentColor = AccentBlue,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, "Mi ubicación")
                }
                
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                    color = PrimaryBlue.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Toca el mapa para fijar tu ubicación exacta",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                
                // Selector de Tipo de Servicio
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TipoServicioCard(
                        title = "Pedir Efectivo",
                        icon = Icons.AutoMirrored.Filled.CallMade,
                        isSelected = tipo == "ENTREGA",
                        modifier = Modifier.weight(1f),
                        onClick = { tipo = "ENTREGA" }
                    )
                    TipoServicioCard(
                        title = "Entregar",
                        icon = Icons.AutoMirrored.Filled.CallReceived,
                        isSelected = tipo == "RECOLECCION",
                        modifier = Modifier.weight(1f),
                        onClick = { tipo = "RECOLECCION" }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Punto de Encuentro") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = AccentBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        
                        val formatter = remember { DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' }) }
                        val visualMonto = remember(rawMonto) { 
                            if(rawMonto.isEmpty()) "" 
                            else try { formatter.format(rawMonto.toLong()) } catch(e:Exception) { rawMonto }
                        }
                        
                        OutlinedTextField(
                            value = visualMonto,
                            onValueChange = { input ->
                                val clean = input.replace(".", "").filter { it.isDigit() }
                                if (clean.length <= 10) rawMonto = clean
                            },
                            label = { Text("Monto COP") },
                            prefix = { Text("$ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        val montoVal = rawMonto.toDoubleOrNull() ?: 0.0
                        val uid = authRepository.getCurrentUserId()
                        if (uid != null && montoVal > 0 && direccion.isNotBlank()) {
                            if (tipo == "RECOLECCION" && (userData?.saldo ?: 0.0) < montoVal) {
                                Toast.makeText(context, "Saldo insuficiente para entregar", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.crearSolicitud(Solicitud(
                                usuarioId = uid,
                                usuarioNombre = userData?.nombre,
                                tipoServicio = if(tipo == "ENTREGA") "BASE" else "RECOLECCION",
                                monto = montoVal,
                                direccion = direccion,
                                latitudDestino = selectedLocation?.latitude ?: bogota.latitude,
                                longitudDestino = selectedLocation?.longitude ?: bogota.longitude
                            ))
                            showSuccessDialog = true
                        } else {
                            Toast.makeText(context, "Faltan datos requeridos (Ubicación y Monto)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("Confirmar Pedido", fontWeight = FontWeight.Black, fontSize = 17.sp)
                }
            }
        }
    }
}

private fun obtenerDireccion(context: android.content.Context, latLng: LatLng, onResult: (String) -> Unit) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= 33) {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    onResult(addresses[0].getAddressLine(0))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                onResult(addresses[0].getAddressLine(0))
            }
        }
    } catch (e: Exception) {
        onResult("Lat: ${latLng.latitude}, Lon: ${latLng.longitude}")
    }
}

@Composable
fun TipoServicioCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryBlue else Color.White),
        border = if (isSelected) null else BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (isSelected) Color.White else AccentBlue, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else TextPrimary, fontSize = 14.sp)
        }
    }
}
