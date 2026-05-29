package com.project.safecash.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.model.User
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.data.repository.SolicitudRepository
import com.project.safecash.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val solicitudRepository = SolicitudRepository()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _solicitudes = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserData(uid)?.let {
                _userData.value = it
            }
            
            solicitudRepository.getSolicitudesUsuario(uid).addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _solicitudes.value = snapshot.toObjects(Solicitud::class.java)
                }
            }
        }
    }
}
