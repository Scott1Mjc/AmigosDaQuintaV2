package com.example.amigosdaquinta.ui.screens.formacao

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
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela de Formação Automática de Times.
 * 
 * Processa a rotatividade dos atletas conforme as regras da pelada:
 * - Time vencedor permanece (limite de 2 jogos seguidos).
 * - Próximo time formado rigorosamente pela ordem de chegada na fila.
 * - Em caso de empate ou tempo esgotado, ambos os times são renovados.
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

    // Lógica de Formação Automática
    val (timeBranco, timeVermelho) = remember(filaEspera, jogadoresTimeGanhador, timeGanhador, jogosConsecutivos) {
        val todosFila = filaEspera.map { it.first }
        val eh1Jogo = jogadoresTimeGanhador.isEmpty()
        val ambosSaem = jogosConsecutivos >= 2 || (timeGanhador == null && !eh1Jogo)

        if (ambosSaem || eh1Jogo) {
            val gols = todosFila.filter { it.isPosicaoGoleiro }
            val linha = todosFila.filter { !it.isPosicaoGoleiro }
            if (gols.size >= 2 && linha.size >= 20) {
                Pair(listOf(gols[0]) + linha.take(10), listOf(gols[1]) + linha.drop(10).take(10))
            } else Pair(emptyList(), emptyList())
        } else {
            val disponiveis = todosFila.filter { jog -> !jogadoresTimeGanhador.any { it.id == jog.id } }
            val gol = disponiveis.firstOrNull { it.isPosicaoGoleiro } ?: todosFila.firstOrNull { it.isPosicaoGoleiro }
            val lin = disponiveis.filter { !it.isPosicaoGoleiro }.take(10)
            
            if (gol != null && lin.size == 10) {
                val novo = (listOf(gol) + lin)
                if (timeGanhador == TimeColor.BRANCO) Pair(jogadoresTimeGanhador, novo)
                else Pair(novo, jogadoresTimeGanhador)
            } else Pair(emptyList(), emptyList())
        }
    }

    val podeIniciar = timeBranco.size == 11 && timeVermelho.size == 11

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Próxima Partida", color = Color.Black) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Painel informativo da regra de rotação
            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = Color(0xFFEBE8EC)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${numeroJogo}º Jogo do Dia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Escalação automática baseada no resultado anterior.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visualização dos Times Formados
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimePreviewCard(titulo = "TIME BRANCO", cor = Color(0xFFE8E2FF), jogadores = timeBranco, modifier = Modifier.weight(1f))
                TimePreviewCard(titulo = "TIME VERMELHO", cor = Color(0xFFFFE1E1), jogadores = timeVermelho, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    sessaoViewModel.criarJogo(timeBranco, timeVermelho)
                    onIniciarJogo()
                },
                enabled = podeIniciar,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
            ) {
                Text("INICIAR ${numeroJogo}º JOGO", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimePreviewCard(titulo: String, cor: Color, jogadores: List<Jogador>, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxHeight(), shape = MaterialTheme.shapes.medium, color = cor) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (jogadores.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aguardando...", color = Color.Gray) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(jogadores) { jog ->
                        Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraSmall, color = Color.White) {
                            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (jog.isPosicaoGoleiro) "[GOL] ${jog.nome}" else jog.nome, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text("#${jog.numeroCamisa}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
