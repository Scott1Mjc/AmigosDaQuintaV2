package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Card de formação manual de um time durante a montagem de partida.
 *
 * Exibe contador de jogadores, alertas de validação (sem goleiro, time completo)
 * e lista scrollável com opção de remoção individual.
 *
 * O botão de adicionar é ocultado automaticamente quando [completo] é true.
 *
 * @param titulo Nome do time exibido no header (ex: "TIME BRANCO").
 * @param cor Cor de fundo do card — deve refletir a cor do time.
 * @param jogadores Jogadores atualmente no time.
 * @param completo True quando o time atingiu 11 jogadores.
 * @param temGoleiro True quando ao menos um jogador da lista é goleiro.
 * @param onAdicionar Chamado ao tocar no botão de adicionar jogador.
 * @param onRemover Chamado ao tocar no ícone de remoção de um jogador específico.
 */
@Composable
fun TimeFormadoCard(
    modifier: Modifier = Modifier,
    titulo: String,
    cor: Color,
    jogadores: List<Jogador>,
    completo: Boolean,
    temGoleiro: Boolean,
    onAdicionar: () -> Unit,
    onRemover: (Jogador) -> Unit
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = cor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = titulo, style = MaterialTheme.typography.titleMedium)
                Text(text = "${jogadores.size}/11", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (jogadores.isNotEmpty() && !temGoleiro) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Precisa de 1 goleiro",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (completo && temGoleiro) {
                Surface(
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Time completo",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!completo) {
                Button(
                    onClick = onAdicionar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Jogador")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (jogadores.isEmpty()) {
                EmptyTimeState()
            } else {
                JogadoresList(
                    modifier = Modifier.weight(1f),
                    jogadores = jogadores,
                    onRemover = onRemover
                )
            }
        }
    }
}

@Composable
private fun EmptyTimeState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Nenhum jogador", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun JogadoresList(
    modifier: Modifier = Modifier,
    jogadores: List<Jogador>,
    onRemover: (Jogador) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = jogadores, key = { it.id }) { jogador ->
            JogadorNoTimeItem(jogador = jogador, onRemover = { onRemover(jogador) })
        }
    }
}

@Composable
private fun JogadorNoTimeItem(
    jogador: Jogador,
    onRemover: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Camisa ${jogador.numeroCamisa}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemover, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover ${jogador.nome}",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}