package com.project.safecash.ui.solicitud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SolicitudViewModel : ViewModel() {
    private val repository = SolicitudRepository()

    private val _solicitudState = MutableStateFlow<SolicitudState>(SolicitudState.Idle)
    val solicitudState: StateFlow<SolicitudState> = _solicitudState

    fun crearSolicitud(solicitud: Solicitud) {
        viewModelScope.launch {
            _solicitudState.value = SolicitudState.Loading
            val result = repository.crearSolicitud(solicitud)
            result.onSuccess {
                _solicitudState.value = SolicitudState.Success
            }.onFailure {
                _solicitudState.value = SolicitudState.Error(it.message ?: "Error al crear la solicitud")
            }
        }
    }

    sealed class SolicitudState {
        object Idle : SolicitudState()
        object Loading : SolicitudState()
        object Success : SolicitudState()
        data class Error(val message: String) : SolicitudState()
    }
}
