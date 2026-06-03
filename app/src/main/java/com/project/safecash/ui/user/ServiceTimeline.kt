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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.safecash.ui.theme.AccentGreen

/**
 * Componente visual que muestra el flujo de estados de un servicio.
 */
@Composable
fun ServiceTimeline(estadoActual: String) {
    val estados = listOf(
        "PENDIENTE" to "Creada",
        "ASIGNADA" to "Asignada",
        "EN_CAMINO" to "En Camino",
        "EN_PROCESO" to "En Proceso",
        "FINALIZADA" to "Finalizado"
    )

    val indiceActual = estados.indexOfFirst { it.first == estadoActual }.let { if (it == -1) 0 else it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        estados.forEachIndexed { index, (id, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                // Indicador visual (Círculo y Línea)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    val isCompleted = index <= indiceActual
                    val color = if (isCompleted) AccentGreen else Color.LightGray
                    
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        }
                    }
                    
                    if (index < estados.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(if (index < indiceActual) AccentGreen else Color.LightGray)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Texto del estado
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (index == indiceActual) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == indiceActual) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}
