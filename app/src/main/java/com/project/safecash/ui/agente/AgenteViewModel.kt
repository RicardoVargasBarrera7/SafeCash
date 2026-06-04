package com.project.safecash.ui.agente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.model.User
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.data.repository.SolicitudRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * ViewModel para el Agente Operativo.
 */
class AgenteViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val solicitudRepository = SolicitudRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private val _tareasAsignadas = MutableStateFlow<List<Solicitud>>(emptyList())
    val tareasAsignadas: StateFlow<List<Solicitud>> = _tareasAsignadas.asStateFlow()

    private val _tareasDisponibles = MutableStateFlow<List<Solicitud>>(emptyList())
    val tareasDisponibles: StateFlow<List<Solicitud>> = _tareasDisponibles.asStateFlow()

    private val _showNotification = MutableStateFlow(false)
    val showNotification: StateFlow<Boolean> = _showNotification.asStateFlow()
    
    private val _notificacionMensaje = MutableStateFlow<String?>(null)
    val notificacionMensaje: StateFlow<String?> = _notificacionMensaje

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var lastBalance: Double = -1.0

    init {
        loadAgenteData()
        listenToPendingTasks()
    }

    private fun loadAgenteData() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            firestore.collection("usuarios").document(uid).addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    if (lastBalance != -1.0 && user.saldoActual > lastBalance) {
                        _showNotification.value = true
                        _notificacionMensaje.value = "Has recibido una nueva base de efectivo."
                    }
                    lastBalance = user.saldoActual
                    _userData.value = user
                }
            }

            // Escuchar tareas asignadas o finalizadas HOY
            firestore.collection("solicitudes")
                .whereEqualTo("agenteId", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val allTasks = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Solicitud::class.java)?.apply { id = doc.id }
                        }
                        
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val hoy = cal.time
                        
                        // En el dashboard solo vemos tareas ACTIVAS o FINALIZADAS de HOY
                        _tareasAsignadas.value = allTasks.filter { tarea ->
                            tarea.estado != "FINALIZADA" || tarea.fechaCreacion.toDate().after(hoy)
                        }.sortedByDescending { it.fechaCreacion }
                    }
                }
        }
    }

    private fun listenToPendingTasks() {
        firestore.collection("solicitudes")
            .whereEqualTo("estado", "PENDIENTE")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val nuevas = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Solicitud::class.java)?.apply { id = doc.id }
                    }
                    
                    // Notificar si hay nuevas tareas disponibles y el agente está en servicio
                    if (_userData.value?.estado == "EN_SERVICIO" && nuevas.size > _tareasDisponibles.value.size) {
                        _showNotification.value = true
                        _notificacionMensaje.value = "Hay nuevas solicitudes disponibles en el sector."
                    }
                    
                    _tareasDisponibles.value = nuevas
                }
            }
    }

    fun clearNotification() {
        _showNotification.value = false
        _notificacionMensaje.value = null
    }

    fun toggleDisponibilidad() {
        val uid = authRepository.getCurrentUserId() ?: return
        val currentEstado = _userData.value?.estado ?: "ACTIVO"
        val nuevoEstado = if (currentEstado == "EN_SERVICIO") "ACTIVO" else "EN_SERVICIO"
        
        viewModelScope.launch {
            try {
                firestore.collection("usuarios").document(uid).update("estado", nuevoEstado).await()
            } catch (e: Exception) {
                _error.value = "Error al cambiar estado"
            }
        }
    }

    fun aceptarTarea(solicitud: Solicitud, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = authRepository.getCurrentUserId() ?: return
        if (solicitud.tipoServicio == "BASE") {
            val saldoAgente = _userData.value?.saldoActual ?: 0.0
            if (saldoAgente < solicitud.monto) {
                onError("No tienes base suficiente ($ ${saldoAgente}) para aceptar este servicio de $ ${solicitud.monto}")
                return
            }
        }

        viewModelScope.launch {
            try {
                firestore.collection("solicitudes").document(solicitud.id).update(
                    "agenteId", uid,
                    "agenteNombre", _userData.value?.nombre ?: "Agente",
                    "estado", "ASIGNADA"
                ).await()
                onSuccess()
            } catch (e: Exception) {
                onError("Error al aceptar tarea")
            }
        }
    }

    fun actualizarEstado(solicitud: Solicitud, nuevoEstado: String, onError: (String) -> Unit) {
        if (nuevoEstado == "ENTREGADO" && solicitud.tipoServicio == "BASE") {
            val saldoAgente = _userData.value?.saldoActual ?: 0.0
            if (saldoAgente < solicitud.monto) {
                onError("Fondos insuficientes en mano para completar la entrega.")
                return
            }
        }

        viewModelScope.launch {
            try {
                solicitudRepository.actualizarEstadoSolicitud(solicitud.id, nuevoEstado).onSuccess {
                }.onFailure {
                    onError("No se pudo actualizar el estado")
                }
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
