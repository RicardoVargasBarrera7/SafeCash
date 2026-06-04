package com.project.safecash.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.CentroAcopio
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _acopioBalance = MutableStateFlow(0.0)
    val acopioBalance: StateFlow<Double> = _acopioBalance

    private val _serviciosActivos = MutableStateFlow<List<Solicitud>>(emptyList())
    val serviciosActivos: StateFlow<List<Solicitud>> = _serviciosActivos

    private val _agentesMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val agentesMap: StateFlow<Map<String, String>> = _agentesMap

    init {
        listenToAcopio()
        listenToActiveServices()
        fetchAgentes()
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
            .whereIn("estado", listOf("PENDIENTE", "ASIGNADA", "EN_CAMINO", "EN_PROCESO"))
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _serviciosActivos.value = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Solicitud::class.java)?.apply { id = doc.id }
                    }
                }
            }
    }

    private fun fetchAgentes() {
        firestore.collection("usuarios")
            .whereEqualTo("rol", "AGENTE")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val map = snapshot.documents.associate { doc ->
                        doc.id to (doc.getString("nombre") ?: "Agente Desconocido")
                    }
                    _agentesMap.value = map
                }
            }
    }

    fun asignarBaseAAgente(agenteId: String, monto: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                
                // 1. Restar del acopio (Usamos SET con MERGE por si el documento no existe)
                val acopioRef = firestore.collection("centroAcopio").document("principal")
                batch.set(acopioRef, mapOf(
                    "saldoDisponible" to FieldValue.increment(-monto)
                ), SetOptions.merge())
                
                // 2. Sumar al agente (saldoActual y baseAsignada)
                val agenteRef = firestore.collection("usuarios").document(agenteId)
                batch.update(agenteRef, 
                    "saldoActual", FieldValue.increment(monto),
                    "baseAsignada", FieldValue.increment(monto)
                )
                
                // 3. Registrar movimiento
                val movimientoRef = firestore.collection("movimientos").document()
                val movimiento = mapOf(
                    "tipo" to "ASIGNACION_BASE",
                    "monto" to monto,
                    "agenteId" to agenteId,
                    "fecha" to FieldValue.serverTimestamp(),
                    "descripcion" to "Asignación de base por Administrador"
                )
                batch.set(movimientoRef, movimiento)
                
                batch.commit().await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al asignar base")
            }
        }
    }
}
