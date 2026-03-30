package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Diálogo de seleção de atletas para escalação manual.
 * 
 * Organiza os atletas em seções (Goleiros e Linha) para facilitar a montagem
 * estratégica das equipes.
 */
@Composable
fun SelecionarJogadorParaTimeDialog(
    jogadores: List<Jogador>,
    bloquearGoleiros: Boolean = false,
    onDismiss: () -> Unit,
    onSelect: (Jogador) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtrados = remember(jogadores, query) {
        jogadores.filter {
            it.nome.contains(query, ignoreCase = true) || it.numeroCamisa.toString().contains(query)
        }.sortedBy { it.numeroCamisa }.distinctBy { it.id }
    }

    val goleiros = filtrados.filter { it.isPosicaoGoleiro }
    val linha = filtrados.filter { !it.isPosicaoGoleiro }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escalar Atleta", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar por nome ou nº...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filtrados.isEmpty()) {
                    Text("Nenhum atleta disponível", modifier = Modifier.padding(16.dp), color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        if (goleiros.isNotEmpty()) {
                            item { SectionHeader("GOLEIROS") }
                            items(goleiros, key = { "gol_${it.id}" }) { jog -> 
                                ItemSelecao(
                                    jogador = jog, 
                                    enabled = !bloquearGoleiros,
                                    onSelect = onSelect 
                                ) 
                            }
                        }

                        if (linha.isNotEmpty()) {
                            item { SectionHeader("JOGADORES DE LINHA") }
                            items(linha, key = { "lin_${it.id}" }) { jog -> ItemSelecao(jogador = jog, onSelect = onSelect) }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF4B0082),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ItemSelecao(
    jogador: Jogador, 
    enabled: Boolean = true,
    onSelect: (Jogador) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled) { onSelect(jogador) },
        shape = MaterialTheme.shapes.small,
        color = if (enabled) Color(0xFFF3F0F5) else Color(0xFFE0E0E0)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = jogador.nome, 
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color.Unspecified else Color.Gray
            )
            Text(
                text = "#${jogador.numeroCamisa}", 
                fontWeight = FontWeight.Bold, 
                color = if (enabled) Color(0xFF4B0082) else Color.Gray
            )
        }
    }
}