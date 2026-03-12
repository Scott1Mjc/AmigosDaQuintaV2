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
    val jogadores by jogadoresViewModel.jogadores.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()
    val duracaoMinutos by sessaoViewModel.duracaoJogoAtualMinutos.collectAsState()
    val jogadoresSubstituidosIds by sessaoViewModel.jogadoresSubstituidosIds.collectAsState()
    val jogadoresQueEntraramSubstitutosIds by sessaoViewModel.jogadoresQueEntraramSubstitutosIds.collectAsState()

    var showFinalizarDialog by remember { mutableStateOf(false) }
    var finalizacaoAutomatica by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSubstituicaoDialog by remember { mutableStateOf(false) }
    var jogadorParaSubstituir by remember { mutableStateOf<Pair<Jogador, TimeColor>?>(null) }

    val jogadoresFiltrados by remember(jogadores, listaPresenca, searchQuery, jogadoresSubstituidosIds) {
        derivedStateOf {
            val filtered = if (searchQuery.isBlank()) {
                jogadores
            } else {
                jogadores.filter { jogador ->
                    jogador.nome.contains(searchQuery, ignoreCase = true) ||
                            jogador.numeroCamisa.toString().contains(searchQuery)
                }
            }

            filtered.sortedWith(
                compareBy<Jogador> { jogador ->
                    jogadoresSubstituidosIds.contains(jogador.id)
                }.thenBy { jogador ->
                    !listaPresenca.any { it.first.id == jogador.id }
                }.thenBy { it.nome }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jogo em Andamento") }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Lista de Jogadores", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Pesquisar...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Pesquisar") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items = jogadoresFiltrados, key = { it.id }) { jogador ->
                            val jaConfirmado = listaPresenca.any { it.first.id == jogador.id }
                            val foiSubstituido = jogadoresSubstituidosIds.contains(jogador.id)
                            val ordemChegada = if (jaConfirmado) {
                                listaPresenca.indexOfFirst { it.first.id == jogador.id } + 1
                            } else {
                                null
                            }
                            
                            JogadorItemLista(
                                jogador = jogador,
                                confirmado = jaConfirmado,
                                substituido = foiSubstituido,
                                ordemChegada = ordemChegada,
                                onTogglePresenca = {
                                    if (jaConfirmado) sessaoViewModel.removerDaListaPresenca(jogador.id)
                                    else sessaoViewModel.adicionarAListaPresenca(jogador)
                                }
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize().weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    modifier = Modifier.fillMaxWidth().weight(0.25f)
                )

                Row(modifier = Modifier.fillMaxWidth().weight(0.65f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EscalacaoCardComSubstituicao(
                        modifier = Modifier.weight(1f),
                        titulo = "TIME BRANCO",
                        jogadores = timeBranco,
                        cor = MaterialTheme.colorScheme.primaryContainer,
                        jogadoresSubstituidosIds = jogadoresSubstituidosIds,
                        jogadoresQueEntraramSubstitutosIds = jogadoresQueEntraramSubstitutosIds,
                        onSubstituir = { jogador ->
                            jogadorParaSubstituir = Pair(jogador, TimeColor.BRANCO)
                            showSubstituicaoDialog = true
                        }
                    )

                    EscalacaoCardComSubstituicao(
                        modifier = Modifier.weight(1f),
                        titulo = "TIME VERMELHO",
                        jogadores = timeVermelho,
                        cor = MaterialTheme.colorScheme.errorContainer,
                        jogadoresSubstituidosIds = jogadoresSubstituidosIds,
                        jogadoresQueEntraramSubstitutosIds = jogadoresQueEntraramSubstitutosIds,
                        onSubstituir = { jogador ->
                            jogadorParaSubstituir = Pair(jogador, TimeColor.VERMELHO)
                            showSubstituicaoDialog = true
                        }
                    )
                }

                Button(
                    onClick = {
                        finalizacaoAutomatica = false
                        showFinalizarDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().weight(0.1f).heightIn(min = 56.dp)
                ) {
                    Text("Finalizar Jogo", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showSubstituicaoDialog && jogadorParaSubstituir != null) {
        SubstituicaoDialog(
            jogadorSaindo = jogadorParaSubstituir!!.first,
            time = jogadorParaSubstituir!!.second,
            jogadoresDisponiveis = jogadores.filter { jogador ->
                !timeBranco.any { it.id == jogador.id } && !timeVermelho.any { it.id == jogador.id }
            },
            onDismiss = {
                showSubstituicaoDialog = false
                jogadorParaSubstituir = null
            },
            onConfirm = { jogadorEntrando, isLesionado ->
                sessaoViewModel.substituirJogador(
                    jogadorSaindo = jogadorParaSubstituir!!.first,
                    jogadorEntrando = jogadorEntrando,
                    time = jogadorParaSubstituir!!.second,
                    isLesionado = isLesionado
                )
                showSubstituicaoDialog = false
                jogadorParaSubstituir = null
            }
        )
    }

    if (showFinalizarDialog) {
        AlertDialog(
            onDismissRequest = { if (!finalizacaoAutomatica) showFinalizarDialog = false },
            title = { Text(if (finalizacaoAutomatica) "Tempo Esgotado!" else "Finalizar Jogo") },
            text = {
                Column {
                    Text("Placar Final: $placarBranco x $placarVermelho")
                    Text("Confirmar resultado?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val vencedor = when {
                            placarBranco > placarVermelho -> TimeColor.BRANCO
                            placarVermelho > placarBranco -> TimeColor.VERMELHO
                            else -> null
                        }
                        sessaoViewModel.finalizarJogo(vencedor)
                        onFinalizarJogo(vencedor)
                        showFinalizarDialog = false
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = if (!finalizacaoAutomatica) {
                { TextButton(onClick = { showFinalizarDialog = false }) { Text("Cancelar") } }
            } else null
        )
    }
}

@Composable
private fun JogadorItemLista(
    jogador: Jogador,
    confirmado: Boolean,
    substituido: Boolean,
    ordemChegada: Int?,
    onTogglePresenca: () -> Unit
) {
    val alphaValue = if (substituido) 0.6f else 1f
    val color = if (substituido) Color(0xFF424242) else MaterialTheme.colorScheme.surface
    
    Surface(
        modifier = Modifier.fillMaxWidth().alpha(alphaValue),
        shape = MaterialTheme.shapes.small,
        color = color,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = confirmado, onCheckedChange = { onTogglePresenca() }, enabled = !substituido)
            
            if (confirmado && ordemChegada != null) {
                Text(
                    text = "${ordemChegada}°", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = if (substituido) Color.White else MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.width(32.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
            
            Text("|", style = MaterialTheme.typography.titleLarge, color = if (substituido) Color.Gray else MaterialTheme.colorScheme.outline)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogador.nome, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = if (substituido) Color.White else Color.Black
                )
                Text(
                    text = if (substituido) "Substituído" else if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (substituido) Color.LightGray else Color.DarkGray
                )
            }
            Text(
                text = "#${jogador.numeroCamisa}", 
                style = MaterialTheme.typography.labelLarge, 
                color = if (substituido) Color.White else Color.Black
            )
        }
    }
}

@Composable
private fun EscalacaoCardComSubstituicao(
    modifier: Modifier = Modifier,
    titulo: String,
    jogadores: List<Jogador>,
    cor: androidx.compose.ui.graphics.Color,
    jogadoresSubstituidosIds: Set<Long>,
    jogadoresQueEntraramSubstitutosIds: Set<Long>,
    onSubstituir: (Jogador) -> Unit
) {
    // Ordenação: 
    // 1. Quem NÃO foi substituído (em campo) deve vir ANTES de quem foi substituído (S)
    // 2. Entre os que estão em campo, o GOLEIRO deve vir PRIMEIRO
    // 3. Depois o critério de entrada (Titulares vs Substitutos (E))
    // 4. Por fim, ordem alfabética
    val jogadoresOrdenados = remember(jogadores, jogadoresSubstituidosIds, jogadoresQueEntraramSubstitutosIds) {
        jogadores.sortedWith(
            compareBy<Jogador> { jogador ->
                // Critério 1: Substituídos por último
                jogadoresSubstituidosIds.contains(jogador.id)
            }.thenBy { jogador ->
                // Critério 2: Goleiro primeiro (false vem antes de true no boolean ascending, 
                // então invertemos ou usamos compareByDescending)
                !jogador.isPosicaoGoleiro
            }.thenBy { jogador ->
                // Critério 3: Quem entrou por substituição depois dos titulares
                jogadoresQueEntraramSubstitutosIds.contains(jogador.id)
            }.thenBy { it.nome }
        )
    }

    Card(modifier = modifier.fillMaxHeight(), colors = CardDefaults.cardColors(containerColor = cor)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(jogadoresOrdenados) { jogador ->
                    val foiSubstituido = jogadoresSubstituidosIds.contains(jogador.id)
                    val entrouComoSubstituto = jogadoresQueEntraramSubstitutosIds.contains(jogador.id)
                    val alphaValue = if (foiSubstituido) 0.6f else 1f
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().alpha(alphaValue), 
                        shape = MaterialTheme.shapes.small, 
                        tonalElevation = 2.dp,
                        color = if (foiSubstituido) Color(0xFF424242) else Color.White
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val prefixo = if (entrouComoSubstituto && !foiSubstituido) "(E) " else ""
                            val sufixo = if (foiSubstituido) " (S)" else ""
                            
                            Text(
                                text = (if (jogador.isPosicaoGoleiro) "[GOL] " else "") + prefixo + jogador.nome + sufixo,
                                style = MaterialTheme.typography.bodyMedium, 
                                modifier = Modifier.weight(1f),
                                color = if (foiSubstituido) Color.White else Color.Black
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${jogador.numeroCamisa}", 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    color = if (foiSubstituido) Color.White else Color.Black
                                )
                                if (!foiSubstituido) {
                                    IconButton(onClick = { onSubstituir(jogador) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = "Substituir", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
