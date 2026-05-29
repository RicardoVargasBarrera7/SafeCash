package com.project.safecash.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.safecash.R
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.databinding.FragmentUserDashboardBinding
import com.project.safecash.ui.adapter.SolicitudAdapter
import kotlinx.coroutines.launch
import java.util.Locale

class UserDashboardFragment : Fragment() {

    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapter: SolicitudAdapter
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        binding.btnNewRequest.setOnClickListener {
            findNavController().navigate(R.id.action_userDashboardFragment_to_crearSolicitudFragment)
        }

        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        authRepository.logout()
        findNavController().navigate(R.id.action_splashFragment_to_loginFragment) // Adjust if necessary or use a global action
    }

    private fun setupRecyclerView() {
        adapter = SolicitudAdapter { solicitud ->
            // Navegar a detalle si es necesario
        }
        binding.rvUserHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserHistory.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userData.collect { user ->
                user?.let {
                    binding.tvWelcome.text = "Hola, ${it.nombre}"
                    binding.tvUserBalance.text = String.format(Locale.getDefault(), "$ %.2f", it.saldo)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.solicitudes.collect { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
