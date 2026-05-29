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
import com.project.safecash.data.model.User
import com.project.safecash.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val email = binding.etEmail.text.toString()
            val telefono = binding.etTelefono.text.toString()
            val direccion = binding.etDireccion.text.toString()
            val pass = binding.etPassword.text.toString()

            if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                val user = User(
                    nombre = nombre,
                    correo = email,
                    telefono = telefono,
                    direccionPrincipal = direccion,
                    rol = "USUARIO"
                )
                viewModel.register(user, pass)
            } else {
                Toast.makeText(requireContext(), "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthViewModel.AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                    }
                    is AuthViewModel.AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_userDashboardFragment)
                    }
                    is AuthViewModel.AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
