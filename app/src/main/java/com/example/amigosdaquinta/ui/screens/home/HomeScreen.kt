package com.example.amigosdaquinta.ui.screens.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amigosdaquinta.data.local.entity.Jogador
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
    val context = LocalContext.current
    val jogadoresBanco by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var timeBranco by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var timeVermelho by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var modoRapido by rememberSaveable { mutableStateOf(false) }

    // Intercepta o botão voltar do sistema
    BackHandler {
        showExitDialog = true
    }

    // Dispara a busca no banco apenas quando o texto muda
    LaunchedEffect(searchQuery) {
        viewModel.buscarPorNome(searchQuery)
    }

    // Lógica de exibição refinada para evitar "piscadas"
    val jogadoresExibidos by remember(jogadoresBanco, listaPresenca, searchQuery, modoRapido) {
        derivedStateOf {
            if (modoRapido || searchQuery.isBlank()) {
                // No modo rápido ou sem busca, mostra apenas a fila de presença
                listaPresenca.sortedBy { it.second }.map { it.first }
            } else {
                // Modo Busca Normal: Resultados do banco
                val presentesIds = listaPresenca.map { it.first.id }.toSet()
                jogadoresBanco.sortedWith(
                    compareByDescending<Jogador> { presentesIds.contains(it.id) }
                    .thenByDescending { it.numeroCamisa }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos da Quinta", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (modoRapido) "Modo Rápido" else "Modo Normal",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (modoRapido) Color(0xFF4B0082) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = modoRapido,
                            onCheckedChange = { modoRapido = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4B0082),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
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
            // COLUNA 1: FILA / BUSCA
            Column(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
                FilaChegadaCard(
                    jogadores = jogadoresExibidos,
                    jogadoresResultados = jogadoresBanco,
                    listaPresenca = listaPresenca,
                    timeBranco = timeBranco,
                    timeVermelho = timeVermelho,
                    isLoading = isLoading,
                    searchQuery = searchQuery,
                    modoRapido = modoRapido,
                    onSearchChange = { searchQuery = it },
                    onConfirmarPresenca = { jogador, confirmado ->
                        if (confirmado) {
                            sessaoViewModel.adicionarAListaPresenca(jogador)
                            searchQuery = "" // Limpa busca ao selecionar
                        } else {
                            sessaoViewModel.removerDaListaPresenca(jogador.id)
                        }
                    },
                    onAdicionarAoTime = { jogador, time ->
                        sessaoViewModel.adicionarAListaPresenca(jogador)
                        searchQuery = "" 
                        
                        if (time == "BRANCO") {
                            if (timeBranco.size < 11 && !timeBranco.any { it.id == jogador.id } && !timeVermelho.any { it.id == jogador.id }) {
                                if (jogador.isPosicaoGoleiro) {
                                    if (!timeBranco.any { it.isPosicaoGoleiro }) {
                                        timeBranco = timeBranco + jogador
                                    }
                                } else {
                                    if (timeBranco.count { !it.isPosicaoGoleiro } < 10) {
                                        timeBranco = timeBranco + jogador
                                    }
                                }
                            }
                        } else {
                            if (timeVermelho.size < 11 && !timeVermelho.any { it.id == jogador.id } && !timeBranco.any { it.id == jogador.id }) {
                                if (jogador.isPosicaoGoleiro) {
                                    if (!timeVermelho.any { it.isPosicaoGoleiro }) {
                                        timeVermelho = timeVermelho + jogador
                                    }
                                } else {
                                    if (timeVermelho.count { !it.isPosicaoGoleiro } < 10) {
                                        timeVermelho = timeVermelho + jogador
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // COLUNA 2: TIMES
            Column(
                modifier = Modifier.weight(0.6f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimeCardEscalacao(
                    titulo = "Time Vermelho",
                    corFundo = Color(0xFFFFE1E1),
                    jogadores = timeVermelho,
                    onRemover = { jogador ->
                        timeVermelho = timeVermelho.filter { it.id != jogador.id }
                    }
                )

                TimeCardEscalacao(
                    titulo = "Time Branco",
                    corFundo = Color(0xFFE8E2FF),
                    jogadores = timeBranco,
                    onRemover = { jogador ->
                        timeBranco = timeBranco.filter { it.id != jogador.id }
                    }
                )

                if (timeBranco.size == 11 && timeVermelho.size == 11) {
                    Button(
                        onClick = {
                            val todosJogadores = (timeBranco + timeVermelho).distinct()
                            todosJogadores.forEach { sessaoViewModel.adicionarAListaPresenca(it) }
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

    // Diálogo de Confirmação de Saída
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Sair do Programa", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente fechar o Amigos da Quinta?") },
            confirmButton = {
                Button(
                    onClick = { (context as? android.app.Activity)?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SAIR", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun FilaChegadaCard(
    jogadores: List<Jogador>,
    jogadoresResultados: List<Jogador>,
    listaPresenca: List<Pair<Jogador, Long>>,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    isLoading: Boolean,
    searchQuery: String,
    modoRapido: Boolean,
    onSearchChange: (String) -> Unit,
    onConfirmarPresenca: (Jogador, Boolean) -> Unit,
    onAdicionarAoTime: (Jogador, String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (modoRapido || searchQuery.isBlank()) "Fila de Presença" else "Resultados da Busca", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nome ou número...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Default.Close, null) }
                        }
                        if (modoRapido && searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    if (jogadoresResultados.isNotEmpty()) {
                                        onConfirmarPresenca(jogadoresResultados.first(), true)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Confirmar", tint = Color(0xFF4B0082))
                            }
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && jogadores.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presencaOrdenada = listaPresenca.sortedBy { it.second }
                    
                    val linhaBCount = timeBranco.count { !it.isPosicaoGoleiro }
                    val linhaVCount = timeVermelho.count { !it.isPosicaoGoleiro }
                    val jaTemGoleiroB = timeBranco.any { it.isPosicaoGoleiro }
                    val jaTemGoleiroV = timeVermelho.any { it.isPosicaoGoleiro }

                    items(items = jogadores, key = { it.id }) { jogador ->
                        val dadosPresenca = listaPresenca.find { it.first.id == jogador.id }
                        val confirmado = dadosPresenca != null
                        val ordem = if (confirmado) presencaOrdenada.indexOfFirst { it.first.id == jogador.id } + 1 else null
                        val jaEscalado = timeBranco.any { it.id == jogador.id } || timeVermelho.any { it.id == jogador.id }
                        
                        val bloquearB = if (jogador.isPosicaoGoleiro) jaTemGoleiroB else linhaBCount >= 10
                        val bloquearV = if (jogador.isPosicaoGoleiro) jaTemGoleiroV else linhaVCount >= 10

                        ItemFilaChegada(
                            jogador = jogador,
                            confirmado = confirmado,
                            ordem = ordem,
                            jaEscalado = jaEscalado,
                            bloquearB = bloquearB,
                            bloquearV = bloquearV,
                            onToggle = { onConfirmarPresenca(jogador, it) },
                            onAddV = { onAdicionarAoTime(jogador, "VERMELHO") },
                            onAddB = { onAdicionarAoTime(jogador, "BRANCO") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemFilaChegada(
    jogador: Jogador, 
    confirmado: Boolean, 
    ordem: Int?, 
    jaEscalado: Boolean,
    bloquearB: Boolean,
    bloquearV: Boolean,
    onToggle: (Boolean) -> Unit,
    onAddV: () -> Unit,
    onAddB: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (jaEscalado) 0.7f else 1f),
        shape = MaterialTheme.shapes.small, 
        color = if (jaEscalado) Color(0xFFF0F0F0) else Color.White
    ) {
        Row(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmado, onCheckedChange = onToggle)
            
            Text(
                text = if (confirmado) "${ordem}º" else "-",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(28.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = if (confirmado) Color(0xFF4B0082) else Color.Gray
            )
            
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    text = jogador.nome, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (jaEscalado) Color.Gray else Color.Black
                )
                Text(
                    text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small, 
                    color = Color(0xFFF0EDFF)
                ) {
                    Text(
                        text = "#${jogador.numeroCamisa}", 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color(0xFF4B0082), 
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text("|", color = Color.LightGray, fontSize = 20.sp)
                
                // Botão Vermelho
                IconButton(
                    onClick = onAddV,
                    enabled = !jaEscalado && !bloquearV,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("V", color = if (jaEscalado || bloquearV) Color.Gray else Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                
                // Botão Branco
                IconButton(
                    onClick = onAddB,
                    enabled = !jaEscalado && !bloquearB,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("B", color = if (jaEscalado || bloquearB) Color.Gray else Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun TimeCardEscalacao(
    titulo: String, 
    corFundo: Color, 
    jogadores: List<Jogador>, 
    onRemover: (Jogador) -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth().height(280.dp), shape = MaterialTheme.shapes.medium, color = corFundo) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${jogadores.size}/11", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(jogadores, key = { it.id }) { jogador ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraSmall, color = Color.White) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = (if (jogador.isPosicaoGoleiro) "[GOL] " else "") + jogador.nome, 
                                style = MaterialTheme.typography.bodySmall, 
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(onClick = { onRemover(jogador) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
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
