package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Diálogo para realização de substituições durante a partida.
 */
@Composable
fun SubstituicaoDialog(
    jogadorSaindo: Jogador,
    time: TimeColor,
    jogadoresDisponiveis: List<Jogador>,
    onDismiss: () -> Unit,
    onConfirm: (Jogador, Boolean) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var lesionado by remember { mutableStateOf(false) }

    // ✅ REGRA: Se sair Goleiro, só entra Goleiro. Se sair Linha, só entra Linha.
    val filtrados = remember(jogadoresDisponiveis, query) {
        jogadoresDisponiveis
            .filter { jog ->
                val matchSearch = query.isBlank() || 
                                 jog.nome.contains(query, ignoreCase = true) || 
                                 jog.numeroCamisa.toString().contains(query)
                
                // Restrição de posição: Goleiro substitui Goleiro, Linha substitui Linha
                val matchPosicao = jog.isPosicaoGoleiro == jogadorSaindo.isPosicaoGoleiro
                
                matchSearch && matchPosicao
            }
            .sortedBy { it.nome }
            .distinctBy { it.id }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp),
        title = { Text("Substituição Tática", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Card de quem sai
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFFFFE1E1)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("SAINDO:", style = MaterialTheme.typography.labelSmall, color = Color.Red, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                (if (jogadorSaindo.isPosicaoGoleiro) "[GOL] " else "") + jogadorSaindo.nome,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("#${jogadorSaindo.numeroCamisa}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = lesionado, onCheckedChange = { lesionado = it })
                    Text("Remover atleta da sessão (Lesão)", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar substituto na fila...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                val tipoText = if (jogadorSaindo.isPosicaoGoleiro) "GOLEIROS" else "JOGADORES DE LINHA"
                Text("$tipoText DISPONÍVEIS NA FILA:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (filtrados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum ${if (jogadorSaindo.isPosicaoGoleiro) "goleiro" else "jogador"} disponível", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtrados, key = { it.id }) { jog ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onConfirm(jog, lesionado) },
                                shape = MaterialTheme.shapes.small,
                                color = Color(0xFFF3F0F5)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        (if (jog.isPosicaoGoleiro) "[GOL] " else "") + jog.nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFF0EDFF)) {
                                        Text("#${jog.numeroCamisa}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = Color(0xFF4B0082), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) } }
    )
}
