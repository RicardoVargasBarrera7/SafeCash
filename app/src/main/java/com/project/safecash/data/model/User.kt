package com.project.safecash.data.model

data class User(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val direccionPrincipal: String = "",
    val saldo: Double = 0.0,
    val rol: String = "", // "ADMIN", "AGENTE_OPERATIVO", "CLIENTE"
    val estado: String = "ACTIVO"
)
