package com.example.amigosdaquinta.ui.screens.presenca

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Dialog de selecao de jogador para adicionar a lista de presenca.
 *
 * Filtra por nome, numero de camisa ou posicao ("goleiro").
 * Jogadores ja presentes na lista devem ser filtrados antes de passar [jogadores].
 *
 * A selecao e imediata: tocar no item chama [onSelect] e fecha o dialog
 * (responsabilidade de fechar e do chamador via [onDismiss]).
 */
@Composable
fun SelecionarJogadorDialog(
    jogadores: List<Jogador>,
    onDismiss: () -> Unit,
    onSelect: (Jogador) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val jogadoresFiltrados = remember(jogadores, searchQuery) {
        if (searchQuery.isBlank()) {
            jogadores.sortedBy { it.numeroCamisa }
        } else {
            jogadores.filter {
                it.nome.contains(searchQuery, ignoreCase = true) ||
                        it.numeroCamisa.toString().contains(searchQuery) ||
                        (it.isPosicaoGoleiro && "goleiro".contains(searchQuery, ignoreCase = true))
            }.sortedBy { it.numeroCamisa }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar a Lista") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar jogador") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (jogadoresFiltrados.isEmpty()) {
                    Text(
                        text = "Nenhum jogador disponivel",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    ) {
                        items(jogadoresFiltrados) { jogador ->
                            JogadorListItem(jogador = jogador, onClick = { onSelect(jogador) })
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun JogadorListItem(jogador: Jogador, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(jogador.nome)
            Text("#${jogador.numeroCamisa}")
        }
    }
}