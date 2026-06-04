package com.project.safecash.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.model.User
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.data.repository.SolicitudRepository
import com.project.safecash.data.repository.UserRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class UserViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val solicitudRepository = SolicitudRepository()
    private var solicitudesListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _solicitudes = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes
    
    private val _notificacionLlegada = MutableStateFlow<String?>(null)
    val notificacionLlegada: StateFlow<String?> = _notificacionLlegada

    init {
        loadData()
    }

    fun loadData() {
        val uid = authRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            // Escuchar perfil del usuario en tiempo real para el saldo
            userListener?.remove()
            userListener = userRepository.getUserReference(uid).addSnapshotListener { snapshot, _ ->
                _userData.value = snapshot?.toObject(User::class.java)?.copy(id = uid)
            }

            // Escuchar solicitudes en tiempo real
            solicitudesListener?.remove()
            solicitudesListener = solicitudRepository.getSolicitudesUsuario(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Solicitud::class.java)?.copy(id = doc.id)
                        }
                        
                        // Sistema de Notificaciones Inteligentes
                        val oldSolicitudes = _solicitudes.value
                        items.forEach { newItem ->
                            val oldItem = oldSolicitudes.find { it.id == newItem.id }
                            if (oldItem != null && oldItem.estado != newItem.estado) {
                                when (newItem.estado) {
                                    "ASIGNADA" -> _notificacionLlegada.value = "¡Agente asignado! ${newItem.agenteNombre} va por tu pedido."
                                    "EN_CAMINO" -> _notificacionLlegada.value = "Tu agente está en camino."
                                    "EN_PROCESO" -> _notificacionLlegada.value = "¡Tu agente ha llegado al punto de encuentro!"
                                    "ENTREGADO" -> _notificacionLlegada.value = "El agente marcó la entrega. Por favor confirma."
                                }
                            }
                        }
                        
                        // FILTRO DASHBOARD: Movimientos HOY o ACTIVOS
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val hoy = cal.time
                        
                        val solicitudesDash = items.filter { 
                            it.estado != "FINALIZADA" || it.fechaCreacion.toDate().after(hoy) 
                        }.sortedByDescending { it.fechaCreacion }

                        _solicitudes.value = solicitudesDash
                    }
                }
        }
    }

    fun clearNotification() {
        _notificacionLlegada.value = null
    }

    fun confirmarRecepcion(solicitud: Solicitud, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = solicitudRepository.confirmarRecepcionUsuario(solicitud)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        solicitudesListener?.remove()
        userListener?.remove()
    }
}
