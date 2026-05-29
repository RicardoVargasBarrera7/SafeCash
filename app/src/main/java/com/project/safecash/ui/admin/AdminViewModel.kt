package com.project.safecash.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.CentroAcopio
import com.project.safecash.data.model.Solicitud
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _acopioBalance = MutableStateFlow(0.0)
    val acopioBalance: StateFlow<Double> = _acopioBalance

    private val _serviciosActivos = MutableStateFlow<List<Solicitud>>(emptyList())
    val serviciosActivos: StateFlow<List<Solicitud>> = _serviciosActivos

    init {
        listenToAcopio()
        listenToActiveServices()
    }

    private fun listenToAcopio() {
        firestore.collection("centroAcopio").document("principal")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val acopio = snapshot.toObject(CentroAcopio::class.java)
                    _acopioBalance.value = acopio?.saldoDisponible ?: 0.0
                }
            }
    }

    private fun listenToActiveServices() {
        firestore.collection("solicitudes")
            .whereIn("estado", listOf("PENDIENTE", "ASIGNADA", "EN_PROCESO"))
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _serviciosActivos.value = snapshot.toObjects(Solicitud::class.java)
                }
            }
    }
}
