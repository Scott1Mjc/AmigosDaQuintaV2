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
import androidx.compose.ui.text.font.FontWeight
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
 * Apresenta o placar final e as escalacoes de ambos os times lado a lado.
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
                // CARD DE CABEÇALHO (PLACAR)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Linha Superior: Numero, Data e Hora
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${jogo.numeroJogo} Jogo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = dateFormat.format(Date(jogo.data)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = timeFormat.format(Date(jogo.data)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Placar central
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "BRANCO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${jogo.placarBranco} x ${jogo.placarVermelho}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp
                            )
                            Text(
                                "VERMELHO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resultado
                        val (textoVencedor, corVencedor) = when (jogo.timeVencedor) {
                            TimeColor.BRANCO -> "Time Branco Venceu!" to MaterialTheme.colorScheme.primary
                            TimeColor.VERMELHO -> "Time Vermelho Venceu!" to MaterialTheme.colorScheme.error
                            null -> "Empate" to MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ESCALAÇÕES (Lado a Lado como na tela de Jogo)
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
                items(jogadores) { jogador ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "#${jogador.numeroCamisa}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
