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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor

@Composable
fun SubstituicaoDialog(
    jogadorSaindo: Jogador,
    time: TimeColor,
    jogadoresDisponiveis: List<Jogador>,
    onDismiss: () -> Unit,
    onConfirm: (Jogador, Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isLesionado by remember { mutableStateOf(false) }

    val jogadoresFiltrados = remember(jogadoresDisponiveis, searchQuery, jogadorSaindo) {
        jogadoresDisponiveis.filter { jogador ->
            jogador.isPosicaoGoleiro == jogadorSaindo.isPosicaoGoleiro &&
            (searchQuery.isBlank() || 
             jogador.nome.contains(searchQuery, ignoreCase = true) ||
             jogador.numeroCamisa.toString().contains(searchQuery))
        }.sortedBy { it.nome }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp),
        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = {
            Column {
                Text("Substituição")
                Text(
                    text = "Apenas ${if (jogadorSaindo.isPosicaoGoleiro) "Goleiros" else "Jogadores de Linha"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("SAI:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = jogadorSaindo.nome, style = MaterialTheme.typography.titleMedium)
                            Text(text = "#${jogadorSaindo.numeroCamisa}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isLesionado, onCheckedChange = { isLesionado = it })
                    Text("O jogador se lesionou? (Sairá do jogo)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Pesquisar...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Pesquisar") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("ENTRA:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (jogadoresFiltrados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray)
                            Text("Nenhum ${if (jogadorSaindo.isPosicaoGoleiro) "goleiro" else "jogador"} disponível", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(jogadoresFiltrados) { jogador ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onConfirm(jogador, isLesionado) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = jogador.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                        Text(text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(text = "#${jogador.numeroCamisa}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private val Color = androidx.compose.ui.graphics.Color
