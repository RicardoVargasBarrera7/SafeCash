package com.project.safecash.data.model

import com.google.firebase.Timestamp

data class Movimiento(
    val id: String = "",
    val usuarioId: String = "",
    val solicitudId: String = "",
    val tipo: String = "", // "ENTREGA", "RECOLECCION"
    val monto: Double = 0.0,
    val fecha: Timestamp = Timestamp.now()
)
