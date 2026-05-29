package com.project.safecash.data.model

import com.google.firebase.Timestamp

data class CentroAcopio(
    val saldoDisponible: Double = 0.0,
    val fechaActualizacion: Timestamp = Timestamp.now()
)
