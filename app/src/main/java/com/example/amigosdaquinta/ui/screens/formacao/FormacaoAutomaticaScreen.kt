package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela de formação automática de times por ordem de chegada.
 *
 * REGRAS IMPLEMENTADAS:
 * - Se time jogou 2x consecutivas OU empate: forma 2 times novos
 * - Caso contrário: time vencedor permanece, forma 1 time novo
 * - Goleiros podem repetir (exceção por falta de goleiros)
 * - Ordem de chegada é respeitada
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormacaoAutomaticaScreen(
    timeGanhador: TimeColor?,
    jogadoresTimeGanhador: List<Jogador>,
    filaEspera: List<Pair<Jogador, Long>>,
    sessaoViewModel: SessaoViewModel,
    onNavigateBack: () -> Unit,
    onIniciarJogo: () -> Unit
) {
    val numeroJogo by sessaoViewModel.numeroDoProximoJogo.collectAsState()
    val jogosConsecutivos by sessaoViewModel.jogosConsecutivosTimeAtual.collectAsState()

    val totalPresentes = filaEspera.size

    // Detecta se é 1º jogo (não tem time ganhador)
    val eh1Jogo = jogadoresTimeGanhador.isEmpty()

    // Se time jogou 2x OU empate: ambos saem (forma 2 times novos)
    val ambosTimesSaem = jogosConsecutivos >= 2 || (timeGanhador == null && !eh1Jogo)

    // Formação dos times
    val (timeBranco, timeVermelho) = remember(filaEspera, jogadoresTimeGanhador, ambosTimesSaem, eh1Jogo) {

        if (ambosTimesSaem || eh1Jogo) {
            // CENÁRIO 1: Forma 2 times NOVOS pela ordem de chegada
            val todosJogadores = filaEspera.map { it.first }

            val goleiros = todosJogadores.filter { it.isPosicaoGoleiro }
            val jogadoresLinha = todosJogadores.filter { !it.isPosicaoGoleiro }

            if (goleiros.size >= 2 && jogadoresLinha.size >= 20) {
                val branco = listOf(goleiros[0]) + jogadoresLinha.take(10)
                val vermelho = listOf(goleiros[1]) + jogadoresLinha.drop(10).take(10)
                Pair(branco, vermelho)
            } else {
                // Não tem jogadores suficientes
                Pair(emptyList(), emptyList())
            }

        } else {
            // CENÁRIO 2: Time vencedor PERMANECE, forma 1 time novo
            val todosJogadores = filaEspera.map { it.first }

            val jogadoresDisponiveis = todosJogadores.filter { jogador ->
                !jogadoresTimeGanhador.any { it.id == jogador.id }
            }

            // Busca goleiro (pode reutilizar se necessário)
            val goleiroDisponivel = jogadoresDisponiveis.firstOrNull { it.isPosicaoGoleiro }
                ?: todosJogadores.firstOrNull { it.isPosicaoGoleiro } // Reutiliza goleiro se necessário

            val linhaDisponivel = jogadoresDisponiveis.filter { !it.isPosicaoGoleiro }.take(10)

            if (goleiroDisponivel != null && linhaDisponivel.size == 10) {
                val timeNovo = listOf(goleiroDisponivel) + linhaDisponivel

                // Completa se necessário
                val timeNovoFinal = if (timeNovo.size < 11) {
                    val faltam = 11 - timeNovo.size
                    val paraCompletar = jogadoresTimeGanhador
                        .filter { jogador -> !timeNovo.any { it.id == jogador.id } }
                        .sortedBy { jogador ->
                            filaEspera.indexOfFirst { it.first.id == jogador.id }
                        }
                        .take(faltam)
                    timeNovo + paraCompletar
                } else {
                    timeNovo
                }

                // Define cores
                if (timeGanhador == TimeColor.BRANCO || timeGanhador == null) {
                    Pair(jogadoresTimeGanhador, timeNovoFinal)
                } else {
                    Pair(timeNovoFinal, jogadoresTimeGanhador)
                }
            } else {
                Pair(emptyList(), emptyList())
            }
        }
    }

    val podeIniciar = timeBranco.size == 11 && timeVermelho.size == 11

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Formar ${numeroJogo}º Jogo")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de informações
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Formação Automática - ${numeroJogo}º Jogo",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (ambosTimesSaem) {
                        if (jogosConsecutivos >= 2) {
                            Text(
                                "Time jogou 2x consecutivas - SAI",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Formando 2 times novos pela ordem de chegada",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "EMPATE no jogo anterior",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Ambos os times saem - formando 2 times novos",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else if (!eh1Jogo) {
                        Text(
                            "Time ${if (timeGanhador == TimeColor.BRANCO) "BRANCO" else "VERMELHO"} venceu e permanece",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Novo time formado pela ordem de chegada",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total na lista: $totalPresentes jogadores")
                }
            }

            // Times lado a lado
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Time Branco
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    TimePreviewColumn(
                        titulo = "TIME BRANCO",
                        jogadores = timeBranco,
                        permanece = !ambosTimesSaem && !eh1Jogo && (timeGanhador == TimeColor.BRANCO || timeGanhador == null)
                    )
                }

                // Time Vermelho
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    TimePreviewColumn(
                        titulo = "TIME VERMELHO",
                        jogadores = timeVermelho,
                        permanece = !ambosTimesSaem && !eh1Jogo && timeGanhador == TimeColor.VERMELHO
                    )
                }
            }

            // Botão Iniciar Jogo
            Button(
                onClick = {
                    sessaoViewModel.criarJogo(
                        timeBranco = timeBranco,
                        timeVermelho = timeVermelho
                    )
                    onIniciarJogo()
                },
                enabled = podeIniciar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (podeIniciar) {
                        "Iniciar Jogo"
                    } else {
                        "Não há jogadores suficientes"
                    }
                )
            }
        }
    }
}

@Composable
private fun TimePreviewColumn(
    titulo: String,
    jogadores: List<Jogador>,
    permanece: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)

        if (permanece) {
            Text("Permanece", style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Novo", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (jogadores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Aguardando...")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jogadores) { jogador ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Camisa ${jogador.numeroCamisa}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}