package com.example.amigosdaquinta.ui.screens.jogadores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.ui.screens.home.AdicionarJogadorDialog

/**
 * Tela de gerenciamento completo de jogadores (CRUD).
 *
 * Funcionalidades:
 * - Listar todos os jogadores
 * - Adicionar novo jogador
 * - Editar jogador existente
 * - Remover jogador (marca como inativo)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarJogadoresScreen(
    viewModel: JogadoresViewModel,
    onNavigateBack: () -> Unit
) {
    val jogadores by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAdicionarDialog by remember { mutableStateOf(false) }
    var jogadorParaEditar by remember { mutableStateOf<Jogador?>(null) }
    var jogadorParaRemover by remember { mutableStateOf<Jogador?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Jogadores") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdicionarDialog = true }) {
                Icon(Icons.Default.Add, "Adicionar Jogador")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                jogadores.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Nenhum jogador cadastrado",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Clique no botão + para adicionar",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                "Total: ${jogadores.size} jogadores",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(
                            items = jogadores,
                            key = { it.id }
                        ) { jogador ->
                            JogadorCardComAcoes(
                                jogador = jogador,
                                onEditar = { jogadorParaEditar = jogador },
                                onRemover = { jogadorParaRemover = jogador }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Adicionar jogador
    if (showAdicionarDialog) {
        AdicionarJogadorDialog(
            onDismiss = { showAdicionarDialog = false },
            onConfirm = { nome, numero, isGoleiro ->
                viewModel.adicionarJogador(nome, numero, isGoleiro)
                showAdicionarDialog = false
            }
        )
    }

    // Dialog: Editar jogador
    jogadorParaEditar?.let { jogador ->
        EditarJogadorDialog(
            jogador = jogador,
            onDismiss = { jogadorParaEditar = null },
            onConfirm = { nome, numero, isGoleiro ->
                viewModel.editarJogador(jogador.id, nome, numero, isGoleiro)
                jogadorParaEditar = null
            }
        )
    }

    // Dialog: Confirmar remoção
    jogadorParaRemover?.let { jogador ->
        AlertDialog(
            onDismissRequest = { jogadorParaRemover = null },
            title = { Text("Remover Jogador") },
            text = {
                Text("Tem certeza que deseja remover ${jogador.nome}? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removerJogador(jogador.id)
                        jogadorParaRemover = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { jogadorParaRemover = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Card de jogador com botões de ação (editar/remover)
 */
@Composable
private fun JogadorCardComAcoes(
    jogador: Jogador,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Informações do jogador
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogador.nome,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Camisa ${jogador.numeroCamisa}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Botões de ação
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar jogador",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onRemover) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remover jogador",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}