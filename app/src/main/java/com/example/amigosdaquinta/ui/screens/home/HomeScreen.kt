package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.ui.screens.formacao.SelecionarJogadorParaTimeDialog
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela Principal do sistema Amigos da Quinta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToGerenciarJogadores: () -> Unit = {},
    onNavigateToJogo: () -> Unit = {}
) {
    val jogadores by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var timeBranco by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var timeVermelho by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var showSelecionarDialog by remember { mutableStateOf(false) }
    var timeParaAdicionar by remember { mutableStateOf<String?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        viewModel.buscarPorNome(searchQuery)
    }

    // Ordenação da fila: 
    // 1. Jogadores na lista de presença primeiro, ordenados pelo timestamp (quem chegou antes fica no topo)
    // 2. Jogadores fora da lista depois, por número de camisa
    val jogadoresOrdenados by remember(jogadores, listaPresenca) {
        derivedStateOf {
            val presentesMap = listaPresenca.associate { it.first.id to it.second }
            jogadores.sortedWith(
                compareByDescending<Jogador> { presentesMap.containsKey(it.id) }
                    .thenBy { presentesMap[it.id] ?: Long.MAX_VALUE }
                    .thenByDescending { it.numeroCamisa }
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
                title = { Text("Amigos da Quinta", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            HomeFabMenu(
                expanded = showFabMenu,
                onToggle = { showFabMenu = !showFabMenu },
                onNavigateToHistorico = onNavigateToHistorico,
                onNavigateToGerenciarJogadores = onNavigateToGerenciarJogadores
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.width(320.dp).fillMaxHeight()) {
                FilaChegadaCard(
                    jogadores = jogadoresOrdenados,
                    listaPresenca = listaPresenca,
                    isLoading = isLoading,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onConfirmarPresenca = { jogador, confirmado ->
                        if (confirmado) sessaoViewModel.adicionarAListaPresenca(jogador)
                        else sessaoViewModel.removerDaListaPresenca(jogador.id)
                    }
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimeCardEscalacao(
                    titulo = "Time Vermelho",
                    corFundo = Color(0xFFFFE1E1),
                    jogadores = timeVermelho,
                    onAdicionar = {
                        timeParaAdicionar = "VERMELHO"
                        showSelecionarDialog = true
                    },
                    onRemover = { jogador ->
                        timeVermelho = timeVermelho.filter { it.id != jogador.id }
                    }
                )

                TimeCardEscalacao(
                    titulo = "Time Branco",
                    corFundo = Color(0xFFE8E2FF),
                    jogadores = timeBranco,
                    onAdicionar = {
                        timeParaAdicionar = "BRANCO"
                        showSelecionarDialog = true
                    },
                    onRemover = { jogador ->
                        timeBranco = timeBranco.filter { it.id != jogador.id }
                    }
                )

                if (timeBranco.size == 11 && timeVermelho.size == 11) {
                    Button(
                        onClick = {
                            val todosJogadores = (timeBranco + timeVermelho).distinct()
                            todosJogadores.forEach { jogador ->
                                sessaoViewModel.adicionarAListaPresenca(jogador)
                            }
                            sessaoViewModel.criarJogo(timeBranco, timeVermelho)
                            onNavigateToJogo()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
                    ) {
                        Text("INICIAR SESSÃO", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showSelecionarDialog && timeParaAdicionar != null) {
        val jaTemGoleiro = if (timeParaAdicionar == "BRANCO") timeBranco.any { it.isPosicaoGoleiro } else timeVermelho.any { it.isPosicaoGoleiro }
        val jogadoresFiltrados = if (jaTemGoleiro) jogadoresDisponiveis.filter { !it.isPosicaoGoleiro } else jogadoresDisponiveis

        SelecionarJogadorParaTimeDialog(
            jogadores = jogadoresFiltrados,
            onDismiss = { showSelecionarDialog = false },
            onSelect = { jogador ->
                if (timeParaAdicionar == "BRANCO" && timeBranco.size < 11) timeBranco = timeBranco + jogador
                else if (timeParaAdicionar == "VERMELHO" && timeVermelho.size < 11) timeVermelho = timeVermelho + jogador
                showSelecionarDialog = false
            }
        )
    }
}

@Composable
private fun FilaChegadaCard(
    jogadores: List<Jogador>,
    listaPresenca: List<Pair<Jogador, Long>>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onConfirmarPresenca: (Jogador, Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Confirmar Chegada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar jogador...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Ordenamos a lista de presença para saber a ordem numérica (1º, 2º...)
                    val presencaOrdenada = listaPresenca.sortedBy { it.second }
                    
                    items(items = jogadores, key = { it.id }) { jogador ->
                        val dadosPresenca = listaPresenca.find { it.first.id == jogador.id }
                        val confirmado = dadosPresenca != null
                        val ordem = if (confirmado) presencaOrdenada.indexOfFirst { it.first.id == jogador.id } + 1 else null
                        
                        // Um jogador é considerado "rotacionado" (já jogou) se ele está na lista de presença
                        // mas seu timestamp é muito recente (foi atualizado na rotatividade).
                        // Para simplificar, na UI, se ele estiver confirmado mas com uma ordem alta após ter jogado,
                        // ele aparecerá no final.
                        
                        ItemFilaChegada(
                            jogador = jogador,
                            confirmado = confirmado,
                            ordem = ordem,
                            onToggle = { onConfirmarPresenca(jogador, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemFilaChegada(jogador: Jogador, confirmado: Boolean, ordem: Int?, onToggle: (Boolean) -> Unit) {
    // Definimos um efeito visual de "preto e branco" (esmaecido) se o jogador estiver muito longe na fila (indicativo de que já jogou)
    // Ou podemos simplesmente usar a lógica de que se ele tem ordem e ela é maior que o número de jogadores ativos.
    val isGrayedOut = confirmado && (ordem ?: 0) > 22 // Exemplo: se ele está depois dos 22 que costumam estar em campo

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isGrayedOut) 0.6f else 1f),
        shape = MaterialTheme.shapes.small, 
        color = if (isGrayedOut) Color(0xFFF5F5F5) else Color.White
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmado, onCheckedChange = onToggle)
            Text(
                text = if (confirmado) "${ordem}º" else "-",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(30.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = if (confirmado) {
                    if (isGrayedOut) Color.Gray else Color(0xFF4B0082)
                } else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogador.nome, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Medium,
                    color = if (isGrayedOut) Color.Gray else Color.Unspecified
                )
                Text(
                    text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small, 
                color = if (isGrayedOut) Color(0xFFE0E0E0) else Color(0xFFF0EDFF)
            ) {
                Text(
                    text = "#${jogador.numeroCamisa}", 
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = if (isGrayedOut) Color.DarkGray else Color(0xFF4B0082), 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ... Restante do arquivo (TimeCardEscalacao, HomeFabMenu, FabMenuItem) permanece igual ...
@Composable
private fun TimeCardEscalacao(titulo: String, corFundo: Color, jogadores: List<Jogador>, onAdicionar: () -> Unit, onRemover: (Jogador) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().height(280.dp), shape = MaterialTheme.shapes.medium, color = corFundo) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${jogadores.size}/11", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (jogadores.size < 11) {
                OutlinedButton(onClick = onAdicionar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Adicionar")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(jogadores) { jogador ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraSmall, color = Color.White) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onRemover(jogador) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFabMenu(expanded: Boolean, onToggle: () -> Unit, onNavigateToHistorico: () -> Unit, onNavigateToGerenciarJogadores: () -> Unit) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(visible = expanded) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FabMenuItem("Histórico", Icons.Default.History, onNavigateToHistorico)
                FabMenuItem("Jogadores", Icons.Default.People, onNavigateToGerenciarJogadores)
            }
        }
        FloatingActionButton(onClick = onToggle, containerColor = Color(0xFF4B0082), contentColor = Color.White) {
            Icon(if (expanded) Icons.Default.Close else Icons.Default.Menu, contentDescription = null)
        }
    }
}

@Composable
private fun FabMenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 2.dp) {
            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
        }
        SmallFloatingActionButton(onClick = onClick, containerColor = Color.White) { Icon(icon, contentDescription = null) }
    }
}
