package com.project.safecash.ui.agente

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
import com.project.safecash.data.model.CierreTurno
import com.project.safecash.databinding.FragmentCierreTurnoBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Fragmento para que el Agente Operativo realice el cierre de su turno
 * y devuelva el saldo acumulado al Centro de Acopio.
 */
class CierreTurnoFragment : Fragment() {

    private var _binding: FragmentCierreTurnoBinding? = null
    private val binding get() = _binding!!
    
    // Usamos el AgenteViewModel compartido
    private val viewModel: AgenteViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCierreTurnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        binding.btnConfirmarCierre.setOnClickListener {
            realizarCierre()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.agenteData.collect { agente ->
                agente?.let {
                    // Mostrar el saldo actual que debe ser devuelto
                    binding.tvSaldoDevolver.text = getString(R.string.balance_format, it.saldoActual)
                }
            }
        }
    }

    /**
     * Lógica de negocio para cerrar el turno:
     * 1. Crea un registro en 'cierresTurno'.
     * 2. Reinicia el saldo del agente a 0.
     * 3. Incrementa el saldo disponible en el Centro de Acopio.
     */
    private fun realizarCierre() {
        val agente = viewModel.agenteData.value ?: return
        val saldoADevolver = agente.saldoActual

        if (saldoADevolver < 0) {
            Toast.makeText(requireContext(), "Saldo inválido para cierre", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmarCierre.isEnabled = false

        lifecycleScope.launch {
            try {
                firestore.runTransaction { transaction ->
                    // 1. Crear registro de cierre
                    val cierreRef = firestore.collection("cierresTurno").document()
                    val cierre = CierreTurno(
                        id = cierreRef.id,
                        agenteId = agente.id, // Actualizado de escoltaId
                        saldoDevuelto = saldoADevolver
                    )
                    transaction.set(cierreRef, cierre)

                    // 2. Actualizar datos del agente
                    val agenteRef = firestore.collection("agentes").document(agente.id)
                    transaction.update(agenteRef, "saldoActual", 0.0)
                    transaction.update(agenteRef, "estado", "INACTIVO")

                    // 3. Actualizar saldo del Centro de Acopio Principal
                    val acopioRef = firestore.collection("centroAcopio").document("principal")
                    transaction.update(acopioRef, "saldoDisponible", FieldValue.increment(saldoADevolver))
                    transaction.update(acopioRef, "fechaActualizacion", Timestamp.now())
                }.await()

                Toast.makeText(requireContext(), "Cierre de turno exitoso", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnConfirmarCierre.isEnabled = true
                Toast.makeText(requireContext(), "Error al realizar cierre: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
