package com.example.amigosdaquinta.ui.screens.formacao

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
 * Dialog de selecao de jogador durante a formacao manual de times.
 *
 * Diferente do [SelecionarJogadorDialog] da tela de presenca, este separa
 * goleiros e jogadores de linha em secoes distintas para facilitar a
 * montagem estrategica do time.
 *
 * A separacao em secoes e calculada a partir de [jogadoresFiltrados] ja filtrado,
 * garantindo que a busca afete ambas as secoes simultaneamente.
 *
 * Jogadores ja alocados em qualquer time devem ser filtrados antes de passar [jogadores].
 */
@Composable
fun SelecionarJogadorParaTimeDialog(
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

    val goleiros = jogadoresFiltrados.filter { it.isPosicaoGoleiro }
    val linha = jogadoresFiltrados.filter { !it.isPosicaoGoleiro }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Jogador") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (jogadoresFiltrados.isEmpty()) {
                    Text(
                        text = "Nenhum jogador disponivel",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(400.dp)
                    ) {
                        if (goleiros.isNotEmpty()) {
                            item {
                                Text(
                                    text = "GOLEIROS",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(goleiros) { jogador ->
                                JogadorSelectItem(jogador = jogador, onSelect = onSelect)
                            }
                        }

                        if (linha.isNotEmpty()) {
                            item {
                                Text(
                                    text = "LINHA",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(linha) { jogador ->
                                JogadorSelectItem(jogador = jogador, onSelect = onSelect)
                            }
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
private fun JogadorSelectItem(jogador: Jogador, onSelect: (Jogador) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect(jogador) }
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