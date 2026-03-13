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
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de Estatísticas Individuais do Jogador.
 * 
 * Apresenta o perfil do atleta, seu desempenho acumulado (vitórias, derrotas, empates)
 * e o histórico resumido das últimas 5 partidas disputadas.
 * 
 * @param jogadorId ID do jogador para consulta das estatísticas.
 * @param viewModel ViewModel de histórico para processamento dos dados.
 * @param onNavigateBack Callback para retornar à tela anterior.
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
                title = { Text("Perfil do Atleta", color = Color.Black) },
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
        if (stats == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4B0082))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                // Card de Identificação do Atleta
                item { PerfilAtletaCard(stats!!) }

                // Card de Estatísticas de Desempenho
                item { DesempenhoAtletaCard(stats!!) }

                item {
                    Text("Últimas 5 Partidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(stats!!.ultimosJogos) { jogo ->
                    JogoResumoHistoricoItem(jogo = jogo, dateFormat = dateFormat)
                }
            }
        }
    }
}

@Composable
private fun PerfilAtletaCard(stats: com.example.amigosdaquinta.viewmodel.EstatisticasJogador) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stats.jogador.nome, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(if (stats.jogador.isPosicaoGoleiro) "Goleiro" else "Jogador de Linha", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFF0EDFF)) {
                Text("#${stats.jogador.numeroCamisa}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.titleLarge, color = Color(0xFF4B0082), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DesempenhoAtletaCard(stats: com.example.amigosdaquinta.viewmodel.EstatisticasJogador) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Desempenho Geral", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            StatLinha("Total de Partidas", stats.totalJogos.toString())
            StatLinha("Vitórias", stats.vitorias.toString(), Color(0xFF2E7D32))
            StatLinha("Derrotas", stats.derrotas.toString(), Color(0xFFC62828))
            StatLinha("Empates", stats.empates.toString())

            if (stats.totalJogos > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                val aproveitamento = (stats.vitorias.toFloat() / stats.totalJogos * 100).toInt()
                StatLinha("Aproveitamento", "$aproveitamento%", Color(0xFF4B0082), isDestaque = true)
            }
        }
    }
}

@Composable
private fun StatLinha(label: String, value: String, color: Color = Color.Black, isDestaque: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        Text(value, style = if (isDestaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge, fontWeight = if (isDestaque) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}

@Composable
private fun JogoResumoHistoricoItem(jogo: Jogo, dateFormat: SimpleDateFormat) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = Color.White, shadowElevation = 0.5.dp) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("${jogo.numeroJogo}° Jogo", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(dateFormat.format(Date(jogo.data)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Text("${jogo.placarBranco} x ${jogo.placarVermelho}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            val (resLabel, resColor) = when (jogo.timeVencedor) {
                TimeColor.BRANCO -> "BRANCO" to Color(0xFF4B0082)
                TimeColor.VERMELHO -> "VERMELHO" to Color(0xFFC62828)
                null -> "EMPATE" to Color.Gray
            }
            Surface(color = resColor.copy(alpha = 0.1f), shape = MaterialTheme.shapes.extraSmall) {
                Text(resLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = resColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
