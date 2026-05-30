package com.project.safecash.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.safecash.R
import com.project.safecash.data.model.Solicitud
import com.project.safecash.databinding.ItemSolicitudBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para gestionar y mostrar una lista de [Solicitud] en un RecyclerView.
 * Utiliza [ListAdapter] y [DiffUtil] para realizar actualizaciones eficientes en la interfaz.
 *
 * @param onClick Acción a ejecutar cuando el usuario pulsa sobre un elemento de la lista.
 */
class SolicitudAdapter(private val onClick: (Solicitud) -> Unit) :
    ListAdapter<Solicitud, SolicitudAdapter.ViewHolder>(DiffCallback) {

    /**
     * ViewHolder que gestiona la vista de cada item de solicitud individual.
     */
    class ViewHolder(private val binding: ItemSolicitudBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Asocia los datos de la [Solicitud] con los elementos visuales del item.
         */
        fun bind(solicitud: Solicitud, onClick: (Solicitud) -> Unit) {
            // Asignación de textos básicos
            binding.tvTipoServicio.text = solicitud.tipoServicio
            
            // Usamos getString con el recurso formateado para evitar avisos de Locale y Hardcoded strings
            binding.tvMonto.text = binding.root.context.getString(R.string.balance_format, solicitud.monto)
            
            // Formateo de fecha: se utiliza Locale.getDefault() para respetar la configuración del sistema
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvFecha.text = sdf.format(solicitud.fechaCreacion.toDate())
            
            binding.tvEstado.text = solicitud.estado
            
            // Configuración del evento de clic
            binding.root.setOnClickListener { onClick(solicitud) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSolicitudBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    /**
     * Comparador para optimizar los cambios en la lista de RecyclerView.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Solicitud>() {
        override fun areItemsTheSame(oldItem: Solicitud, newItem: Solicitud): Boolean {
            // Comparamos por ID único de la solicitud
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Solicitud, newItem: Solicitud): Boolean {
            // Comparamos el contenido completo del objeto
            return oldItem == newItem
        }
    }
}
