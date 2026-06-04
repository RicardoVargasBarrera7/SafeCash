package com.project.safecash.ui.agente

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.model.User
import com.project.safecash.ui.theme.*
import com.project.safecash.ui.user.ServiceTimeline
import com.project.safecash.ui.user.UserViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleServicioScreen(
    navController: NavController,
    solicitudId: String,
    agenteViewModel: AgenteViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var solicitud by remember { mutableStateOf<Solicitud?>(null) }
    var userSolicitante by remember { mutableStateOf<User?>(null) }
    var agenteAsignado by remember { mutableStateOf<User?>(null) }
    val currentUser by userViewModel.userData.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cargar datos de sesión al entrar
    LaunchedEffect(Unit) {
        userViewModel.loadData()
    }

    LaunchedEffect(solicitudId) {
        firestore.collection("solicitudes").document(solicitudId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val item = snapshot.toObject(Solicitud::class.java)?.apply { id = snapshot.id }
                    solicitud = item
                    
                    // Cargar perfiles involucrados
                    item?.usuarioId?.let { uid ->
                        firestore.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { userSolicitante = it.toObject(User::class.java) }
                    }
                    item?.agenteId?.let { aid ->
                        firestore.collection("usuarios").document(aid).get()
                            .addOnSuccessListener { agenteAsignado = it.toObject(User::class.java) }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val isAgentView = currentUser?.id == solicitud?.agenteId && currentUser?.rol == "AGENTE"
                    Text(
                        text = if (isAgentView) "Misión de Entrega" else "Estado de mi Pedido",
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        solicitud?.let { item ->
            val currentUserId = currentUser?.id ?: ""
            // Lógica de roles blindada para evitar mezcla de pantallas
            val isRequester = currentUserId == item.usuarioId && currentUser?.rol == "USUARIO"
            val isAssignedAgent = currentUserId == item.agenteId && currentUser?.rol == "AGENTE"

            Box(modifier = Modifier.fillMaxSize()) {
                // Mapa interactivo
                val destination = LatLng(item.latitudDestino, item.longitudDestino)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(destination, 16f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
                ) {
                    Marker(state = MarkerState(position = destination), title = "Lugar de encuentro")
                }

                // GPS solo para el Agente
                if (isAssignedAgent) {
                    FloatingActionButton(
                        onClick = {
                            val uri = Uri.parse("google.navigation:q=${item.latitudDestino},${item.longitudDestino}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                            try { context.startActivity(mapIntent) } catch (e: Exception) { }
                        },
                        modifier = Modifier.align(Alignment.TopEnd).padding(padding).padding(16.dp),
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Navigation, "GPS")
                    }
                }

                // Panel de Información Inferior
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .heightIn(min = 350.dp, max = 550.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    text = if (item.tipoServicio == "RECOLECCION") "RECAUDO DIGITAL" else "PEDIDO DE EFECTIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if(item.tipoServicio == "RECOLECCION") ErrorRed else SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                val formatter = DecimalFormat("$ #,###", DecimalFormatSymbols(Locale.getDefault()).apply { groupingSeparator = '.' })
                                Text(text = formatter.format(item.monto), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            }
                            Surface(
                                color = when(item.estado) {
                                    "ENTREGADO", "FINALIZADA" -> SuccessGreen.copy(0.1f)
                                    "PENDIENTE" -> WarningOrange.copy(0.1f)
                                    else -> AccentBlue.copy(0.1f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = item.estado, 
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = if(item.estado == "ENTREGADO" || item.estado == "FINALIZADA") SuccessGreen else AccentBlue
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = BorderLight)
                        Spacer(Modifier.height(16.dp))

                        // Información de Contacto Inversa: El usuario ve al agente y viceversa
                        val contactLabel = if (isAssignedAgent) "CLIENTE SOLICITANTE" else "TU ASISTENTE ASIGNADO"
                        val contactUser = if (isAssignedAgent) userSolicitante else agenteAsignado
                        
                        Text(contactLabel, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        
                        if (contactUser != null) {
                            UserContactCardItem(contactUser)
                        } else {
                            Text(
                                text = if(isAssignedAgent) "Cargando cliente..." else "Buscando asistente para tu pedido...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("ESTADO DEL PROCESO", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        ServiceTimeline(solicitud = item)

                        Spacer(Modifier.height(32.dp))

                        // BOTONES DE ACCIÓN SEGÚN ROL
                        if (isAssignedAgent) {
                            // Botones para el Agente
                            AgenteActionButtons(item, agenteViewModel) {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            }
                        } else if (isRequester && item.estado == "ENTREGADO") {
                            // Botón para el Usuario: Aceptar que recibió/entregó el dinero
                            val actionText = if(item.tipoServicio == "BASE") "HE RECIBIDO EL DINERO" else "HE ENTREGADO EL DINERO"
                            Button(
                                onClick = {
                                    userViewModel.confirmarRecepcion(item) {
                                        Toast.makeText(context, "Transacción Finalizada", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Icon(Icons.Default.VerifiedUser, null)
                                Spacer(Modifier.width(12.dp))
                                Text(actionText, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        } else if (isRequester && item.estado != "FINALIZADA") {
                            Surface(
                                color = BackgroundLight,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Text(
                                    "El botón de confirmación aparecerá aquí en cuanto el agente marque la entrega.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
    }
}

@Composable
fun AgenteActionButtons(item: Solicitud, viewModel: AgenteViewModel, onError: (String) -> Unit) {
    val config = when (item.estado) {
        "ASIGNADA" -> Triple("Iniciar Recorrido", Icons.AutoMirrored.Filled.DirectionsBike, PrimaryBlue)
        "EN_CAMINO" -> Triple("Llegué al Punto", Icons.Default.WhereToVote, AccentBlue)
        "EN_PROCESO" -> Triple("Marcar como Entregado", Icons.Default.CheckCircle, SuccessGreen)
        else -> null
    }

    config?.let { (text, icon, color) ->
        Button(
            onClick = {
                val next = when (item.estado) {
                    "ASIGNADA" -> "EN_CAMINO"
                    "EN_CAMINO" -> "EN_PROCESO"
                    "EN_PROCESO" -> "ENTREGADO"
                    else -> item.estado
                }
                viewModel.actualizarEstado(item, next, onError)
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Text(text, fontWeight = FontWeight.Black, fontSize = 17.sp)
        }
    }
}

@Composable
fun UserContactCardItem(user: User) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PrimaryDark
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.1f)) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                Text(user.telefono, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.telefono}"))) },
                modifier = Modifier.background(SuccessGreen.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Phone, null, tint = SuccessGreen)
            }
        }
    }
}
