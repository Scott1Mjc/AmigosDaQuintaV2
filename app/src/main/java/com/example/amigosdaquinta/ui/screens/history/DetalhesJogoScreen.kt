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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import com.example.amigosdaquinta.viewmodel.JogadorParticipacao
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de detalhes de uma partida especifica com cores atualizadas.
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${jogo.numeroJogo} Jogo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = dateFormat.format(Date(jogo.data)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = timeFormat.format(Date(jogo.data)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "BRANCO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "${jogo.placarBranco} x ${jogo.placarVermelho}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp,
                                color = Color.Black
                            )
                            Text(
                                "VERMELHO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val (textoVencedor, corVencedor) = when (jogo.timeVencedor) {
                            TimeColor.BRANCO -> "Time Branco Venceu!" to Color(0xFF1B5E20)
                            TimeColor.VERMELHO -> "Time Vermelho Venceu!" to Color(0xFFB71C1C)
                            null -> "Empate" to Color.Black
                        }
                        Text(
                            text = textoVencedor,
                            color = corVencedor,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Duração: ${jogo.duracao} minutos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TeamEscalacaoCard(
                        modifier = Modifier.weight(1f),
                        titulo = "TIME BRANCO",
                        jogadores = detalhes!!.timeBranco,
                        cor = MaterialTheme.colorScheme.primaryContainer
                    )

                    TeamEscalacaoCard(
                        modifier = Modifier.weight(1f),
                        titulo = "TIME VERMELHO",
                        jogadores = detalhes!!.timeVermelho,
                        cor = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamEscalacaoCard(
    modifier: Modifier = Modifier,
    titulo: String,
    jogadores: List<JogadorParticipacao>,
    cor: Color
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = cor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "$titulo (${jogadores.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jogadores) { participacao ->
                    val jogador = participacao.jogador
                    val foiSubstituido = participacao.foiSubstituido
                    val alphaValue = if (foiSubstituido) 0.5f else 1f

                    Surface(
                        modifier = Modifier.fillMaxWidth().alpha(alphaValue),
                        shape = MaterialTheme.shapes.small,
                        color = if (foiSubstituido) Color.LightGray else Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (if (jogador.isPosicaoGoleiro) "[GOL] " else "") + jogador.nome + (if (foiSubstituido) " (S)" else ""),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                color = Color.Black
                            )
                            Text(
                                text = "#${jogador.numeroCamisa}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
