package com.project.safecash.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Usuario / Cliente
    data object UserDashboard : Screen("user_dashboard")
    data object CrearSolicitud : Screen("crear_solicitud")

    // Agente Operativo
    data object AgenteDashboard : Screen("agente_dashboard")
    data object CierreTurno : Screen("cierre_turno")
    data object TareasDisponibles : Screen("tareas_disponibles")
    data object DetalleServicio : Screen("detalle_servicio/{solicitudId}") {
        fun createRoute(solicitudId: String) = "detalle_servicio/$solicitudId"
    }

    // Administrador
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminAgentes : Screen("admin_agentes")
    data object AdminUsuarios : Screen("admin_usuarios")
    data object AdminMovimientos : Screen("admin_movimientos")
}
