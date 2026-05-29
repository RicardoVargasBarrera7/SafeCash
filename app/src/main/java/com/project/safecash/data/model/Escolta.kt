package com.project.safecash.data.model

data class Escolta(
    val id: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val baseAsignada: Double = 0.0,
    val saldoActual: Double = 0.0,
    val estado: String = "ACTIVO" // "ACTIVO", "EN_SERVICIO", "INACTIVO"
)
