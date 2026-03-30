package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela de Formação Automática de Times.
 * 
 * Implementa a regra de negócio de integridade de times:
 * Se um jogador sai, o substituto é buscado no banco APÓS o próximo time formado.
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
    val numeroJogo by sessaoViewModel.numeroDoProximoJogo.collectAsStateWithLifecycle()
    val jogosConsecutivos by sessaoViewModel.jogosConsecutivosTimeAtual.collectAsStateWithLifecycle()

    // ✅ Lógica de Formação com Inteligência de Integridade
    val (timeBranco, timeVermelho) = remember(filaEspera, jogadoresTimeGanhador, timeGanhador, jogosConsecutivos) {
        val todosFila = filaEspera.map { it.first }.sortedBy { jog -> 
            filaEspera.find { it.first.id == jog.id }?.second ?: 0L 
        }
        
        val eh1Jogo = jogadoresTimeGanhador.isEmpty()
        val ambosSaem = (jogosConsecutivos ?: 0) >= 2 || (timeGanhador == null && !eh1Jogo)

        if (ambosSaem || eh1Jogo) {
            val gols = todosFila.filter { it.isPosicaoGoleiro }
            val linhas = todosFila.filter { !it.isPosicaoGoleiro }
            
            if (gols.size >= 2 && linhas.size >= 20) {
                val t1L = linhas.take(10)
                val t2L = linhas.drop(10).take(10)
                Pair(listOf(gols[0]) + t1L, listOf(gols[1]) + t2L)
            } else {
                // Tenta formar com o que tiver se não houver 22, mas priorizando 11 vs 11
                if (gols.size >= 2 && linhas.size >= 10) {
                    Pair(listOf(gols[0]) + linhas.take(10), emptyList<Jogador>())
                } else Pair(emptyList(), emptyList())
            }
        } else {
            // Um time fica e o desafiante entra
            val timeQueFica = jogadoresTimeGanhador.filter { jog -> todosFila.any { it.id == jog.id } }
            val idsTimeQueFica = timeQueFica.map { it.id }.toSet()
            
            val disponiveis = todosFila.filter { it.id !in idsTimeQueFica }
            
            // 1. Reserva o time desafiante (próximos 11 da fila)
            val desafianteGol = disponiveis.firstOrNull { it.isPosicaoGoleiro }
            val desafianteLinhas = disponiveis.filter { !it.isPosicaoGoleiro }.take(10)
            
            val idsDesafianteReservado = (desafianteLinhas.map { it.id } + (desafianteGol?.id ?: -1L)).toSet()
            
            // 2. Preenche buracos no Time Que Fica usando quem sobrou APÓS o desafiante
            val bancoAposDesafiante = disponiveis.filter { it.id !in idsDesafianteReservado }
            
            val timeQueFicaCompleto = timeQueFica.toMutableList()
            if (!timeQueFicaCompleto.any { it.isPosicaoGoleiro }) {
                bancoAposDesafiante.firstOrNull { it.isPosicaoGoleiro }?.let { timeQueFicaCompleto.add(it) }
            }
            val faltaLinha = 10 - timeQueFicaCompleto.count { !it.isPosicaoGoleiro }
            if (faltaLinha > 0) {
                timeQueFicaCompleto.addAll(bancoAposDesafiante.filter { !it.isPosicaoGoleiro }.take(faltaLinha))
            }

            if (timeQueFicaCompleto.size == 11 && desafianteGol != null && desafianteLinhas.size == 10) {
                val timeDesafiante = listOf(desafianteGol) + desafianteLinhas
                if (timeGanhador == TimeColor.BRANCO) Pair(timeQueFicaCompleto, timeDesafiante)
                else Pair(timeDesafiante, timeQueFicaCompleto)
            } else {
                Pair(emptyList(), emptyList())
            }
        }
    }

    val podeIniciar = timeBranco.size == 11 && timeVermelho.size == 11

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Próxima Partida", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = Color(0xFFEBE8EC)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${numeroJogo}º Jogo do Dia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Escalação automática: Vencedor permanece e desafiantes entram seguindo a fila.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimePreviewCard(titulo = "TIME VERMELHO", cor = Color(0xFFFFE1E1), jogadores = timeVermelho, modifier = Modifier.weight(1f))
                TimePreviewCard(titulo = "TIME BRANCO", cor = Color(0xFFE8E2FF), jogadores = timeBranco, modifier = Modifier.weight(1f))
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
                Text(
                    if (podeIniciar) "INICIAR ${numeroJogo}º JOGO" else "AGUARDANDO JOGADORES SUFICIENTES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TimePreviewCard(
    titulo: String,
    cor: Color,
    jogadores: List<Jogador>,
    modifier: Modifier = Modifier
) {
    // ✅ REGRA: GOLEIRO SEMPRE NO TOPO
    val ordenados = remember(jogadores) {
        jogadores.sortedByDescending { it.isPosicaoGoleiro }
    }

    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = cor
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${jogadores.size}/11", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (jogadores.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aguardando formação...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ordenados, key = { it.id }) { jogador ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraSmall,
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    "#${jogador.numeroCamisa}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
