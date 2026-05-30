package com.project.safecash.ui.agente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.Agente
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.data.repository.SolicitudRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la lógica de los Agentes Operativos.
 */
class AgenteViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val solicitudRepository = SolicitudRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _agenteData = MutableStateFlow<Agente?>(null)
    val agenteData: StateFlow<Agente?> = _agenteData

    private val _tareasAsignadas = MutableStateFlow<List<Solicitud>>(emptyList())
    val tareasAsignadas: StateFlow<List<Solicitud>> = _tareasAsignadas

    init {
        loadAgenteData()
    }

    private fun loadAgenteData() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            // Escuchar cambios en los datos del agente
            firestore.collection("agentes").document(uid).addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _agenteData.value = snapshot.toObject(Agente::class.java)
                }
            }

            // Escuchar solicitudes asignadas a este agente
            firestore.collection("solicitudes")
                .whereEqualTo("agenteId", uid)
                .whereIn("estado", listOf("ASIGNADA", "EN_PROCESO"))
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _tareasAsignadas.value = snapshot.toObjects(Solicitud::class.java)
                    }
                }
        }
    }

    fun actualizarEstado(solicitudId: String, nuevoEstado: String) {
        viewModelScope.launch {
            solicitudRepository.actualizarEstadoSolicitud(solicitudId, nuevoEstado)
        }
    }
}
