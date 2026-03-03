package com.example.amigosdaquinta.ui.screens.presenca

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Item da lista de presenca do dia.
 *
 * Exibe a ordem de chegada do jogador (badge numerado), nome, numero de camisa,
 * horario de chegada e botao de remocao.
 *
 * @param ordem Posicao na fila (1-based), exibida como badge.
 * @param horario Horario de chegada ja formatado (ex: "14:32").
 * @param onRemove Chamado ao tocar no icone de remocao.
 */
@Composable
fun PresencaItem(
    ordem: Int,
    jogador: Jogador,
    horario: String,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$ordem",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text(
                        text = jogador.nome,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "#${jogador.numeroCamisa} • $horario",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover ${jogador.nome}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}