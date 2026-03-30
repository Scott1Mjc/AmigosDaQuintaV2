package com.example.amigosdaquinta.ui.presenca

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
 * Diálogo para seleção de atletas cadastrados.
 * 
 * Utilizado para adicionar jogadores à lista de presença da sessão.
 */
@Composable
fun SelecionarJogadorDialog(
    jogadores: List<Jogador>,
    onDismiss: () -> Unit,
    onSelect: (Jogador) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtrados = remember(jogadores, query) {
        jogadores.filter {
            it.nome.contains(query, ignoreCase = true) || it.numeroCamisa.toString().contains(query)
        }.sortedBy { it.numeroCamisa }.distinctBy { it.id }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Atleta", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Pesquisar por nome ou nº...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filtrados.isEmpty()) {
                    Text("Nenhum atleta disponível", modifier = Modifier.padding(16.dp), color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        items(filtrados, key = { it.id }) { jog ->
                            ItemSelecaoJogador(jogador = jog, onClick = { onSelect(jog) })
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
private fun ItemSelecaoJogador(jogador: Jogador, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = Color(0xFFF3F0F5)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(jogador.nome, fontWeight = FontWeight.Medium)
            Text("#${jogador.numeroCamisa}", fontWeight = FontWeight.Bold, color = Color(0xFF4B0082))
        }
    }
}