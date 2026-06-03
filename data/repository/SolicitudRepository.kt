package com.project.safecash.data.repository

import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.model.Movimiento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class SolicitudRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun crearSolicitud(solicitud: Solicitud): Result<Unit> {
        return try {
            val docRef = db.collection("solicitudes").document()
            val nuevaSolicitud = solicitud.copy(
                id = docRef.id,
                fechaCreacion = Timestamp.now(),
                estado = "PENDIENTE"
            )
            docRef.set(nuevaSolicitud).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSolicitudesUsuario(uid: String) = 
        db.collection("solicitudes")
            .whereEqualTo("usuarioId", uid)

    fun getSolicitudesPendientes() = 
        db.collection("solicitudes")
            .whereEqualTo("estado", "PENDIENTE")

    suspend fun actualizarEstadoSolicitud(id: String, nuevoEstado: String): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "estado" to nuevoEstado
            )
            
            val timestampField = when (nuevoEstado) {
                "ASIGNADA" -> "fechaAsignacion"
                "EN_CAMINO" -> "fechaEnCamino"
                "EN_PROCESO" -> "fechaEnProceso"
                "ENTREGADO" -> "fechaEntregado"
                "FINALIZADA" -> "fechaFinalizacion"
                else -> null
            }
            
            if (timestampField != null) {
                updates[timestampField] = FieldValue.serverTimestamp()
            }
            
            db.collection("solicitudes").document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmarRecepcionUsuario(solicitud: Solicitud): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val solicitudRef = db.collection("solicitudes").document(solicitud.id)
                val usuarioRef = db.collection("usuarios").document(solicitud.usuarioId)
                val agenteRef = db.collection("usuarios").document(solicitud.agenteId!!)

                transaction.update(solicitudRef, "estado", "FINALIZADA")
                transaction.update(solicitudRef, "fechaFinalizacion", FieldValue.serverTimestamp())

                val monto = solicitud.monto
                // Lógica de saldo: 
                // RETIRO (User recibe efectivo): Saldo digital sube? O baja si es como un cajero?
                // El usuario dijo "se le veria el saldo afectado". 
                // Normalmente en estas apps: 
                // RETIRO: Usuario recibe cash, se le descuenta de su saldo digital.
                // DEPOSITO: Usuario entrega cash, se le suma a su saldo digital.
                
                if (solicitud.tipoServicio == "DEPÓSITO") {
                    transaction.update(usuarioRef, "saldo", FieldValue.increment(monto))
                    transaction.update(agenteRef, "saldoActual", FieldValue.increment(monto))
                } else {
                    transaction.update(usuarioRef, "saldo", FieldValue.increment(-monto))
                    transaction.update(agenteRef, "saldoActual", FieldValue.increment(-monto))
                }

                val movRef = db.collection("movimientos").document()
                val movimiento = Movimiento(
                    id = movRef.id,
                    tipoMovimiento = solicitud.tipoServicio,
                    monto = monto,
                    origen = if (solicitud.tipoServicio == "DEPÓSITO") "AGENTE" else "USUARIO",
                    destino = if (solicitud.tipoServicio == "DEPÓSITO") "USUARIO" else "AGENTE",
                    usuarioId = solicitud.usuarioId,
                    agenteId = solicitud.agenteId,
                    solicitudId = solicitud.id,
                    fecha = Timestamp.now()
                )
                transaction.set(movRef, movimiento)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
