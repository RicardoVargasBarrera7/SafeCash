package com.project.safecash.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.safecash.data.model.Solicitud
import com.project.safecash.databinding.ItemSolicitudBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SolicitudAdapter(private val onClick: (Solicitud) -> Unit) :
    ListAdapter<Solicitud, SolicitudAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemSolicitudBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(solicitud: Solicitud, onClick: (Solicitud) -> Unit) {
            binding.tvTipoServicio.text = solicitud.tipoServicio
            binding.tvMonto.text = String.format("$ %.2f", solicitud.monto)
            
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvFecha.text = sdf.format(solicitud.fechaCreacion.toDate())
            
            binding.tvEstado.text = solicitud.estado
            
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

    companion object DiffCallback : DiffUtil.ItemCallback<Solicitud>() {
        override fun areItemsTheSame(oldItem: Solicitud, newItem: Solicitud): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Solicitud, newItem: Solicitud): Boolean {
            return oldItem == newItem
        }
    }
}
