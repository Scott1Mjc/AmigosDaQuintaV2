package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela principal de uma partida em andamento.
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
    // Estados observados do ViewModel
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()
    val jogadores by jogadoresViewModel.jogadores.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()
    val duracaoMinutos by sessaoViewModel.duracaoJogoAtualMinutos.collectAsState()

    // Estados locais de controle de diálogos
    var showFinalizarDialog by remember { mutableStateOf(false) }
    var finalizacaoAutomatica by remember { mutableStateOf(false) }

    // ✅ CORREÇÃO: Ordenação decrescente por número da camisa e SEM mover selecionados para o fim
    val jogadoresOrdenados by remember(jogadores) {
        derivedStateOf {
            jogadores.sortedByDescending { it.numeroCamisa }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jogo em Andamento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
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
            // COLUNA 1: LISTA DE JOGADORES
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
                                    if (confirmado && !jaConfirmado) {
                                        sessaoViewModel.adicionarAListaPresenca(jogador)
                                    } else if (!confirmado && jaConfirmado) {
                                        sessaoViewModel.removerDaListaPresenca(jogador.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // COLUNA 2: ÁREA PRINCIPAL
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // PLACAR E CRONÔMETRO UNIFICADOS
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.25f)
                )

                // ESCALAÇÕES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.65f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EscalacaoCard(
                        modifier = Modifier.weight(1f),
                        titulo = "TIME BRANCO",
                        jogadores = timeBranco,
                        cor = MaterialTheme.colorScheme.primaryContainer
                    )

                    EscalacaoCard(
                        modifier = Modifier.weight(1f),
                        titulo = "TIME VERMELHO",
                        jogadores = timeVermelho,
                        cor = MaterialTheme.colorScheme.errorContainer
                    )
                }

                Button(
                    onClick = {
                        finalizacaoAutomatica = false
                        showFinalizarDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f)
                        .heightIn(min = 56.dp)
                ) {
                    Text(
                        "Finalizar Jogo",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    if (showFinalizarDialog) {
        AlertDialog(
            onDismissRequest = { if (!finalizacaoAutomatica) showFinalizarDialog = false },
            title = { Text(if (finalizacaoAutomatica) "Tempo Esgotado!" else "Finalizar Jogo") },
            text = {
                Column {
                    if (finalizacaoAutomatica) {
                        Text("O tempo de jogo acabou!", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text("Placar Final:")
                    Text("TIME BRANCO: $placarBranco", style = MaterialTheme.typography.titleMedium)
                    Text("TIME VERMELHO: $placarVermelho", style = MaterialTheme.typography.titleMedium)
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
private fun JogadorComCheckbox(
    jogador: Jogador,
    confirmado: Boolean,
    ordemChegada: Int?,
    onConfirmar: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = confirmado, onCheckedChange = onConfirmar)

            if (confirmado && ordemChegada != null) {
                Text(
                    text = "${ordemChegada}°",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(32.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }

            Text("|", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)

            Column(modifier = Modifier.weight(1f)) {
                Text(text = jogador.nome, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "#${jogador.numeroCamisa}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun EscalacaoCard(
    modifier: Modifier = Modifier,
    titulo: String,
    jogadores: List<Jogador>,
    cor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = cor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jogadores) { jogador ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "#${jogador.numeroCamisa}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
