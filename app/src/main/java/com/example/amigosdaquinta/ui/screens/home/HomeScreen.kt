package com.example.amigosdaquinta.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToGerenciarJogadores: () -> Unit = {},
    onNavigateToJogo: () -> Unit = {},
    onNavigateToFormacao: () -> Unit = {}
) {
    val context = LocalContext.current
    val jogadoresBanco by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()
    val temJogoAtivo by sessaoViewModel.temJogoAtivo.collectAsState()
    val numeroJogo by sessaoViewModel.numeroDoProximoJogo.collectAsState()
    val timeBrancoSessao by sessaoViewModel.timeBrancoAtual.collectAsState()
    val timeVermelhoSessao by sessaoViewModel.timeVermelhoAtual.collectAsState()
    val timeBrancoLocal by sessaoViewModel.timeBrancoEscalacaoManual.collectAsState()
    val timeVermelhoLocal by sessaoViewModel.timeVermelhoEscalacaoManual.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showFabMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val permitirEdicao = !temJogoAtivo && numeroJogo == 1
    val timeBrancoExibido = if (permitirEdicao) timeBrancoLocal else timeBrancoSessao
    val timeVermelhoExibido = if (permitirEdicao) timeVermelhoLocal else timeVermelhoSessao

    BackHandler {
        showExitDialog = true
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(300)
            viewModel.buscarPorNome(searchQuery)
        } else {
            viewModel.buscarPorNome("") // Recarrega todos ao limpar
        }
    }

    // ✅ REGRA: Ordem de chegada absoluta (FIFO) - Não filtra por pesquisa
    val jogadoresExibidos by remember(listaPresenca) {
        derivedStateOf {
            listaPresenca
                .distinctBy { it.first.id }
                .sortedBy { it.second }
                .map { it.first }
        }
    }

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
            FilaChegadaCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                jogadores = jogadoresExibidos,
                jogadoresResultados = jogadoresBanco,
                listaPresenca = listaPresenca,
                timeBranco = timeBrancoExibido,
                timeVermelho = timeVermelhoExibido,
                isLoading = isLoading,
                searchQuery = searchQuery,
                permitirEdicao = permitirEdicao,
                onSearchChange = { searchQuery = it },
                onConfirmarPresenca = { jogador, confirmado ->
                    if (confirmado) {
                        sessaoViewModel.adicionarAListaPresenca(jogador)
                        searchQuery = ""
                    } else {
                        sessaoViewModel.removerDaListaPresenca(jogador.id)
                    }
                },
                onAdicionarAoTime = { jogador, time ->
                    if (!permitirEdicao) return@FilaChegadaCard
                    
                    val timeAlvo = if (time == "BRANCO") timeBrancoLocal else timeVermelhoLocal
                    val jaNoOutroTime = (if (time == "BRANCO") timeVermelhoLocal else timeBrancoLocal).any { it.id == jogador.id }
                    val jaNoMesmoTime = timeAlvo.any { it.id == jogador.id }

                    if (!jaNoOutroTime && !jaNoMesmoTime) {
                        val temGoleiro = timeAlvo.any { it.isPosicaoGoleiro }
                        val qtdLinha = timeAlvo.count { !it.isPosicaoGoleiro }
                        
                        val podeAdicionar = if (jogador.isPosicaoGoleiro) !temGoleiro else qtdLinha < 10
                        
                        if (podeAdicionar) {
                            sessaoViewModel.adicionarAoTimeManual(jogador, if (time == "BRANCO") TimeColor.BRANCO else TimeColor.VERMELHO)
                            searchQuery = ""
                        }
                    }
                }
            )

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val timeVermelhoOrdenado = remember(timeVermelhoExibido) {
                    timeVermelhoExibido.distinctBy { it.id }.sortedByDescending { it.isPosicaoGoleiro }
                }
                TimeCardEscalacao(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    titulo = "Time Vermelho",
                    corFundo = Color(0xFFFFE1E1),
                    jogadores = timeVermelhoOrdenado,
                    permitirEdicao = permitirEdicao,
                    onRemover = { jogador -> 
                        if (permitirEdicao) sessaoViewModel.removerDoTimeManual(jogador.id) 
                    }
                )

                val timeBrancoOrdenado = remember(timeBrancoExibido) {
                    timeBrancoExibido.distinctBy { it.id }.sortedByDescending { it.isPosicaoGoleiro }
                }
                TimeCardEscalacao(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    titulo = "Time Branco",
                    corFundo = Color(0xFFE8E2FF),
                    jogadores = timeBrancoOrdenado,
                    permitirEdicao = permitirEdicao,
                    onRemover = { jogador -> 
                        if (permitirEdicao) sessaoViewModel.removerDoTimeManual(jogador.id) 
                    }
                )

                // ✅ NOVA REGRA: Mínimo 2 jogadores em cada time (4 no total)
                val podeIniciar = timeBrancoLocal.size >= 2 && timeVermelhoLocal.size >= 2
                
                Button(
                    onClick = {
                        if (temJogoAtivo) {
                            onNavigateToJogo()
                        } else if (numeroJogo > 1) {
                            onNavigateToJogo() 
                        } else {
                            // Escalação manual do primeiro jogo: envia os times para o ViewModel e vai para o preview
                            sessaoViewModel.criarJogo(timeBrancoLocal, timeVermelhoLocal)
                            onNavigateToFormacao()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = temJogoAtivo || podeIniciar || (numeroJogo > 1 && !permitirEdicao),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (temJogoAtivo) Color(0xFF2E7D32) else Color(0xFF4B0082)
                    )
                ) {
                    Icon(if (temJogoAtivo) Icons.Default.PlayArrow else Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when {
                            temJogoAtivo -> "VOLTAR PRO JOGO"
                            numeroJogo > 1 -> "CONTINUAR SESSÃO"
                            else -> "INICIAR SESSÃO"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

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
    modifier: Modifier = Modifier,
    jogadores: List<Jogador>,
    jogadoresResultados: List<Jogador>,
    listaPresenca: List<Pair<Jogador, Long>>,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    isLoading: Boolean,
    searchQuery: String,
    permitirEdicao: Boolean,
    onSearchChange: (String) -> Unit,
    onConfirmarPresenca: (Jogador, Boolean) -> Unit,
    onAdicionarAoTime: (Jogador, String) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text("Lista de Chegada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar jogador...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty() && jogadoresResultados.isNotEmpty()) {
                            val primeiroJogador = jogadoresResultados.firstOrNull()
                            if (primeiroJogador != null) {
                                IconButton(onClick = { onConfirmarPresenca(primeiroJogador, true); onSearchChange("") }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Adicionar", tint = Color(0xFF4B0082))
                                }
                            }
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, "Limpar")
                            }
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading && searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                // ✅ REGRA: A lista sempre mostra quem já confirmou presença (Lista de Chegada)
                // Ela não deve ser filtrada enquanto o usuário digita.
                val listaParaExibir = remember(jogadores) { jogadores.distinctBy { it.id } }

                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = listaParaExibir, key = { it.id }) { jogador ->
                        val indexOriginal = jogadores.indexOfFirst { it.id == jogador.id }
                        val jaConfirmado = true // Se está em 'jogadores', já está confirmado
                        val ordem = if (indexOriginal != -1) indexOriginal + 1 else null
                        val noTime = timeBranco.any { it.id == jogador.id } || timeVermelho.any { it.id == jogador.id }
                        
                        // Lógica de barramento (Botões Cinzas)
                        val podeAddBranco: Boolean
                        val podeAddVermelho: Boolean
                        
                        if (noTime) {
                            podeAddBranco = false
                            podeAddVermelho = false
                        } else {
                            val temGolB = timeBranco.any { it.isPosicaoGoleiro }
                            val linB = timeBranco.count { !it.isPosicaoGoleiro }
                            podeAddBranco = if (jogador.isPosicaoGoleiro) !temGolB else linB < 10

                            val temGolV = timeVermelho.any { it.isPosicaoGoleiro }
                            val linV = timeVermelho.count { !it.isPosicaoGoleiro }
                            podeAddVermelho = if (jogador.isPosicaoGoleiro) !temGolV else linV < 10
                        }

                        ItemFilaChegada(
                            jogador = jogador,
                            confirmado = jaConfirmado,
                            noTime = noTime,
                            ordem = ordem,
                            permitirEdicao = permitirEdicao,
                            podeAddBranco = podeAddBranco,
                            podeAddVermelho = podeAddVermelho,
                            onToggle = { onConfirmarPresenca(jogador, it) },
                            onAdicionarBranco = { onAdicionarAoTime(jogador, "BRANCO") },
                            onAdicionarVermelho = { onAdicionarAoTime(jogador, "VERMELHO") }
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
    noTime: Boolean,
    ordem: Int?,
    permitirEdicao: Boolean,
    podeAddBranco: Boolean,
    podeAddVermelho: Boolean,
    onToggle: (Boolean) -> Unit,
    onAdicionarBranco: () -> Unit,
    onAdicionarVermelho: () -> Unit
) {
    val backgroundColor = if (noTime) Color(0xFFE0E0E0) else Color.White

    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = backgroundColor) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmado, onCheckedChange = onToggle, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = if (confirmado && ordem != null) "${ordem}°" else "-", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(28.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if (confirmado) Color(0xFF4B0082) else Color.Gray)
            Spacer(modifier = Modifier.width(3.dp))
            Column(modifier = Modifier.weight(1f)) { 
                Text(
                    text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Medium, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                ) 
            }
            Spacer(modifier = Modifier.width(3.dp))
            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFF0EDFF)) { Text("#${jogador.numeroCamisa}", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4B0082), fontWeight = FontWeight.Bold) }
            if (permitirEdicao) {
                Spacer(modifier = Modifier.width(6.dp))
                Text("|", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "V", 
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp), 
                    fontWeight = FontWeight.ExtraBold, 
                    color = if (podeAddVermelho) Color.Red else Color.LightGray, 
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable(enabled = podeAddVermelho, onClick = onAdicionarVermelho)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "B", 
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp), 
                    fontWeight = FontWeight.ExtraBold, 
                    color = if (podeAddBranco) Color(0xFF4B0082) else Color.LightGray, 
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable(enabled = podeAddBranco, onClick = onAdicionarBranco)
                )
            }
        }
    }
}

@Composable
private fun TimeCardEscalacao(
    modifier: Modifier = Modifier,
    titulo: String,
    corFundo: Color,
    jogadores: List<Jogador>,
    permitirEdicao: Boolean,
    onRemover: (Jogador) -> Unit
) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, color = corFundo) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${jogadores.size}/11", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            val jogadoresUnicos = remember(jogadores) { jogadores.distinctBy { it.id } }
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(items = jogadoresUnicos, key = { it.id }) { jogador ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraSmall, color = Color.White) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (permitirEdicao) {
                                IconButton(onClick = { onRemover(jogador) }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFabMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToHistorico: () -> Unit,
    onNavigateToGerenciarJogadores: () -> Unit
) {
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
        Surface(color = Color.White, shape = MaterialTheme.shapes.small, shadowElevation = 2.dp) { Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge) }
        SmallFloatingActionButton(onClick = onClick, containerColor = Color.White) { Icon(icon, contentDescription = null) }
    }
}
