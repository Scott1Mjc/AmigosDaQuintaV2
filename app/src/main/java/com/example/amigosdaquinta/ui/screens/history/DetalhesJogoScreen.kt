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
 * Tela de Detalhes de uma Partida do Histórico.
 * 
 * Exibe o placar final centralizado e a escalação completa de ambos os times, 
 * destacando quem foi substituído (S) e quem entrou durante o jogo (E).
 * 
 * @param jogoId Identificador da partida a ser exibida.
 * @param viewModel ViewModel de histórico para carregar os dados.
 * @param onNavigateBack Callback para retornar à lista de histórico.
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
                title = { Text("Detalhes da Partida", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        if (detalhes == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4B0082))
            }
        } else {
            val jogo = detalhes!!.jogo

            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Painel de Resultado Centralizado
                PainelResultadoDetalhes(
                    jogo = jogo,
                    dateFormat = dateFormat,
                    timeFormat = timeFormat
                )

                // Escaladas dos Times
                // ✅ ORDEM INVERTIDA: VERMELHO à esquerda, BRANCO à direita
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EscalacaoHistoricoCard(
                        titulo = "TIME VERMELHO",
                        jogadores = detalhes!!.timeVermelho,
                        containerColor = Color(0xFFFFE1E1),
                        modifier = Modifier.weight(1f)
                    )

                    EscalacaoHistoricoCard(
                        titulo = "TIME BRANCO",
                        jogadores = detalhes!!.timeBranco,
                        containerColor = Color(0xFFE8E2FF),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PainelResultadoDetalhes(
    jogo: com.example.amigosdaquinta.data.local.entity.Jogo,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${jogo.numeroJogo}° Jogo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${dateFormat.format(Date(jogo.data))} ${timeFormat.format(Date(jogo.data))}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Alinhamento centralizado garantido com pesos iguais nos nomes dos times
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERMELHOS",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(
                    text = " ${jogo.placarVermelho} x ${jogo.placarBranco} ",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 56.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "BRANCO",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val vencedorTexto = when (jogo.timeVencedor) {
                TimeColor.BRANCO -> "Vitória do Time Branco"
                TimeColor.VERMELHO -> "Vitória do Time Vermelho"
                null -> "Empate"
            }
            
            Text(vencedorTexto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Duração: ${jogo.duracao} minutos", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        }
    }
}

@Composable
private fun EscalacaoHistoricoCard(
    titulo: String,
    jogadores: List<JogadorParticipacao>,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val ordenados = remember(jogadores) {
        // ✅ GOLEIROS SEMPRE NO TOPO
        jogadores.distinctBy { it.jogador.id }.sortedWith(
            compareBy<JogadorParticipacao> { !it.jogador.isPosicaoGoleiro } // Goleiro (false) vem antes de linha (true)
                .thenBy { it.foiSubstituido }
                .thenBy { it.entrouComoSubstituto }
                .thenBy { it.jogador.nome }
        )
    }

    Surface(modifier = modifier.fillMaxHeight(), shape = MaterialTheme.shapes.medium, color = containerColor) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("$titulo (${jogadores.size})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ordenados, key = { it.jogador.id }) { part ->
                    val s = part.foiSubstituido
                    val e = part.entrouComoSubstituto
                    val surfCol = if (s) Color.Black else Color.White
                    val textCol = if (s) Color.White else Color.Black

                    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = surfCol, shadowElevation = 0.5.dp) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val txt = (if (part.jogador.isPosicaoGoleiro) "[GOL] " else "") + (if (e && !s) "(E) " else "") + part.jogador.nome + (if (s) " (S)" else "")
                            Text(txt, style = MaterialTheme.typography.bodySmall, fontWeight = if (s) FontWeight.Normal else FontWeight.Medium, modifier = Modifier.weight(1f), color = textCol)
                            
                            Surface(color = if (s) Color(0xFF616161) else Color(0xFFF0EDFF), shape = MaterialTheme.shapes.small) {
                                Text("#${part.jogador.numeroCamisa}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (s) Color.White else Color(0xFF4B0082))
                            }
                        }
                    }
                }
            }
        }
    }
}
