package com.project.safecash.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.project.safecash.databinding.FragmentAdminDashboardBinding
import kotlinx.coroutines.launch
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        binding.btnManageEscoltas.setOnClickListener {
            // Navegar a Gestión de Escoltas
        }

        binding.btnManageUsers.setOnClickListener {
            // Navegar a Gestión de Usuarios
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.acopioBalance.collect { balance ->
                binding.tvAcopioBalance.text = String.format(Locale.getDefault(), "$ %.2f", balance)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
