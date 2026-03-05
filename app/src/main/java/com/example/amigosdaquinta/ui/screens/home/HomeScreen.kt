package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import androidx.compose.material.icons.filled.Close

/**
 * Tela inicial do app com layout otimizado para tablet.
 *
 * Layout de 3 colunas:
 * - Esquerda: Lista de jogadores cadastrados
 * - Centro: Card Time Vermelho
 * - Direita: Card Time Branco
 *
 * FAB: Menu com opções de Ver Histórico e Gerenciar Jogadores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JogadoresViewModel,
    onNavigateToPresenca: () -> Unit = {},
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {},
    onNavigateToGerenciarJogadores: () -> Unit = {}
) {
    val jogadores by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAdicionarDialog by remember { mutableStateOf(false) }
    var timeParaAdicionar by remember { mutableStateOf<String?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }

    val jogadoresOrdenados by remember(jogadores) {
        derivedStateOf { jogadores.sortedBy { it.numeroCamisa } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gerenciador de Futebol",
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    viewModel.popularBancoComJogadoresDeTeste()
                                }
                            )
                        }
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToDebug) {
                        Text("Debug")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Menu expandido
                if (showFabMenu) {
                    // Histórico
                    SmallFloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            onNavigateToHistorico()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.History, "Ver Histórico")
                    }

                    // Gerenciar Jogadores
                    SmallFloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            onNavigateToGerenciarJogadores()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Settings, "Gerenciar Jogadores")
                    }
                }

                // FAB Principal
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu }
                ) {
                    Icon(
                        if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (showFabMenu) "Fechar" else "Menu"
                    )
                }
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // COLUNA 1: Lista de Jogadores
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "Lista de Jogadores",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Total: ${jogadoresOrdenados.size} jogadores",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        jogadoresOrdenados.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Nenhum jogador cadastrado")
                                    Text(
                                        "Use o menu para adicionar",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = jogadoresOrdenados,
                                    key = { it.id }
                                ) { jogador ->
                                    JogadorItemCompact(jogador)
                                }
                            }
                        }
                    }
                }
            }

            // COLUNA 2: Card Time Vermelho
            TimeCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                titulo = "Card Time Vermelho",
                cor = MaterialTheme.colorScheme.errorContainer,
                onAdicionarJogador = {
                    timeParaAdicionar = "VERMELHO"
                    showAdicionarDialog = true
                },
                jogadoresDisponiveis = jogadoresOrdenados.size
            )

            // COLUNA 3: Card Time Branco
            TimeCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                titulo = "Card Time Branco",
                cor = MaterialTheme.colorScheme.primaryContainer,
                onAdicionarJogador = {
                    timeParaAdicionar = "BRANCO"
                    showAdicionarDialog = true
                },
                jogadoresDisponiveis = jogadoresOrdenados.size
            )
        }
    }

    // Dialog: Adicionar Jogador
    if (showAdicionarDialog) {
        AdicionarJogadorDialog(
            onDismiss = {
                showAdicionarDialog = false
                timeParaAdicionar = null
            },
            onConfirm = { nome, numero, isGoleiro ->
                viewModel.adicionarJogador(nome, numero, isGoleiro)
                showAdicionarDialog = false
                timeParaAdicionar = null
            }
        )
    }
}

/**
 * Card de time com botão para adicionar jogador
 */
@Composable
private fun TimeCard(
    modifier: Modifier = Modifier,
    titulo: String,
    cor: androidx.compose.ui.graphics.Color,
    onAdicionarJogador: () -> Unit,
    jogadoresDisponiveis: Int
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (jogadoresDisponiveis >= 22) {
                    Text(
                        "Pronto para formar time",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        "Aguardando jogadores...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Faltam ${22 - jogadoresDisponiveis}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = onAdicionarJogador,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Adicionar Jogador")
                }
            }

            if (jogadoresDisponiveis >= 22) {
                Button(
                    onClick = { /* TODO: Iniciar sessão */ },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Iniciar Sessão")
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/**
 * Item compacto de jogador para lista lateral
 */
@Composable
private fun JogadorItemCompact(jogador: Jogador) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogador.nome,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "#${jogador.numeroCamisa}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}