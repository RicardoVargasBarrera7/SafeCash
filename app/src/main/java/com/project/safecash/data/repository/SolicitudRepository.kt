package com.project.safecash.data.repository

import com.project.safecash.data.model.Solicitud
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class SolicitudRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun crearSolicitud(solicitud: Solicitud): Result<Unit> {
        return try {
            val docRef = firestore.collection("solicitudes").document()
            val nuevaSolicitud = solicitud.copy(id = docRef.id)
            docRef.set(nuevaSolicitud).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSolicitudesUsuario(uid: String) = 
        firestore.collection("solicitudes")
            .whereEqualTo("usuarioId", uid)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)

    fun getSolicitudesPendientes() = 
        firestore.collection("solicitudes")
            .whereEqualTo("estado", "PENDIENTE")
            .orderBy("fechaCreacion", Query.Direction.ASCENDING)

    suspend fun actualizarEstadoSolicitud(id: String, nuevoEstado: String): Result<Unit> {
        return try {
            firestore.collection("solicitudes").document(id)
                .update("estado", nuevoEstado).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
