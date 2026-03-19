package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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

/**
 * Tela de Jogo em Andamento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoScreen(
    sessaoViewModel: SessaoViewModel,
    jogadoresViewModel: JogadoresViewModel,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    onNavigateBack: () -> Unit,
    onFinalizarJogo: (TimeColor?) -> Unit
) {
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()
    val jogadoresBanco by jogadoresViewModel.jogadores.collectAsState()
    val isLoading by jogadoresViewModel.isLoading.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()
    val duracaoMinutos by sessaoViewModel.duracaoJogoAtualMinutos.collectAsState()
    val substituidoresIds by sessaoViewModel.jogadoresSubstituidosIds.collectAsState()
    val entrouSubstitutoIds by sessaoViewModel.jogadoresQueEntraramSubstitutosIds.collectAsState()

    var showFinalizarDialog by remember { mutableStateOf(false) }
    var finalizacaoAutomatica by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSubDialog by remember { mutableStateOf(false) }
    var jogadorParaSubstituir by remember { mutableStateOf<Pair<Jogador, TimeColor>?>(null) }
    var modoRapido by rememberSaveable { mutableStateOf(false) }

    // Dispara a busca no banco apenas quando o texto muda
    LaunchedEffect(searchQuery) {
        jogadoresViewModel.buscarPorNome(searchQuery)
    }

    // Lógica de exibição UNIFICADA com a HomeScreen
    val jogadoresExibidos by remember(jogadoresBanco, listaPresenca, searchQuery, modoRapido) {
        derivedStateOf {
            if (modoRapido || searchQuery.isBlank()) {
                listaPresenca.sortedBy { it.second }.map { it.first }
            } else {
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
                title = { Text("Partida em Andamento", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
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
                                checkedTrackColor = Color(0xFF4B0082)
                            )
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // COLUNA LATERAL: FILA DE ESPERA
            FilaEsperaLateral(
                modifier = Modifier.weight(0.35f),
                jogadores = jogadoresExibidos,
                jogadoresResultados = jogadoresBanco,
                listaPresenca = listaPresenca,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho,
                substituidoresIds = substituidoresIds,
                isLoading = isLoading,
                searchQuery = searchQuery,
                modoRapido = modoRapido,
                onSearchChange = { searchQuery = it },
                onTogglePresenca = { jogador, jaConfirmado ->
                    if (jaConfirmado) {
                        sessaoViewModel.removerDaListaPresenca(jogador.id)
                    } else {
                        sessaoViewModel.adicionarAListaPresenca(jogador)
                        if (modoRapido) searchQuery = ""
                    }
                }
            )

            // COLUNA CENTRAL: PLACAR E TIMES
            Column(modifier = Modifier.weight(0.65f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PlacarComponent(
                    placarBranco = placarBranco,
                    placarVermelho = placarVermelho,
                    duracaoMinutos = duracaoMinutos,
                    onGolBranco = { sessaoViewModel.incrementarPlacarBranco() },
                    onGolVermelho = { sessaoViewModel.incrementarPlacarVermelho() },
                    onTempoEsgotado = {
                        finalizacaoAutomatica = true
                        showFinalizarDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().weight(0.35f)
                )

                Row(modifier = Modifier.fillMaxWidth().weight(0.55f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
                ) {
                    Text("ENCERRAR PARTIDA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialogs
    if (showSubDialog && jogadorParaSubstituir != null) {
        val presentesIds = listaPresenca.map { it.first.id }.toSet()
        val jogadoresPresentesDisponiveis = jogadoresBanco.filter { j -> 
            presentesIds.contains(j.id) && 
            !timeBranco.any { it.id == j.id } && 
            !timeVermelho.any { it.id == j.id } 
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
                val vencedor = when {
                    placarBranco > placarVermelho -> TimeColor.BRANCO
                    placarVermelho > placarBranco -> TimeColor.VERMELHO
                    else -> null
                }
                sessaoViewModel.finalizarJogo(vencedor)
                onFinalizarJogo(vencedor)
                showFinalizarDialog = false
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
    modoRapido: Boolean,
    onSearchChange: (String) -> Unit,
    onTogglePresenca: (Jogador, Boolean) -> Unit
) {
    Surface(modifier = modifier.fillMaxHeight(), shape = MaterialTheme.shapes.medium, color = Color(0xFFEBE8EC)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (modoRapido || searchQuery.isBlank()) "Fila de Presença" else "Busca", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar...") },
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
                                        onTogglePresenca(jogadoresResultados.first(), false)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Confirmar", tint = Color(0xFF4B0082))
                            }
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading && jogadores.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presencaOrdenada = listaPresenca.sortedBy { it.second }
                    items(items = jogadores, key = { it.id }) { jogador ->
                        val jaConfirmado = listaPresenca.any { it.first.id == jogador.id }
                        val foiSubstituido = substituidoresIds.contains(jogador.id)
                        val jaEscalado = timeBranco.any { it.id == jogador.id } || timeVermelho.any { it.id == jogador.id }
                        val ordem = if (jaConfirmado) presencaOrdenada.indexOfFirst { it.first.id == jogador.id } + 1 else null
                        
                        ItemFilaJogo(jogador, jaConfirmado, jaEscalado, foiSubstituido, ordem, onTogglePresenca)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemFilaJogo(
    jogador: Jogador, 
    confirmado: Boolean, 
    jaEscalado: Boolean, 
    substituido: Boolean, 
    ordem: Int?, 
    onToggle: (Jogador, Boolean) -> Unit
) {
    val isGrayedOut = jaEscalado || substituido

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isGrayedOut) 0.5f else 1f),
        shape = MaterialTheme.shapes.small,
        color = if (jaEscalado) Color(0xFFF0F0F0) else Color.White
    ) {
        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = confirmado, 
                onCheckedChange = { onToggle(jogador, confirmado) }, 
                enabled = !substituido && !jaEscalado, 
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = if (confirmado) "${ordem}º" else "-",
                style = MaterialTheme.typography.labelSmall,
                color = if (substituido) Color.Red else Color(0xFF4B0082),
                modifier = Modifier.width(24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(
                    text = jogador.nome, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold, 
                    color = if (jaEscalado) Color.Gray else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val statusText = when {
                    substituido -> "Saiu (S)"
                    jaEscalado -> "Em Jogo"
                    jogador.isPosicaoGoleiro -> "Goleiro"
                    else -> "Linha"
                }
                Text(statusText, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Surface(
                shape = MaterialTheme.shapes.small, 
                color = if (isGrayedOut) Color(0xFF616161) else Color(0xFFF0EDFF)
            ) {
                Text(
                    text = "#${jogador.numeroCamisa}", 
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = if (isGrayedOut) Color.White else Color(0xFF4B0082)
                )
            }
        }
    }
}

@Composable
private fun EscalacaoAtivaCard(titulo: String, jogadores: List<Jogador>, containerColor: Color, substituidoresIds: Set<Long>, entrouSubstitutoIds: Set<Long>, onSubstituir: (Jogador) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxHeight(), shape = MaterialTheme.shapes.medium, color = containerColor) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val ordenados = jogadores.sortedWith(
                compareBy<Jogador> { substituidoresIds.contains(it.id) }
                .thenBy { !it.isPosicaoGoleiro }
                .thenBy { entrouSubstitutoIds.contains(it.id) }
                .thenBy { it.nome }
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ordenados) { jog ->
                    val s = substituidoresIds.contains(jog.id)
                    val e = entrouSubstitutoIds.contains(jog.id)
                    Surface(modifier = Modifier.fillMaxWidth().alpha(if (s) 0.6f else 1f), shape = MaterialTheme.shapes.small, color = if (s) Color(0xFF424242) else Color.White) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val txt = (if (jog.isPosicaoGoleiro) "[GOL] " else "") + (if (e && !s) "(E) " else "") + jog.nome + (if (s) " (S)" else "")
                            Text(
                                text = txt, 
                                style = MaterialTheme.typography.bodySmall, 
                                modifier = Modifier.weight(1f), 
                                color = if (s) Color.White else Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("#${jog.numeroCamisa}", style = MaterialTheme.typography.bodySmall, color = if (s) Color.White else Color.Black)
                            if (!s) {
                                IconButton(onClick = { onSubstituir(jog) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.SwapHoriz, null, tint = Color(0xFF4B0082), modifier = Modifier.size(16.dp))
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
private fun ConfirmarResultadoDialog(placarBranco: Int, placarVermelho: Int, isAutomatico: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isAutomatico) "Tempo Esgotado!" else "Encerrar Partida") },
        text = { Text("O placar final foi $placarBranco x $placarVermelho. Deseja confirmar e processar a rotatividade dos times?") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))) { Text("Confirmar") } },
        dismissButton = if (!isAutomatico) { { TextButton(onClick = onDismiss) { Text("Cancelar") } } } else null
    )
}
