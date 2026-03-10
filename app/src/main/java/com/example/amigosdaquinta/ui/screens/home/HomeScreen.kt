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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.ui.screens.formacao.SelecionarJogadorParaTimeDialog
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela inicial do app com layout otimizado para tablet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    onNavigateToPresenca: () -> Unit = {},
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToGerenciarJogadores: () -> Unit = {},
    onNavigateToFormacaoManual: () -> Unit = {}
) {
    val jogadores by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    var timeBranco by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var timeVermelho by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var showSelecionarDialog by remember { mutableStateOf(false) }
    var timeParaAdicionar by remember { mutableStateOf<String?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }

    // ✅ ORDENAÇÃO: Não confirmados (topo) -> Confirmados (baixo) + Número Decrescente
    val jogadoresOrdenados by remember(jogadores, listaPresenca) {
        derivedStateOf {
            jogadores.sortedWith(
                compareBy<Jogador> { jogador ->
                    listaPresenca.any { it.first.id == jogador.id }
                }.thenByDescending { it.numeroCamisa }
            )
        }
    }

    val jogadoresDisponiveis = jogadores.filter { jogador ->
        !timeBranco.any { it.id == jogador.id } &&
        !timeVermelho.any { it.id == jogador.id }
    }.sortedByDescending { it.numeroCamisa }

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
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
            // COLUNA 1: Confirmar Chegada
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
                        "Confirmar Chegada",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Total: ${jogadoresOrdenados.size} jogadores",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = jogadoresOrdenados,
                                key = { it.id }
                            ) { jogador ->
                                val jaConfirmado = listaPresenca.any { it.first.id == jogador.id }
                                val ordemChegada = if (jaConfirmado) {
                                    listaPresenca.indexOfFirst { it.first.id == jogador.id } + 1
                                } else {
                                    null
                                }

                                JogadorComCheckbox(
                                    jogador = jogador,
                                    confirmado = jaConfirmado,
                                    ordemChegada = ordemChegada,
                                    onConfirmar = { confirmado ->
                                        if (confirmado) sessaoViewModel.adicionarAListaPresenca(jogador)
                                        else sessaoViewModel.removerDaListaPresenca(jogador.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // COLUNA 2: Times
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimeCardCompact(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    titulo = "Time Vermelho",
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

                TimeCardCompact(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    titulo = "Time Branco",
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

                if (timeBranco.size == 11 && timeVermelho.size == 11) {
                    Button(
                        onClick = {
                            sessaoViewModel.criarJogo(timeBranco, timeVermelho)
                            onNavigateToFormacaoManual()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Iniciar Sessão", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (showSelecionarDialog && timeParaAdicionar != null) {
        val jaTemGoleiroBranco = timeBranco.any { it.isPosicaoGoleiro }
        val jaTemGoleiroVermelho = timeVermelho.any { it.isPosicaoGoleiro }

        val jogadoresFiltrados = when (timeParaAdicionar) {
            "BRANCO" -> if (jaTemGoleiroBranco) jogadoresDisponiveis.filter { !it.isPosicaoGoleiro } else jogadoresDisponiveis
            "VERMELHO" -> if (jaTemGoleiroVermelho) jogadoresDisponiveis.filter { !it.isPosicaoGoleiro } else jogadoresDisponiveis
            else -> jogadoresDisponiveis
        }

        SelecionarJogadorParaTimeDialog(
            jogadores = jogadoresFiltrados,
            onDismiss = { showSelecionarDialog = false },
            onSelect = { jogador ->
                when (timeParaAdicionar) {
                    "BRANCO" -> if (timeBranco.size < 11) timeBranco = timeBranco + jogador
                    "VERMELHO" -> if (timeVermelho.size < 11) timeVermelho = timeVermelho + jogador
                }
                showSelecionarDialog = false
            }
        )
    }
}

@Composable
private fun TimeCardCompact(
    modifier: Modifier = Modifier,
    titulo: String,
    cor: androidx.compose.ui.graphics.Color,
    jogadores: List<Jogador>,
    onAdicionarJogador: () -> Unit,
    onRemoverJogador: (Jogador) -> Unit
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = cor)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text("${jogadores.size}/11", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (jogadores.size < 11) {
                Button(onClick = onAdicionarJogador, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Jogador")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = jogadores, key = { it.id }) { jogador ->
                    JogadorNoTimeItemCompact(jogador = jogador, onRemover = { onRemoverJogador(jogador) })
                }
            }
        }
    }
}

@Composable
private fun JogadorComCheckbox(
    jogador: Jogador,
    confirmado: Boolean,
    ordemChegada: Int?,
    onConfirmar: (Boolean) -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, tonalElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Checkbox(checked = confirmado, onCheckedChange = onConfirmar)
            if (confirmado && ordemChegada != null) {
                Text(text = "${ordemChegada}°", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(32.dp))
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
            Text("|", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = jogador.nome, style = MaterialTheme.typography.bodyMedium)
                Text(text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(text = "#${jogador.numeroCamisa}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun JogadorNoTimeItemCompact(jogador: Jogador, onRemover: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "#${jogador.numeroCamisa}", style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = onRemover, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
