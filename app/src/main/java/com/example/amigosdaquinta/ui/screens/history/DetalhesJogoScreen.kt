package com.example.amigosdaquinta.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import com.example.amigosdaquinta.viewmodel.JogadorParticipacao
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de detalhes de uma partida padronizada com o estilo Print 1.
 * Mantém o padrão de cores dos times (Branco/Vermelho) nos cards de escalação.
 * Jogadores substituídos retornam ao padrão de fundo preto com letras brancas.
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
                title = { Text("Detalhes do Jogo", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
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
                // CARD DE RESULTADO (Alinhado no meio - Print 4)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFFEBE8EC)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${jogo.numeroJogo} Jogo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${dateFormat.format(Date(jogo.data))} ${timeFormat.format(Date(jogo.data))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "BRANCO",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "  ${jogo.placarBranco} x ${jogo.placarVermelho}  ",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 56.sp,
                                color = Color.Black
                            )
                            Text(
                                "VERMELHO",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val (textoVencedor, corTexto) = when (jogo.timeVencedor) {
                            TimeColor.BRANCO -> "Vencedor: Branco" to Color.Black
                            TimeColor.VERMELHO -> "Vencedor: Vermelho" to Color.Black
                            null -> "Empate" to Color.Black
                        }
                        
                        Text(
                            text = textoVencedor,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = corTexto,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Duração: ${jogo.duracao} minutos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Escalacoes mantendo o padrão de cor (Branco/Vermelho)
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
                        containerColor = Color(0xFFE8E2FF) // Lavanda padrão
                    )

                    TeamEscalacaoCard(
                        modifier = Modifier.weight(1f),
                        titulo = "TIME VERMELHO",
                        jogadores = detalhes!!.timeVermelho,
                        containerColor = Color(0xFFFFE1E1) // Vermelho claro padrão
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
    containerColor: Color
) {
    val jogadoresOrdenados = remember(jogadores) {
        jogadores.sortedWith(
            compareBy<JogadorParticipacao> { it.foiSubstituido }
                .thenBy { !it.jogador.isPosicaoGoleiro }
                .thenBy { it.entrouComoSubstituto }
                .thenBy { it.jogador.nome }
        )
    }

    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "$titulo (${jogadores.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(jogadoresOrdenados) { participacao ->
                    val jogador = participacao.jogador
                    val foiSubstituido = participacao.foiSubstituido
                    val entrouComoSubstituto = participacao.entrouComoSubstituto
                    
                    // Revertendo para fundo preto e letras brancas para substituidos
                    val surfaceColor = if (foiSubstituido) Color(0xFF424242) else Color.White
                    val textColor = if (foiSubstituido) Color.White else Color.Black

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = surfaceColor,
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val prefixo = if (entrouComoSubstituto && !foiSubstituido) "(E) " else ""
                            val sufixo = if (foiSubstituido) " (S)" else ""

                            Text(
                                text = (if (jogador.isPosicaoGoleiro) "[GOL] " else "") + prefixo + jogador.nome + sufixo,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (foiSubstituido) FontWeight.Normal else FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )
                            
                            Surface(
                                color = if (foiSubstituido) Color(0xFF616161) else Color(0xFFF0EDFF),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "#${jogador.numeroCamisa}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = if (foiSubstituido) Color.White else Color(0xFF4B0082)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
