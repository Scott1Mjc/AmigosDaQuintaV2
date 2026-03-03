package com.example.amigosdaquinta.ui.screens.history

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
import androidx.compose.ui.unit.sp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de detalhes de uma partida especifica.
 *
 * Carrega os detalhes via [HistoricoViewModel.obterDetalhesJogo] ao entrar na tela,
 * reagindo ao [jogoId] recebido como parametro de navegacao.
 *
 * Exibe estado de carregamento (CircularProgressIndicator) enquanto [jogoDetalhes] e null.
 * Apos carregado, mostra: data/hora, placar, vencedor, duracao e escalacoes de ambos os times.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesJogoScreen(
    jogoId: Long,
    viewModel: HistoricoViewModel,
    onNavigateBack: () -> Unit
) {
    val detalhes by viewModel.jogoDetalhes.collectAsState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(jogoId) {
        viewModel.obterDetalhesJogo(jogoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Jogo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (detalhes == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val jogo = detalhes!!.jogo

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${jogo.numeroJogo} Jogo",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = dateFormat.format(Date(jogo.data)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = timeFormat.format(Date(jogo.data)),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("BRANCO", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${jogo.placarBranco} x ${jogo.placarVermelho}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontSize = 40.sp
                                )
                                Text("VERMELHO", style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val (textoVencedor, corVencedor) = when (jogo.timeVencedor) {
                                TimeColor.BRANCO -> "Time Branco Venceu!" to MaterialTheme.colorScheme.primary
                                TimeColor.VERMELHO -> "Time Vermelho Venceu!" to MaterialTheme.colorScheme.error
                                null -> "Empate" to MaterialTheme.colorScheme.onSurface
                            }
                            Text(
                                text = textoVencedor,
                                color = corVencedor,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Duracao: ${jogo.duracao} minutos")
                        }
                    }
                }

                item {
                    Text(
                        text = "TIME BRANCO (${detalhes!!.timeBranco.size} jogadores)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(detalhes!!.timeBranco) { jogador -> JogadorDetalheCard(jogador) }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TIME VERMELHO (${detalhes!!.timeVermelho.size} jogadores)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(detalhes!!.timeVermelho) { jogador -> JogadorDetalheCard(jogador) }
            }
        }
    }
}

@Composable
private fun JogadorDetalheCard(jogador: Jogador) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome
            )
            Text("#${jogador.numeroCamisa}")
        }
    }
}