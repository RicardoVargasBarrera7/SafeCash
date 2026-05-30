package com.project.safecash.ui.agente

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
import com.project.safecash.databinding.FragmentAgenteDashboardBinding
import com.project.safecash.ui.adapter.SolicitudAdapter
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Fragmento que representa el panel principal del Agente Operativo.
 * Muestra el saldo actual y la lista de tareas/solicitudes asignadas.
 */
class AgenteDashboardFragment : Fragment() {

    private var _binding: FragmentAgenteDashboardBinding? = null
    private val binding get() = _binding!!
    
    // Usamos el nuevo AgenteViewModel
    private val viewModel: AgenteViewModel by viewModels()
    private lateinit var adapter: SolicitudAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgenteDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnCierreTurno.setOnClickListener {
            // Navegación corregida a Cierre de Turno
            findNavController().navigate(R.id.action_agenteDashboardFragment_to_cierreTurnoFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = SolicitudAdapter { solicitud ->
            // Navegación al detalle del servicio
            val action = AgenteDashboardFragmentDirections.actionAgenteDashboardFragmentToDetalleServicioFragment(solicitud.id)
            findNavController().navigate(action)
        }
        binding.rvAgenteTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAgenteTasks.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.agenteData.collect { agente ->
                agente?.let {
                    // Actualización del saldo con el formato centralizado
                    binding.tvAgenteBalance.text = getString(R.string.balance_format, it.saldoActual)
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
