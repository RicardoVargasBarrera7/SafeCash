package com.project.safecash.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.safecash.R
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

/**
 * Fragmento de pantalla de carga (Splash).
 * Se encarga de verificar si hay una sesión activa y redirigir al usuario
 * a su panel correspondiente según su rol (Admin, Agente o Usuario).
 */
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Esperamos 2 segundos para mostrar el logo antes de redirigir
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000)
    }

    /**
     * Comprueba si el usuario tiene una sesión iniciada en Firebase.
     */
    private fun checkUserSession() {
        val uid = authRepository.getCurrentUserId()
        if (uid != null) {
            // Si hay sesión, buscamos el rol en Firestore para saber a qué dashboard ir
            viewLifecycleOwner.lifecycleScope.launch {
                val role = authRepository.getUserRole(uid)
                navigateToDashboard(role)
            }
        } else {
            // Si no hay sesión, enviamos al usuario a la pantalla de Login
            if (isAdded) {
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
    }

    /**
     * Navega al dashboard específico basado en el rol del usuario.
     * Se actualizó de 'escolta' a 'agente' para mantener la consistencia.
     */
    private fun navigateToDashboard(role: String?) {
        if (!isAdded) return
        val actionId = when (role) {
            "ADMIN" -> R.id.action_splashFragment_to_adminDashboardFragment
            // Se corrigió el ID de la acción: ahora usa agenteDashboardFragment
            "ESCOLTA" -> R.id.action_splashFragment_to_agenteDashboardFragment
            else -> R.id.action_splashFragment_to_userDashboardFragment
        }
        findNavController().navigate(actionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberamos el binding para evitar fugas de memoria
        _binding = null
    }
}
