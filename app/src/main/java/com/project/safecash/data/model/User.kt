package com.project.safecash.data.model

/**
 * Modelo de usuario único para SafeCash.
 * Roles permitidos: ADMIN, AGENTE, USUARIO.
 */
data class User(
    val id: String = "", // Gestionado manualmente para evitar conflictos con Firestore
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val direccionPrincipal: String = "",
    val saldo: Double = 0.0, // Saldo digital para USUARIO
    val saldoActual: Double = 0.0, // Efectivo en mano para AGENTE
    val baseAsignada: Double = 0.0, // Base entregada por ADMIN
    val rol: String = "USUARIO",
    val estado: String = "ACTIVO"
)
