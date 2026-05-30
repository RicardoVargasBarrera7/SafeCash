package com.project.safecash.data.model

import com.google.firebase.Timestamp

/**
 * Modelo que representa una solicitud de servicio en el sistema.
 */
data class Solicitud(
    val id: String = "",
    val usuarioId: String = "",
    val agenteId: String? = null, // Cambiado de escoltaId a agenteId
    val tipoServicio: String = "", // "RECOLECCION", "ENTREGA"
    val monto: Double = 0.0,
    val direccion: String = "",
    val observaciones: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val estado: String = "PENDIENTE" // "PENDIENTE", "ASIGNADA", "EN_PROCESO", "FINALIZADA", "CANCELADA"
)
