package com.project.safecash.ui.escolta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.safecash.databinding.FragmentEscoltaDashboardBinding
import com.project.safecash.ui.adapter.SolicitudAdapter
import kotlinx.coroutines.launch
import java.util.Locale

class EscoltaDashboardFragment : Fragment() {

    private var _binding: FragmentEscoltaDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EscoltaViewModel by viewModels()
    private lateinit var adapter: SolicitudAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEscoltaDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnCierreTurno.setOnClickListener {
            // Navegar a Cierre de Turno
        }
    }

    private fun setupRecyclerView() {
        adapter = SolicitudAdapter { solicitud ->
            // Ir a detalle de servicio
        }
        binding.rvEscoltaTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEscoltaTasks.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.escoltaData.collect { escolta ->
                escolta?.let {
                    binding.tvEscoltaBalance.text = String.format(Locale.getDefault(), "$ %.2f", it.saldoActual)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tareasAsignadas.collect { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
