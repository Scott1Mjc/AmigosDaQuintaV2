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
 * Tela de formação automática de times.
 *
 * Cobre três cenários mutuamente exclusivos, determinados pelo número de presentes
 * e pelo estado do jogo anterior:
 *
 * 1. Primeiro jogo com 33+ jogadores (Rotacao Total):
 *    Dois times inteiramente novos, formados por ordem de chegada.
 *
 * 2. Segundo jogo ou posterior com 30+ jogadores (Rotacao Forcada):
 *    Remove os jogadores do jogo anterior e forma dois times com quem ainda nao jogou.
 *
 * 3. Segundo jogo ou posterior com menos de 30 jogadores (Normal):
 *    Time vencedor permanece; novo time adversario é montado da fila de espera.
 *    Se a fila estiver curta, completa com jogadores do time perdedor por ordem de chegada.
 *
 * A logica de montagem está contida no bloco [remember] e recalculada sempre que
 * [filaEspera], [jogadoresTimeGanhador] ou [forcaRotacao] mudarem.
 *
 * Duracao do jogo: 30 min se for o primeiro jogo, 15 min para os demais.
 *
 * TODO: Considerar mover a logica de formacao de times para o [SessaoViewModel] ou um UseCase,
 * deixando esta tela apenas como apresentacao do resultado calculado.
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
    val numeroProximoJogo by sessaoViewModel.numeroProximoJogo.collectAsState()
    val totalPresentes = filaEspera.size
    val ehPrimeiroJogo = jogadoresTimeGanhador.isEmpty()

    val forcaRotacao = if (ehPrimeiroJogo) totalPresentes >= 33 else totalPresentes >= 30
    val duracaoJogo = if (ehPrimeiroJogo) 30 else 15

    val (timeBranco, timeVermelho) = remember(filaEspera, jogadoresTimeGanhador, forcaRotacao, ehPrimeiroJogo) {
        val todos = filaEspera.map { it.first }

        when {
            ehPrimeiroJogo -> {
                // Cenario 1: Primeiro jogo — forma dois times pela ordem de chegada
                val goleiros = todos.filter { it.isPosicaoGoleiro }
                val linha = todos.filter { !it.isPosicaoGoleiro }
                if (goleiros.size >= 2 && linha.size >= 20) {
                    Pair(
                        listOf(goleiros[0]) + linha.take(10),
                        listOf(goleiros[1]) + linha.drop(10).take(10)
                    )
                } else {
                    Pair(emptyList(), emptyList())
                }
            }

            forcaRotacao -> {
                // Cenario 2: Rotacao forcada — remove quem jogou no jogo anterior e forma dois times novos
                val idsAnterior = jogadoresTimeGanhador.map { it.id }.toSet()
                val disponiveis = todos.filter { it.id !in idsAnterior }
                val goleiros = disponiveis.filter { it.isPosicaoGoleiro }
                val linha = disponiveis.filter { !it.isPosicaoGoleiro }
                if (goleiros.size >= 2 && linha.size >= 20) {
                    Pair(
                        listOf(goleiros[0]) + linha.take(10),
                        listOf(goleiros[1]) + linha.drop(10).take(10)
                    )
                } else {
                    Pair(emptyList(), emptyList())
                }
            }

            else -> {
                // Cenario 3: Normal — time vencedor permanece, novo time vem da fila
                val idsGanhador = jogadoresTimeGanhador.map { it.id }.toSet()
                val disponiveis = todos.filter { it.id !in idsGanhador }
                val goleiroNovo = disponiveis.firstOrNull { it.isPosicaoGoleiro }
                val linhaNova = disponiveis.filter { !it.isPosicaoGoleiro }.take(10)

                if (goleiroNovo != null && linhaNova.size == 10) {
                    var timeNovo = listOf(goleiroNovo) + linhaNova

                    // Completa o time novo com jogadores do time perdedor se necessario
                    if (timeNovo.size < 11) {
                        val faltam = 11 - timeNovo.size
                        val idsNoTimeNovo = timeNovo.map { it.id }.toSet()
                        val complemento = jogadoresTimeGanhador
                            .filter { it.id !in idsNoTimeNovo }
                            .sortedBy { jogador -> filaEspera.indexOfFirst { it.first.id == jogador.id } }
                            .take(faltam)
                        timeNovo = timeNovo + complemento
                    }

                    if (timeGanhador == TimeColor.BRANCO || timeGanhador == null) {
                        Pair(jogadoresTimeGanhador, timeNovo)
                    } else {
                        Pair(timeNovo, jogadoresTimeGanhador)
                    }
                } else {
                    Pair(emptyList(), emptyList())
                }
            }
        }
    }

    val podeIniciar = timeBranco.size == 11 && timeVermelho.size == 11

    val tituloTela = when {
        ehPrimeiroJogo && forcaRotacao -> "Formar 1 Jogo (Rotacao Total 33+)"
        forcaRotacao -> "Formar ${numeroProximoJogo} Jogo (Rotacao 30+)"
        else -> "Formar ${numeroProximoJogo} Jogo"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloTela) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            FormacaoInfoCard(
                ehPrimeiroJogo = ehPrimeiroJogo,
                forcaRotacao = forcaRotacao,
                numeroProximoJogo = numeroProximoJogo,
                timeGanhador = timeGanhador,
                totalPresentes = totalPresentes
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    TimePreviewColumn(
                        titulo = "TIME BRANCO",
                        jogadores = timeBranco,
                        permanece = !forcaRotacao && !ehPrimeiroJogo && (timeGanhador == TimeColor.BRANCO || timeGanhador == null)
                    )
                }

                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    TimePreviewColumn(
                        titulo = "TIME VERMELHO",
                        jogadores = timeVermelho,
                        permanece = !forcaRotacao && !ehPrimeiroJogo && timeGanhador == TimeColor.VERMELHO
                    )
                }
            }

            Button(
                onClick = {
                    sessaoViewModel.criarPrimeiroJogo(
                        timeBranco = timeBranco,
                        timeVermelho = timeVermelho,
                        duracao = duracaoJogo
                    )
                    onIniciarJogo()
                },
                enabled = podeIniciar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (podeIniciar) "Iniciar Jogo - $duracaoJogo minutos"
                    else "Nao ha jogadores suficientes"
                )
            }
        }
    }
}

@Composable
private fun FormacaoInfoCard(
    ehPrimeiroJogo: Boolean,
    forcaRotacao: Boolean,
    numeroProximoJogo: Int,
    timeGanhador: TimeColor?,
    totalPresentes: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (ehPrimeiroJogo) "Formacao Automatica - 1 Jogo"
                else "Formacao Automatica - $numeroProximoJogo Jogo",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (forcaRotacao) {
                Text(
                    text = if (ehPrimeiroJogo) "ROTACAO TOTAL (33+ jogadores)"
                    else "ROTACAO ATIVADA (30+ jogadores)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (ehPrimeiroJogo) "Formacao pela ordem de chegada"
                    else "Todos que nao jogaram no jogo anterior entram agora",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = if (timeGanhador != null) {
                        "Time ${if (timeGanhador == TimeColor.BRANCO) "BRANCO" else "VERMELHO"} venceu e permanece"
                    } else {
                        "Empate - Time Branco permanece"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Novo time formado pela ordem de chegada",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Total na lista: $totalPresentes jogadores")
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
        Text(text = titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            text = if (permanece) "Permanece" else "Novo",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (jogadores.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aguardando...")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(jogadores) { jogador ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "#${jogador.numeroCamisa}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}