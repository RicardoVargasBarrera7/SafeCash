package com.project.safecash.data.model

import com.google.firebase.Timestamp

/**
 * Modelo para la trazabilidad completa de los movimientos de efectivo.
 */
data class Movimiento(
    val id: String = "",
    val tipoMovimiento: String = "", // RECOLECCION, ENTREGA
    val monto: Double = 0.0,
    val origen: String = "", // Nombre o Rol de quien entrega
    val destino: String = "", // Nombre o Rol de quien recibe
    val usuarioId: String = "",
    val agenteId: String? = null,
    val solicitudId: String = "",
    val fecha: Timestamp = Timestamp.now()
)
