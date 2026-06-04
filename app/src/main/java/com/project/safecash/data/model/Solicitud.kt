package com.project.safecash.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Modelo de solicitud optimizado para Firebase.
 * Incluye registros de tiempo para cada etapa del proceso.
 */
@IgnoreExtraProperties
data class Solicitud(
    var id: String = "",
    var usuarioId: String = "",
    var agenteId: String? = null,
    var agenteNombre: String? = null,
    var usuarioNombre: String? = null,
    var tipoServicio: String = "", // "RETIRO" o "DEPÓSITO"
    var monto: Double = 0.0,
    var direccion: String = "",
    var latitudDestino: Double = 0.0,
    var longitudDestino: Double = 0.0,
    var observaciones: String = "",
    var fechaCreacion: Timestamp = Timestamp.now(),
    var fechaAsignacion: Timestamp? = null,
    var fechaEnCamino: Timestamp? = null,
    var fechaEnProceso: Timestamp? = null,
    var fechaEntregado: Timestamp? = null,
    var fechaFinalizacion: Timestamp? = null, // Cuando el usuario confirma
    var estado: String = "PENDIENTE" // PENDIENTE, ASIGNADA, EN_CAMINO, EN_PROCESO, ENTREGADO, FINALIZADA
)
