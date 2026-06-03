package com.project.safecash.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.User
import com.project.safecash.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona los estados de autenticación.
 */
class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, pass)
            result.onSuccess { uid ->
                val role = repository.getUserRole(uid)
                _authState.value = AuthState.Success(role ?: "USUARIO")
            }.onFailure {
                _authState.value = AuthState.Error(it.localizedMessage ?: "Error en login")
            }
        }
    }

    fun register(user: User, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(user, pass)
            result.onSuccess {
                _authState.value = AuthState.Success(user.rol)
            }.onFailure { e ->
                // Captura errores específicos de Firebase para dar mejor feedback
                val message = when (e) {
                    is FirebaseAuthException -> "Error de Firebase: ${e.message}"
                    else -> e.localizedMessage ?: "Error en el registro"
                }
                _authState.value = AuthState.Error(message)
            }
        }
    }

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data class Success(val role: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
