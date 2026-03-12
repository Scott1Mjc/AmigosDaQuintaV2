package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
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
 * 
 * Permite trocar um jogador em campo por um da fila de espera, respeitando a posição
 * técnica (Goleiro substitui Goleiro). Inclui opção para registrar lesão.
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

    val filtrados = remember(jogadoresDisponiveis, query, jogadorSaindo) {
        jogadoresDisponiveis.filter {
            it.isPosicaoGoleiro == jogadorSaindo.isPosicaoGoleiro &&
            (query.isBlank() || it.nome.contains(query, ignoreCase = true) || it.numeroCamisa.toString().contains(query))
        }.sortedBy { it.nome }
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
                            Text(jogadorSaindo.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    placeholder = { Text("Buscar substituto...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("DISPONÍVEIS:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (filtrados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum ${if (jogadorSaindo.isPosicaoGoleiro) "goleiro" else "atleta"} na fila", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtrados) { jog ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onConfirm(jog, lesionado) },
                                shape = MaterialTheme.shapes.small,
                                color = Color(0xFFF3F0F5)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(jog.nome, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
