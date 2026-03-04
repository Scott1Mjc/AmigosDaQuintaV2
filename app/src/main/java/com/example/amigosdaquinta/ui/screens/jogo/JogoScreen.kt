package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela principal de uma partida em andamento.
 * Tela do jogo em andamento.
 * Exibe placar e permite marcar gols e finalizar o jogo.
 *
 * O vencedor e determinado no momento em que o usuario confirma o encerramento,
 * comparando os placares atuais. Empate e representado por vencedor == null.
 *
 * Ao tocar em voltar, um dialog de confirmacao e exibido para evitar abandono acidental.
 * Abandonar o jogo nao salva o resultado no historico.
 *
 * NOTA: Cronômetro foi removido - jogos não têm limite de tempo fixo.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoScreen(
    sessaoViewModel: SessaoViewModel,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    onNavigateBack: () -> Unit,
    onFinalizarJogo: (TimeColor?) -> Unit
) {
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()

    var showFinalizarDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jogo em Andamento") },
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Placar
            PlacarComponent(
                placarBranco = placarBranco,
                placarVermelho = placarVermelho,
                onGolBranco = { sessaoViewModel.incrementarPlacarBranco() },
                onGolVermelho = { sessaoViewModel.incrementarPlacarVermelho() },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Escalações
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Finalizar
            Button(
                onClick = { showFinalizarDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalizar Jogo")
            }
        }
    }

    // Dialog de finalizar
    if (showFinalizarDialog) {
        AlertDialog(
            onDismissRequest = { showFinalizarDialog = false },
            title = { Text("Finalizar Jogo") },
            text = {
                Column {
                    Text("Placar Final:")
                    Text("TIME BRANCO: $placarBranco")
                    Text("TIME VERMELHO: $placarVermelho")
                    Spacer(modifier = Modifier.height(8.dp))
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
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            jogadores.forEach { jogador ->
                Text(
                    text = if (jogador.isPosicaoGoleiro) {
                        "[GOL] ${jogador.nome} - ${jogador.numeroCamisa}"
                    } else {
                        "${jogador.nome} - ${jogador.numeroCamisa}"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}