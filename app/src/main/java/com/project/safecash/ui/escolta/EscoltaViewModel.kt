package com.project.safecash.ui.escolta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.Escolta
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.data.repository.SolicitudRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EscoltaViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val solicitudRepository = SolicitudRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _escoltaData = MutableStateFlow<Escolta?>(null)
    val escoltaData: StateFlow<Escolta?> = _escoltaData

    private val _tareasAsignadas = MutableStateFlow<List<Solicitud>>(emptyList())
    val tareasAsignadas: StateFlow<List<Solicitud>> = _tareasAsignadas

    init {
        loadEscoltaData()
    }

    private fun loadEscoltaData() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            // Escuchar cambios en los datos del escolta
            firestore.collection("escoltas").document(uid).addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _escoltaData.value = snapshot.toObject(Escolta::class.java)
                }
            }

            // Escuchar solicitudes asignadas a este escolta
            firestore.collection("solicitudes")
                .whereEqualTo("escoltaId", uid)
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
