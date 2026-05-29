package com.project.safecash.data.model

import com.google.firebase.Timestamp

data class CierreTurno(
    val id: String = "",
    val escoltaId: String = "",
    val saldoDevuelto: Double = 0.0,
    val fecha: Timestamp = Timestamp.now()
)
