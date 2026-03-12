package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela de Jogo em Andamento.
 * 
 * Gerencia a partida em tempo real, permitindo registro de gols, substituições e 
 * controle de cronômetro. Apresenta uma visão lateral da fila de espera para substituições.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoScreen(
    sessaoViewModel: SessaoViewModel,
    jogadoresViewModel: JogadoresViewModel,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    onFinalizarJogo: (TimeColor?) -> Unit
) {
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()
    val jogadores by jogadoresViewModel.jogadores.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()
    val duracaoMinutos by sessaoViewModel.duracaoJogoAtualMinutos.collectAsState()
    val substituidoresIds by sessaoViewModel.jogadoresSubstituidosIds.collectAsState()
    val entrouSubstitutoIds by sessaoViewModel.jogadoresQueEntraramSubstitutosIds.collectAsState()

    var showFinalizarDialog by remember { mutableStateOf(false) }
    var finalizacaoAutomatica by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSubDialog by remember { mutableStateOf(false) }
    var jogadorParaSubstituir by remember { mutableStateOf<Pair<Jogador, TimeColor>?>(null) }

    // Filtragem e ordenação da fila de espera lateral
    val jogadoresFilaFiltrados by remember(jogadores, listaPresenca, searchQuery, substituidoresIds) {
        derivedStateOf {
            jogadores.filter { jog ->
                val matchSearch = jog.nome.contains(searchQuery, ignoreCase = true) || jog.numeroCamisa.toString().contains(searchQuery)
                matchSearch
            }.sortedWith(
                compareBy<Jogador> { substituidoresIds.contains(it.id) }
                .thenBy { !listaPresenca.any { p -> p.first.id == it.id } }
                .thenBy { it.nome }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partida em Andamento", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                jogadores = jogadoresFilaFiltrados,
                listaPresenca = listaPresenca,
                substituidoresIds = substituidoresIds,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onTogglePresenca = { jogador, jaConfirmado ->
                    if (jaConfirmado) sessaoViewModel.removerDaListaPresenca(jogador.id)
                    else sessaoViewModel.adicionarAListaPresenca(jogador)
                }
            )

            // COLUNA CENTRAL: PLACAR E TIMES
            Column(modifier = Modifier.fillMaxSize().weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Componente de Placar e Cronômetro
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
                    modifier = Modifier.fillMaxWidth().weight(0.3f)
                )

                // Cards de Escalação com Substituição
                Row(modifier = Modifier.fillMaxWidth().weight(0.6f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
        SubstituicaoDialog(
            jogadorSaindo = jogadorParaSubstituir!!.first,
            time = jogadorParaSubstituir!!.second,
            jogadoresDisponiveis = jogadores.filter { j -> 
                !timeBranco.any { it.id == j.id } && !timeVermelho.any { it.id == j.id } 
            },
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
    jogadores: List<Jogador>,
    listaPresenca: List<Pair<Jogador, Long>>,
    substituidoresIds: Set<Long>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onTogglePresenca: (Jogador, Boolean) -> Unit
) {
    Surface(modifier = Modifier.width(300.dp).fillMaxHeight(), shape = MaterialTheme.shapes.medium, color = Color(0xFFEBE8EC)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Fila de Espera", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = jogadores, key = { it.id }) { jogador ->
                    val jaConfirmado = listaPresenca.any { it.first.id == jogador.id }
                    val foiSubstituido = substituidoresIds.contains(jogador.id)
                    val ordem = if (jaConfirmado) listaPresenca.indexOfFirst { it.first.id == jogador.id } + 1 else null
                    
                    ItemFilaJogo(jogador, jaConfirmado, foiSubstituido, ordem, onTogglePresenca)
                }
            }
        }
    }
}

@Composable
private fun ItemFilaJogo(jogador: Jogador, confirmado: Boolean, substituido: Boolean, ordem: Int?, onToggle: (Jogador, Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().alpha(if (substituido) 0.5f else 1f),
        shape = MaterialTheme.shapes.small,
        color = if (substituido) Color(0xFF424242) else Color.White
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmado, onCheckedChange = { onToggle(jogador, confirmado) }, enabled = !substituido)
            Text(
                text = if (confirmado) "${ordem}º" else "-",
                style = MaterialTheme.typography.labelMedium,
                color = if (substituido) Color.White else Color(0xFF4B0082),
                modifier = Modifier.width(24.dp)
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(jogador.nome, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (substituido) Color.White else Color.Black)
                Text(if (substituido) "Saiu (S)" else if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha", style = MaterialTheme.typography.labelSmall, color = if (substituido) Color.LightGray else Color.Gray)
            }
            Surface(shape = MaterialTheme.shapes.small, color = if (substituido) Color(0xFF616161) else Color(0xFFF0EDFF)) {
                Text("#${jogador.numeroCamisa}", modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall, color = if (substituido) Color.White else Color(0xFF4B0082))
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
                            Text(txt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = if (s) Color.White else Color.Black)
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
