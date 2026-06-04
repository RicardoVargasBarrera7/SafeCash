package com.project.safecash.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.project.safecash.data.model.Solicitud
import com.project.safecash.ui.theme.AccentGreen
import com.project.safecash.ui.theme.TextLight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ServiceTimeline(solicitud: Solicitud) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val pasos = listOf(
        StepData("PENDIENTE", "Creada", solicitud.fechaCreacion),
        StepData("ASIGNADA", "Asignada", solicitud.fechaAsignacion),
        StepData("EN_CAMINO", "En Camino", solicitud.fechaEnCamino),
        StepData("EN_PROCESO", "En Proceso", solicitud.fechaEnProceso),
        StepData("ENTREGADO", "Entregado", solicitud.fechaEntregado),
        StepData("FINALIZADA", "Finalizado", solicitud.fechaFinalizacion)
    )

    val estadoActual = solicitud.estado
    val indiceActual = pasos.indexOfLast { it.id == estadoActual }.let { if (it == -1) 0 else it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        pasos.forEachIndexed { index, step ->
            val isCompleted = index <= indiceActual || step.timestamp != null
            val isActive = index == indiceActual

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                // Indicador visual
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    val color = if (isCompleted) AccentGreen else Color.LightGray
                    
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (isCompleted) color else color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.White)
                        } else {
                            Box(modifier = Modifier.size(8.dp).background(Color.LightGray, CircleShape))
                        }
                    }
                    
                    if (index < pasos.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(if (index < indiceActual) AccentGreen else Color.LightGray.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Información del paso
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = step.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) MaterialTheme.colorScheme.primary else if (isCompleted) Color.Black else Color.Gray
                        )
                    }
                    
                    if (step.timestamp != null) {
                        Text(
                            text = dateFormat.format(step.timestamp.toDate()),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class StepData(
    val id: String,
    val label: String,
    val timestamp: Timestamp?
)
