package com.example.amigosdaquinta.ui.screens.jogadores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
 * - Listar todos os jogadores cadastrados
 * - Adicionar novo jogador (números de camisa: 0 a 999)
 * - Editar jogador existente
 * - Remover jogador (exclusão permanente com confirmação)
 *
 * Validações:
 * - Nome não pode ser vazio
 * - Número da camisa deve estar entre 0 e 999
 * - Suporte a números especiais (777, 110, etc.)
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

    // Ordenar jogadores por número de camisa
    val jogadoresOrdenados by remember(jogadores) {
        derivedStateOf { jogadores.sortedBy { it.numeroCamisa } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Jogadores") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        "Total: ${jogadores.size} jogadores",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Números de camisa suportados: 0 a 999",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(
                            items = jogadoresOrdenados,
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
            onConfirm = { jogadorAtualizado ->
                viewModel.editarJogador(
                    jogadorAtualizado.id,
                    jogadorAtualizado.nome,
                    jogadorAtualizado.numeroCamisa,
                    jogadorAtualizado.isPosicaoGoleiro
                )
                jogadorParaEditar = null
            }
        )
    }

    // Dialog: Confirmar remoção
    jogadorParaRemover?.let { jogador ->
        AlertDialog(
            onDismissRequest = { jogadorParaRemover = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Remover Jogador") },
            text = {
                Column {
                    Text("Tem certeza que deseja remover este jogador?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                jogador.nome,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Camisa #${jogador.numeroCamisa}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (jogador.isPosicaoGoleiro) "Goleiro" else "Jogador de Linha",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Esta ação não pode ser desfeita.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
 * Card de jogador com botões de ação (editar/remover).
 *
 * Exibe:
 * - Nome do jogador
 * - Número da camisa (suporta 0-999)
 * - Posição (Goleiro ou Linha)
 * - Botões de editar e remover
 *
 * @param jogador Dados do jogador
 * @param onEditar Callback ao clicar em editar
 * @param onRemover Callback ao clicar em remover
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
                    // Badge do número da camisa
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "#${jogador.numeroCamisa}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Badge da posição
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (jogador.isPosicaoGoleiro) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = if (jogador.isPosicaoGoleiro) "GOL" else "LINHA",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
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