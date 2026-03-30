package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoScreen(
    sessaoViewModel: SessaoViewModel,
    jogadoresViewModel: JogadoresViewModel,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onFinalizarJogo: (TimeColor?) -> Unit
) {
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()
    val jogadoresBanco by jogadoresViewModel.jogadores.collectAsState()
    val isLoading by jogadoresViewModel.isLoading.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()
    val tempoRestanteSegundos by sessaoViewModel.tempoRestanteSegundos.collectAsState()
    val timerPausado by sessaoViewModel.timerPausado.collectAsState()
    val substituidoresIds by sessaoViewModel.jogadoresSubstituidosIds.collectAsState()
    val entrouSubstitutoIds by sessaoViewModel.jogadoresQueEntraramSubstitutosIds.collectAsState()
    val numeroJogo by sessaoViewModel.numeroDoProximoJogo.collectAsState()

    var showFinalizarDialog by remember { mutableStateOf(false) }
    var showEmpateDialog by remember { mutableStateOf(false) }
    var finalizacaoAutomatica by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSubDialog by remember { mutableStateOf(false) }
    var jogadorParaSubstituir by remember { mutableStateOf<Pair<Jogador, TimeColor>?>(null) }

    LaunchedEffect(Unit) {
        sessaoViewModel.eventoTempoEsgotado.collect {
            finalizacaoAutomatica = true
            showFinalizarDialog = true
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(300)
            jogadoresViewModel.buscarPorNome(searchQuery)
        }
    }

    // ✅ REGRA: A lista lateral segue estritamente a ordem de chegada (FIFO)
    val jogadoresExibidos by remember(listaPresenca, searchQuery) {
        derivedStateOf {
            val baseList = listaPresenca.sortedBy { it.second }.map { it.first }
            
            if (searchQuery.isBlank()) {
                baseList
            } else {
                baseList.filter { j -> 
                    j.nome.contains(searchQuery, ignoreCase = true) || 
                    j.numeroCamisa.toString().contains(searchQuery) 
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partida em Andamento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar pra Home")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilaEsperaLateral(
                modifier = Modifier.weight(0.3f),
                jogadores = jogadoresExibidos,
                jogadoresResultados = jogadoresBanco,
                listaPresenca = listaPresenca,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho,
                substituidoresIds = substituidoresIds,
                isLoading = isLoading,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onTogglePresenca = { jogador, jaConfirmado ->
                    if (jaConfirmado) {
                        sessaoViewModel.removerDaListaPresenca(jogador.id)
                    } else {
                        sessaoViewModel.adicionarAListaPresenca(jogador)
                        searchQuery = ""
                    }
                }
            )

            Column(
                modifier = Modifier.weight(0.7f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlacarComponent(
                    placarBranco = placarBranco,
                    placarVermelho = placarVermelho,
                    tempoRestanteSegundos = tempoRestanteSegundos,
                    pausado = timerPausado,
                    duracaoMinutos = if (numeroJogo == 1) 30 else 15,
                    onGolBranco = { sessaoViewModel.incrementarPlacarBranco() },
                    onGolVermelho = { sessaoViewModel.incrementarPlacarVermelho() },
                    onAlternarPausa = { sessaoViewModel.alternarPausaTimer() },
                    modifier = Modifier.fillMaxWidth().weight(0.18f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().weight(0.76f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EscalacaoAtivaCard(
                        titulo = "TIME VERMELHO",
                        jogadores = timeVermelho,
                        containerColor = Color(0xFFFFE1E1),
                        substituidoresIds = substituidoresIds,
                        entrouSubstitutoIds = entrouSubstitutoIds,
                        onSubstituir = {
                            jogadorParaSubstituir = Pair(it, TimeColor.VERMELHO)
                            showSubDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )

                    EscalacaoAtivaCard(
                        titulo = "TIME BRANCO",
                        jogadores = timeBranco,
                        containerColor = Color(0xFFE8E2FF),
                        substituidoresIds = substituidoresIds,
                        entrouSubstitutoIds = entrouSubstitutoIds,
                        onSubstituir = {
                            jogadorParaSubstituir = Pair(it, TimeColor.BRANCO)
                            showSubDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        finalizacaoAutomatica = false
                        showFinalizarDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().weight(0.06f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
                ) {
                    Text("ENCERRAR PARTIDA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSubDialog && jogadorParaSubstituir != null) {
        val jogadoresPresentesDisponiveis = listaPresenca
            .map { it.first }
            .filter { j ->
                j.id != jogadorParaSubstituir!!.first.id &&
                !timeBranco.any { it.id == j.id && it.id !in substituidoresIds } &&
                !timeVermelho.any { it.id == j.id && it.id !in substituidoresIds }
            }

        SubstituicaoDialog(
            jogadorSaindo = jogadorParaSubstituir!!.first,
            time = jogadorParaSubstituir!!.second,
            jogadoresDisponiveis = jogadoresPresentesDisponiveis,
            onDismiss = { showSubDialog = false },
            onConfirm = { entrando, lesionado ->
                sessaoViewModel.substituirJogador(jogadorParaSubstituir!!.first, entrando, jogadorParaSubstituir!!.second, lesionado)
                showSubDialog = false
            }
        )
    }

    if (showFinalizarDialog) {
        ConfirmarResultadoDialog(
            placarBranco = placarBranco,
            placarVermelho = placarVermelho,
            isAutomatico = finalizacaoAutomatica,
            onDismiss = { if (!finalizacaoAutomatica) showFinalizarDialog = false },
            onConfirm = {
                if (placarBranco == placarVermelho && numeroJogo == 1) {
                    showFinalizarDialog = false
                    showEmpateDialog = true
                } else {
                    val vencedor = when {
                        placarBranco > placarVermelho -> TimeColor.BRANCO
                        placarVermelho > placarBranco -> TimeColor.VERMELHO
                        else -> null 
                    }
                    sessaoViewModel.finalizarJogo(vencedor)
                    onFinalizarJogo(vencedor)
                    showFinalizarDialog = false
                }
            }
        )
    }

    if (showEmpateDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Empate no 1º Jogo") },
            text = { Text("Quem permanece em campo para a próxima partida?") },
            confirmButton = {
                TextButton(onClick = {
                    sessaoViewModel.finalizarJogo(TimeColor.BRANCO)
                    onFinalizarJogo(TimeColor.BRANCO)
                    showEmpateDialog = false
                }) { Text("TIME BRANCO") }
            },
            dismissButton = {
                TextButton(onClick = {
                    sessaoViewModel.finalizarJogo(TimeColor.VERMELHO)
                    onFinalizarJogo(TimeColor.VERMELHO)
                    showEmpateDialog = false
                }) { Text("TIME VERMELHO") }
            }
        )
    }
}

@Composable
private fun FilaEsperaLateral(
    modifier: Modifier = Modifier,
    jogadores: List<Jogador>,
    jogadoresResultados: List<Jogador>,
    listaPresenca: List<Pair<Jogador, Long>>,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    substituidoresIds: Set<Long>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onTogglePresenca: (Jogador, Boolean) -> Unit
) {
    Surface(modifier = modifier.fillMaxHeight(), shape = MaterialTheme.shapes.medium, color = Color(0xFFEBE8EC)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "Fila de Presença", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty() && jogadoresResultados.isNotEmpty()) {
                            val primeiroJogador = jogadoresResultados.firstOrNull()
                            if (primeiroJogador != null && !listaPresenca.any { it.first.id == primeiroJogador.id }) {
                                IconButton(onClick = { onTogglePresenca(primeiroJogador, false); onSearchChange("") }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Adicionar", tint = Color(0xFF4B0082))
                                }
                            }
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val listaTotalOrdenada = remember(listaPresenca) { listaPresenca.sortedBy { it.second }.map { it.first.id } }
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                    items(jogadores, key = { it.id }) { jogador ->
                        val indexGlobal = listaTotalOrdenada.indexOf(jogador.id)
                        val ordem = if (indexGlobal != -1) indexGlobal + 1 else null
                        val noTime = (timeBranco + timeVermelho).any { it.id == jogador.id && it.id !in substituidoresIds }
                        
                        FilaJogadorItem(
                            jogador = jogador,
                            confirmado = true,
                            noTime = noTime,
                            ordem = ordem,
                            onToggle = { onTogglePresenca(jogador, true) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilaJogadorItem(jogador: Jogador, confirmado: Boolean, noTime: Boolean, ordem: Int?, onToggle: () -> Unit) {
    val backgroundColor = when {
        noTime -> Color(0xFFE0E0E0)
        else -> Color.White
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = backgroundColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmado, onCheckedChange = { onToggle() }, modifier = Modifier.size(24.dp), enabled = !noTime)
            
            Spacer(modifier = Modifier.width(4.dp))
            
            if (ordem != null) {
                Text(
                    text = "${ordem}º",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4B0082),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(24.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = (if (jogador.isPosicaoGoleiro) "[GOL] " else "") + jogador.nome, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            Surface(shape = MaterialTheme.shapes.extraSmall, color = Color(0xFFF0EDFF)) {
                Text(
                    "#${jogador.numeroCamisa}",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4B0082),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun EscalacaoAtivaCard(
    titulo: String,
    jogadores: List<Jogador>,
    containerColor: Color,
    substituidoresIds: Set<Long>,
    entrouSubstitutoIds: Set<Long>,
    onSubstituir: (Jogador) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ REGRA: GOLEIRO SEMPRE NO TOPO DA ESCALAÇÃO
    val jogadoresOrdenados = remember(jogadores) {
        jogadores.sortedByDescending { it.isPosicaoGoleiro }
    }

    Card(modifier = modifier.fillMaxHeight(), colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 4.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxSize()) {
                items(jogadoresOrdenados, key = { it.id }) { jogador ->
                    val jaSaiu = substituidoresIds.contains(jogador.id)
                    val eSubstituto = entrouSubstitutoIds.contains(jogador.id)
                    Surface(modifier = Modifier.fillMaxWidth().alpha(if (jaSaiu) 0.5f else 1f), shape = MaterialTheme.shapes.extraSmall, color = Color.White.copy(alpha = 0.7f)) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${jogador.numeroCamisa}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                            
                            Text((if (jogador.isPosicaoGoleiro) "[GOL] " else "") + jogador.nome, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            
                            if (jaSaiu) {
                                Text("(S)", style = MaterialTheme.typography.labelSmall, color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                            if (eSubstituto) {
                                Text("(E)", style = MaterialTheme.typography.labelSmall, color = Color.Blue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }

                            if (!jaSaiu) {
                                IconButton(onClick = { onSubstituir(jogador) }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
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
fun ConfirmarResultadoDialog(placarBranco: Int, placarVermelho: Int, isAutomatico: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (isAutomatico) "Tempo Esgotado!" else "Encerrar Partida") }, text = { Text("Confirmar placar final: BRANCO $placarBranco x $placarVermelho VERMELHO?") }, confirmButton = { Button(onClick = onConfirm) { Text("CONFIRMAR") } }, dismissButton = { if (!isAutomatico) { TextButton(onClick = onDismiss) { Text("CANCELAR") } } })
}
