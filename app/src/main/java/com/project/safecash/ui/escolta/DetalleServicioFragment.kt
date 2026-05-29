package com.project.safecash.ui.escolta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.project.safecash.data.model.Solicitud
import com.project.safecash.databinding.FragmentDetalleServicioBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

class DetalleServicioFragment : Fragment() {

    private var _binding: FragmentDetalleServicioBinding? = null
    private val binding get() = _binding!!
    private val args: DetalleServicioFragmentArgs by navArgs()
    private val viewModel: EscoltaViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleServicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDetalle(args.solicitudId)

        binding.btnAceptarServicio.setOnClickListener {
            actualizarEstado("ASIGNADA")
        }

        binding.btnIniciarServicio.setOnClickListener {
            actualizarEstado("EN_PROCESO")
        }

        binding.btnFinalizarServicio.setOnClickListener {
            finalizarServicio()
        }
    }

    private fun loadDetalle(id: String) {
        firestore.collection("solicitudes").document(id).addSnapshotListener { snapshot, _ ->
            val solicitud = snapshot?.toObject(Solicitud::class.java)
            solicitud?.let { setupUI(it) }
        }
    }

    private fun setupUI(solicitud: Solicitud) {
        binding.tvTipoDetalle.text = solicitud.tipoServicio
        binding.tvMontoDetalle.text = String.format(Locale.getDefault(), "$ %.2f", solicitud.monto)
        binding.tvDireccionDetalle.text = solicitud.direccion
        binding.tvObservacionesDetalle.text = solicitud.observaciones.ifEmpty { "Sin observaciones" }

        // Control de visibilidad de botones según el estado
        binding.btnAceptarServicio.visibility = if (solicitud.estado == "PENDIENTE") View.VISIBLE else View.GONE
        binding.btnIniciarServicio.visibility = if (solicitud.estado == "ASIGNADA") View.VISIBLE else View.GONE
        binding.btnFinalizarServicio.visibility = if (solicitud.estado == "EN_PROCESO") View.VISIBLE else View.GONE
    }

    private fun actualizarEstado(nuevoEstado: String) {
        viewModel.actualizarEstado(args.solicitudId, nuevoEstado)
        Toast.makeText(requireContext(), "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
    }

    private fun finalizarServicio() {
        val uid = firestore.collection("escoltas").document().id // Mock, should be current user
        // En una app real, aquí se usaría una transacción para actualizar saldos del escolta y usuario
        actualizarEstado("FINALIZADA")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
