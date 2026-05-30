package com.project.safecash.data.model

import com.google.firebase.Timestamp

/**
 * Modelo que representa el registro de un cierre de turno de un agente.
 */
data class CierreTurno(
    val id: String = "",
    val agenteId: String = "", // Cambiado de escoltaId a agenteId
    val saldoDevuelto: Double = 0.0,
    val fecha: Timestamp = Timestamp.now()
)
