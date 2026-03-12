package com.example.amigosdaquinta.ui.presenca

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Item visual para a lista de presença.
 * 
 * Exibe a ordem de chegada, nome, número da camisa e horário de registro.
 */
@Composable
fun PresencaItem(
    ordem: Int,
    jogador: Jogador,
    horario: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge de Ordem de Chegada
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFFF0EDFF)
                ) {
                    Text(
                        text = "${ordem}º",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4B0082),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = jogador.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "#${jogador.numeroCamisa} • Chegada às $horario",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}
