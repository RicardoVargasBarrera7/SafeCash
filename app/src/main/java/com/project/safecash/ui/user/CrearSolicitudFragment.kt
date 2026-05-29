package com.project.safecash.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.safecash.data.model.Solicitud
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.databinding.FragmentCrearSolicitudBinding
import com.project.safecash.ui.solicitud.SolicitudViewModel
import kotlinx.coroutines.launch

class CrearSolicitudFragment : Fragment() {

    private var _binding: FragmentCrearSolicitudBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SolicitudViewModel by viewModels()
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearSolicitudBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdown()
        
        binding.btnEnviar.setOnClickListener {
            val monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0
            val tipo = binding.spinnerTipo.text.toString()
            val direccion = binding.etDireccion.text.toString()
            val observaciones = binding.etObservaciones.text.toString()
            val uid = authRepository.getCurrentUserId() ?: return@setOnClickListener

            if (monto > 0 && tipo.isNotEmpty() && direccion.isNotEmpty()) {
                val solicitud = Solicitud(
                    usuarioId = uid,
                    tipoServicio = tipo,
                    monto = monto,
                    direccion = direccion,
                    observaciones = observaciones
                )
                viewModel.crearSolicitud(solicitud)
            } else {
                Toast.makeText(requireContext(), "Por favor, completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        observeState()
    }

    private fun setupDropdown() {
        val tipos = arrayOf("RECOLECCION", "ENTREGA")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tipos)
        binding.spinnerTipo.setAdapter(adapter)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.solicitudState.collect { state ->
                when (state) {
                    is SolicitudViewModel.SolicitudState.Loading -> {
                        binding.btnEnviar.isEnabled = false
                    }
                    is SolicitudViewModel.SolicitudState.Success -> {
                        Toast.makeText(requireContext(), "Solicitud enviada con éxito", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is SolicitudViewModel.SolicitudState.Error -> {
                        binding.btnEnviar.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
