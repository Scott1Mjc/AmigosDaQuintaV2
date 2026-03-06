package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.ui.screens.formacao.SelecionarJogadorParaTimeDialog
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

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
    sessaoViewModel: SessaoViewModel,
    onNavigateToPresenca: () -> Unit = {},
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {},
    onNavigateToGerenciarJogadores: () -> Unit = {},
    onNavigateToFormacaoManual: () -> Unit = {}
) {
    val jogadores by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ✅ USAR rememberSaveable para manter estado ao navegar
    var timeBranco by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var timeVermelho by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var showSelecionarDialog by remember { mutableStateOf(false) }
    var timeParaAdicionar by remember { mutableStateOf<String?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showErroGoleiroDialog by remember { mutableStateOf(false) }

    val jogadoresOrdenados by remember(jogadores) {
        derivedStateOf { jogadores.sortedBy { it.numeroCamisa } }
    }

    // Jogadores disponíveis (não estão em nenhum time)
    val jogadoresDisponiveis = jogadoresOrdenados.filter { jogador ->
        !timeBranco.any { it.id == jogador.id } &&
                !timeVermelho.any { it.id == jogador.id }
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
                    TextButton(onClick = onNavigateToDebug) {
                        Text("Debug")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Opções do menu
                AnimatedVisibility(visible = showFabMenu) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Ver Histórico
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "Ver Histórico",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    showFabMenu = false
                                    onNavigateToHistorico()
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(Icons.Default.AccessTime, "Ver Histórico")
                            }
                        }

                        // Gerenciar Jogadores
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "Gerenciar Jogadores",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
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
            // COLUNA 1: Lista de Jogadores (lado esquerdo)
            Card(
                modifier = Modifier
                    .width(300.dp)
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

            // COLUNA 2: Cards dos times (lado direito)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Time Vermelho
                TimeCardCompact(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    titulo = "Card Time Vermelho",
                    cor = MaterialTheme.colorScheme.errorContainer,
                    jogadores = timeVermelho,
                    onAdicionarJogador = {
                        timeParaAdicionar = "VERMELHO"
                        showSelecionarDialog = true
                    },
                    onRemoverJogador = { jogador ->
                        timeVermelho = timeVermelho.filter { it.id != jogador.id }
                    }
                )

                // Card Time Branco
                TimeCardCompact(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    titulo = "Card Time Branco",
                    cor = MaterialTheme.colorScheme.primaryContainer,
                    jogadores = timeBranco,
                    onAdicionarJogador = {
                        timeParaAdicionar = "BRANCO"
                        showSelecionarDialog = true
                    },
                    onRemoverJogador = { jogador ->
                        timeBranco = timeBranco.filter { it.id != jogador.id }
                    }
                )

                // ✅ BOTÃO INICIAR SESSÃO AQUI EMBAIXO (SÓ 1)
                if (timeBranco.size == 11 && timeVermelho.size == 11) {
                    Button(
                        onClick = {
                            // Salvar times no SessaoViewModel
                            sessaoViewModel.criarJogo(
                                timeBranco = timeBranco,
                                timeVermelho = timeVermelho
                            )
                            // Ir direto pro jogo
                            onNavigateToFormacaoManual()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            "Iniciar Sessão",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    // Dialog: Selecionar Jogador para Time
    if (showSelecionarDialog && timeParaAdicionar != null) {
        val jaTemGoleiroBranco = timeBranco.any { it.isPosicaoGoleiro }
        val jaTemGoleiroVermelho = timeVermelho.any { it.isPosicaoGoleiro }

        // ✅ FILTRAR GOLEIROS se já tem 1 no time
        val jogadoresFiltrados = when (timeParaAdicionar) {
            "BRANCO" -> if (jaTemGoleiroBranco) {
                jogadoresDisponiveis.filter { !it.isPosicaoGoleiro }
            } else {
                jogadoresDisponiveis
            }
            "VERMELHO" -> if (jaTemGoleiroVermelho) {
                jogadoresDisponiveis.filter { !it.isPosicaoGoleiro }
            } else {
                jogadoresDisponiveis
            }
            else -> jogadoresDisponiveis
        }

        SelecionarJogadorParaTimeDialog(
            jogadores = jogadoresFiltrados,
            onDismiss = {
                showSelecionarDialog = false
                timeParaAdicionar = null
            },
            onSelect = { jogador ->
                when (timeParaAdicionar) {
                    "BRANCO" -> {
                        val jaTemGoleiro = timeBranco.any { it.isPosicaoGoleiro }
                        if (jogador.isPosicaoGoleiro && jaTemGoleiro) {
                            showErroGoleiroDialog = true
                        } else if (timeBranco.size < 11) {
                            timeBranco = timeBranco + jogador
                        }
                    }
                    "VERMELHO" -> {
                        val jaTemGoleiro = timeVermelho.any { it.isPosicaoGoleiro }
                        if (jogador.isPosicaoGoleiro && jaTemGoleiro) {
                            showErroGoleiroDialog = true
                        } else if (timeVermelho.size < 11) {
                            timeVermelho = timeVermelho + jogador
                        }
                    }
                }
                showSelecionarDialog = false
                timeParaAdicionar = null
            }
        )
    }

    // Dialog: Erro - Goleiro duplicado
    if (showErroGoleiroDialog) {
        AlertDialog(
            onDismissRequest = { showErroGoleiroDialog = false },
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Erro ao adicionar jogador") },
            text = { Text("Este time já possui um goleiro. Cada time pode ter apenas 1 goleiro.") },
            confirmButton = {
                Button(onClick = { showErroGoleiroDialog = false }) {
                    Text("Entendi")
                }
            }
        )
    }
}

/**
 * Card compacto de time (sem botão iniciar sessão interno)
 */
@Composable
private fun TimeCardCompact(
    modifier: Modifier = Modifier,
    titulo: String,
    cor: androidx.compose.ui.graphics.Color,
    jogadores: List<Jogador>,
    onAdicionarJogador: () -> Unit,
    onRemoverJogador: (Jogador) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${jogadores.size}/11",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botão adicionar (só se não estiver completo)
            if (jogadores.size < 11) {
                Button(
                    onClick = onAdicionarJogador,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Jogador")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Lista de jogadores
            if (jogadores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum jogador", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = jogadores,
                        key = { it.id }
                    ) { jogador ->
                        JogadorNoTimeItemCompact(
                            jogador = jogador,
                            onRemover = { onRemoverJogador(jogador) }
                        )
                    }
                }
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

/**
 * Item de jogador dentro do time (com botão remover)
 */
@Composable
private fun JogadorNoTimeItemCompact(
    jogador: Jogador,
    onRemover: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${jogador.numeroCamisa}",
                    style = MaterialTheme.typography.bodySmall
                )
                IconButton(
                    onClick = onRemover,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remover",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}