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
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de estatisticas individuais de um jogador.
 *
 * Carrega os dados via [HistoricoViewModel.obterEstatisticasJogador] ao entrar na tela.
 * Exibe estado de carregamento enquanto [estatisticasJogador] e null.
 *
 * Aproveitamento e calculado localmente (vitorias / totalJogos * 100) e exibido
 * apenas quando [totalJogos] > 0 para evitar divisao por zero.
 *
 * A lista de ultimos jogos e limitada a 5 registros pelo ViewModel/Repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstatisticasJogadorScreen(
    jogadorId: Long,
    viewModel: HistoricoViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.estatisticasJogador.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(jogadorId) {
        viewModel.obterEstatisticasJogador(jogadorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estatisticas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (stats == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                                text = stats!!.jogador.nome,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = if (stats!!.jogador.isPosicaoGoleiro) "Goleiro" else "Jogador de Linha",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "#${stats!!.jogador.numeroCamisa}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Estatisticas Gerais",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            StatRow("Total de Jogos", stats!!.totalJogos.toString())
                            StatRow("Vitorias", stats!!.vitorias.toString())
                            StatRow("Derrotas", stats!!.derrotas.toString())
                            StatRow("Empates", stats!!.empates.toString())

                            if (stats!!.totalJogos > 0) {
                                val aproveitamento = (stats!!.vitorias.toFloat() / stats!!.totalJogos * 100).toInt()
                                StatRow("Aproveitamento", "$aproveitamento%")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Ultimos 5 Jogos",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(stats!!.ultimosJogos) { jogo ->
                    JogoResumoCard(jogo = jogo, dateFormat = dateFormat)
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun JogoResumoCard(jogo: Jogo, dateFormat: SimpleDateFormat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${jogo.numeroJogo} Jogo",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(Date(jogo.data)),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "${jogo.placarBranco} x ${jogo.placarVermelho}",
                style = MaterialTheme.typography.titleMedium
            )

            val (textoResultado, corResultado) = when (jogo.timeVencedor) {
                TimeColor.BRANCO -> "B" to MaterialTheme.colorScheme.primary
                TimeColor.VERMELHO -> "V" to MaterialTheme.colorScheme.error
                null -> "E" to MaterialTheme.colorScheme.onSurface
            }
            Text(text = textoResultado, color = corResultado)
        }
    }
}