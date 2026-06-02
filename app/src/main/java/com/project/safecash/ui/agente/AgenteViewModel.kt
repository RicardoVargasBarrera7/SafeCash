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
import kotlinx.coroutines.launch

/**
 * ViewModel centralizado para la lógica del Agente Operativo.
 * Gestiona datos del perfil (colección 'usuarios'), tareas asignadas y tareas disponibles.
 */
class AgenteViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val solicitudRepository = SolicitudRepository()
    private val firestore = FirebaseFirestore.getInstance()

    // Perfil del agente — se mapea desde la colección 'usuarios' usando el modelo User
    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _tareasAsignadas = MutableStateFlow<List<Solicitud>>(emptyList())
    val tareasAsignadas: StateFlow<List<Solicitud>> = _tareasAsignadas

    private val _tareasDisponibles = MutableStateFlow<List<Solicitud>>(emptyList())
    val tareasDisponibles: StateFlow<List<Solicitud>> = _tareasDisponibles

    init {
        loadAgenteData()
        listenToPendingTasks()
    }

    private fun loadAgenteData() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            // Perfil del agente desde la colección unificada 'usuarios'
            firestore.collection("usuarios").document(uid).addSnapshotListener { snapshot, _ ->
                _userData.value = snapshot?.toObject(User::class.java)
            }

            // Tareas que ya tiene asignadas este agente
            firestore.collection("solicitudes")
                .whereEqualTo("agenteId", uid)
                .whereIn("estado", listOf("ASIGNADA", "EN_PROCESO"))
                .addSnapshotListener { snapshot, _ ->
                    _tareasAsignadas.value = snapshot?.toObjects(Solicitud::class.java) ?: emptyList()
                }
        }
    }

    private fun listenToPendingTasks() {
        // Tareas en el mercado (PENDIENTE) que cualquier agente puede tomar
        firestore.collection("solicitudes")
            .whereEqualTo("estado", "PENDIENTE")
            .addSnapshotListener { snapshot, _ ->
                _tareasDisponibles.value = snapshot?.toObjects(Solicitud::class.java) ?: emptyList()
            }
    }

    fun aceptarTarea(solicitudId: String) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            firestore.collection("solicitudes").document(solicitudId).update(
                "agenteId", uid,
                "estado", "ASIGNADA"
            )
        }
    }

    fun actualizarEstado(solicitudId: String, nuevoEstado: String) {
        viewModelScope.launch {
            solicitudRepository.actualizarEstadoSolicitud(solicitudId, nuevoEstado)
        }
    }
}
