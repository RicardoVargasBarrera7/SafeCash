package com.project.safecash.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.safecash.R
import com.project.safecash.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

/**
 * Fragmento encargado del inicio de sesión (Login).
 * Maneja la entrada de credenciales y redirige al usuario a su panel correspondiente según su rol.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el layout usando View Binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Acción al pulsar el botón de ingresar
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.login(email, pass)
            } else {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Navegación a la pantalla de registro
        binding.btnGoToRegister.setOnClickListener {
            // ID de acción corregido según el nav_graph.xml
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        observeState()
    }

    /**
     * Observa el estado de autenticación desde el ViewModel para reaccionar ante éxitos o errores.
     */
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthViewModel.AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is AuthViewModel.AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        navigateToDashboard(state.role)
                    }
                    is AuthViewModel.AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                }
            }
        }
    }

    /**
     * Redirige al panel correspondiente (Admin, Agente o Usuario) basándose en el rol.
     */
    private fun navigateToDashboard(role: String) {
        val actionId = when (role) {
            "ADMIN" -> R.id.action_loginFragment_to_adminDashboardFragment
            // Se actualizó de Escolta a Agente para mantener la consistencia
            "ESCOLTA" -> R.id.action_loginFragment_to_agenteDashboardFragment
            else -> R.id.action_loginFragment_to_userDashboardFragment
        }
        findNavController().navigate(actionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberamos el binding para evitar fugas de memoria
        _binding = null
    }
}
