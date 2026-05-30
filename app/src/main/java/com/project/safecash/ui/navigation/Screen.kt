package com.project.safecash.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object UserDashboard : Screen("user_dashboard")
    data object AgenteDashboard : Screen("agente_dashboard")
    data object AdminDashboard : Screen("admin_dashboard")
    data object CrearSolicitud : Screen("crear_solicitud")
    data object DetalleServicio : Screen("detalle_servicio/{solicitudId}") {
        fun createRoute(solicitudId: String) = "detalle_servicio/$solicitudId"
    }
    data object CierreTurno : Screen("cierre_turno")
}
