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

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.login(email, pass)
            } else {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        observeState()
    }

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

    private fun navigateToDashboard(role: String) {
        when (role) {
            "ADMIN" -> findNavController().navigate(R.id.action_loginFragment_to_adminDashboardFragment)
            "ESCOLTA" -> findNavController().navigate(R.id.action_loginFragment_to_escoltaDashboardFragment)
            else -> findNavController().navigate(R.id.action_loginFragment_to_userDashboardFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
