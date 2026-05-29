package com.project.safecash.data.model

import com.google.firebase.Timestamp

data class Solicitud(
    val id: String = "",
    val usuarioId: String = "",
    val escoltaId: String? = null,
    val tipoServicio: String = "", // "RECOLECCION", "ENTREGA"
    val monto: Double = 0.0,
    val direccion: String = "",
    val observaciones: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val estado: String = "PENDIENTE" // "PENDIENTE", "ASIGNADA", "EN_PROCESO", "FINALIZADA", "CANCELADA"
)
