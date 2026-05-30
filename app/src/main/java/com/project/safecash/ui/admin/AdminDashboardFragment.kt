package com.project.safecash.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.project.safecash.R
import com.project.safecash.databinding.FragmentAdminDashboardBinding
import kotlinx.coroutines.launch

/**
 * Fragmento que representa el panel de control del administrador.
 * Muestra el balance actual del centro de acopio y proporciona accesos rápidos
 * a la gestión de agentes operativos, usuarios y otros reportes.
 */
class AdminDashboardFragment : Fragment() {

    // Referencia al binding de la vista. Se usa un backing property (_binding) 
    // para cumplir con el ciclo de vida de los Fragments (evitar fugas de memoria).
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    // Inicialización del ViewModel mediante el delegado viewModels()
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el layout usando View Binding
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Iniciamos la observación de los datos del ViewModel
        observeViewModel()

        // Configuración de eventos de clic para las tarjetas de navegación
        binding.btnManageAgentes.setOnClickListener {
            // TODO: Implementar navegación a Gestión de Agentes Operativos
        }

        binding.btnManageUsers.setOnClickListener {
            // TODO: Implementar navegación a Gestión de Usuarios
        }
    }

    /**
     * Configura la recolección de flujos (Flows) desde el ViewModel para actualizar la UI en tiempo real.
     */
    private fun observeViewModel() {
        // Usamos viewLifecycleOwner para que la observación se detenga cuando la vista se destruya
        viewLifecycleOwner.lifecycleScope.launch {
            // Escuchamos los cambios en el balance del centro de acopio
            viewModel.acopioBalance.collect { balance ->
                // Usamos getString con el recurso y el valor para formatear el balance.
                // Esto resuelve el warning de interpolación y sigue las guías de Android.
                binding.tvAcopioBalance.text = getString(R.string.balance_format, balance)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberamos el binding para evitar fugas de memoria al destruir la vista
        _binding = null
    }
}
