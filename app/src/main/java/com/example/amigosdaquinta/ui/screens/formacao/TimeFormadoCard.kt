package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Card para formação manual de times.
 * 
 * Exibe a contagem de atletas, validações de formação (mínimo de 1 goleiro)
 * e a lista de jogadores escalados com opção de remoção.
 *
 * @param titulo Nome do time (ex: "TIME BRANCO").
 * @param cor Cor de fundo temática do time.
 * @param jogadores Lista de atletas escalados.
 * @param completo Indica se o time atingiu o limite de 11 jogadores.
 * @param temGoleiro Indica se há ao menos um goleiro escalado.
 * @param onAdicionar Callback para inclusão de novo atleta.
 * @param onRemover Callback para remoção de um atleta específico.
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
    val jogadoresUnicos = remember(jogadores) { jogadores.distinctBy { it.id } }

    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = cor),
        shape = MaterialTheme.shapes.medium
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
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${jogadoresUnicos.size}/11", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alertas de Validação
            if (jogadoresUnicos.isNotEmpty() && !temGoleiro) {
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "⚠️ Necessário 1 goleiro",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!completo) {
                Button(
                    onClick = onAdicionar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Jogador", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (jogadoresUnicos.isEmpty()) {
                EmptyTimeState()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = jogadoresUnicos, key = { it.id }) { jogador ->
                        JogadorNoTimeItem(jogador = jogador, onRemover = { onRemover(jogador) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTimeState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Escalação vazia", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
private fun JogadorNoTimeItem(jogador: Jogador, onRemover: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "#${jogador.numeroCamisa}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onRemover, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    }
}