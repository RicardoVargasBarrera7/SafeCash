package com.project.safecash.ui.escolta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.safecash.data.model.CierreTurno
import com.project.safecash.databinding.FragmentCierreTurnoBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class CierreTurnoFragment : Fragment() {

    private var _binding: FragmentCierreTurnoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EscoltaViewModel by viewModels()
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
            viewModel.escoltaData.collect { escolta ->
                escolta?.let {
                    binding.tvSaldoDevolver.text = String.format(Locale.getDefault(), "$ %.2f", it.saldoActual)
                }
            }
        }
    }

    private fun realizarCierre() {
        val escolta = viewModel.escoltaData.value ?: return
        val saldoADevolver = escolta.saldoActual

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
                        escoltaId = escolta.id,
                        saldoDevuelto = saldoADevolver
                    )
                    transaction.set(cierreRef, cierre)

                    // 2. Actualizar saldo de escolta a cero
                    val escoltaRef = firestore.collection("escoltas").document(escolta.id)
                    transaction.update(escoltaRef, "saldoActual", 0.0)
                    transaction.update(escoltaRef, "estado", "INACTIVO")

                    // 3. Actualizar saldo de Centro de Acopio
                    val acopioRef = firestore.collection("centroAcopio").document("principal")
                    transaction.update(acopioRef, "saldoDisponible", com.google.firebase.firestore.FieldValue.increment(saldoADevolver))
                    transaction.update(acopioRef, "fechaActualizacion", com.google.firebase.Timestamp.now())
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
