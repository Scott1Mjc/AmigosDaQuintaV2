package com.example.amigosdaquinta.ui.screens.jogo

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
import kotlinx.coroutines.delay

/**
 * Tela principal de uma partida em andamento.
 *
 * Gerencia o cronometro regressivo, placar e escalacao dos dois times.
 * O cronometro e iniciado automaticamente e pausado quando [jogoEmAndamento] se torna false
 * (por esgotamento do tempo ou por encerramento manual).
 *
 * O vencedor e determinado no momento em que o usuario confirma o encerramento,
 * comparando os placares atuais. Empate e representado por vencedor == null.
 *
 * Ao tocar em voltar, um dialog de confirmacao e exibido para evitar abandono acidental.
 * Abandonar o jogo nao salva o resultado no historico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoScreen(
    sessaoViewModel: SessaoViewModel,
    duracaoMinutos: Int,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    onNavigateBack: () -> Unit,
    onFinalizarJogo: (TimeColor?) -> Unit
) {
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()

    var tempoRestante by remember { mutableStateOf(duracaoMinutos * 60) }
    var jogoEmAndamento by remember { mutableStateOf(true) }
    var showFinalizarDialog by remember { mutableStateOf(false) }
    var showConfirmBackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jogoEmAndamento) {
        while (jogoEmAndamento && tempoRestante > 0) {
            delay(1000L)
            tempoRestante--
        }
        if (tempoRestante == 0) jogoEmAndamento = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jogo em Andamento") },
                navigationIcon = {
                    IconButton(onClick = { showConfirmBackDialog = true }) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CronometroComponent(
                tempoRestante = tempoRestante,
                totalSegundos = duracaoMinutos * 60
            )

            PlacarComponent(
                placarBranco = placarBranco,
                placarVermelho = placarVermelho,
                onGolBranco = { sessaoViewModel.incrementarPlacarBranco() },
                onGolVermelho = { sessaoViewModel.incrementarPlacarVermelho() }
            )

            Divider()

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Text("TIME BRANCO", style = MaterialTheme.typography.titleMedium) }
                items(timeBranco) { jogador -> EscalacaoItem(jogador) }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TIME VERMELHO", style = MaterialTheme.typography.titleMedium)
                }
                items(timeVermelho) { jogador -> EscalacaoItem(jogador) }
            }

            Button(
                onClick = { showFinalizarDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Finalizar Jogo")
            }
        }
    }

    if (showFinalizarDialog) {
        AlertDialog(
            onDismissRequest = { showFinalizarDialog = false },
            title = { Text("Finalizar Jogo?") },
            text = {
                Column {
                    Text("Placar Final:")
                    Text(
                        text = "BRANCO $placarBranco x $placarVermelho VERMELHO",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val vencedor = when {
                            placarBranco > placarVermelho -> TimeColor.BRANCO
                            placarVermelho > placarBranco -> TimeColor.VERMELHO
                            else -> null
                        }
                        sessaoViewModel.finalizarJogo(vencedor)
                        onFinalizarJogo(vencedor)
                    }
                ) {
                    Text("Finalizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showConfirmBackDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmBackDialog = false },
            title = { Text("Abandonar jogo?") },
            text = {
                Column {
                    Text("O jogo sera perdido e nao sera salvo no historico.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Placar atual: BRANCO $placarBranco x $placarVermelho VERMELHO",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmBackDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Abandonar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmBackDialog = false }) {
                    Text("Continuar Jogo")
                }
            }
        )
    }
}

@Composable
private fun EscalacaoItem(jogador: Jogador) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome
            )
            Text("#${jogador.numeroCamisa}")
        }
    }
}