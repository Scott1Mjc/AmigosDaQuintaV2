package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Componente Central de Placar e Cronômetro.
 * 
 * Unifica a exibição da pontuação das equipes com o controle de tempo da partida.
 * Oferece botões rápidos para registro de gols e pausar/retomar o tempo.
 */
@Composable
fun PlacarComponent(
    placarBranco: Int,
    placarVermelho: Int,
    duracaoMinutos: Int,
    onGolBranco: () -> Unit,
    onGolVermelho: () -> Unit,
    onTempoEsgotado: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempoRestante by rememberSaveable { mutableIntStateOf(duracaoMinutos * 60) }
    var pausado by rememberSaveable { mutableStateOf(false) }
    val tempoInicial = rememberSaveable { duracaoMinutos * 60 }

    LaunchedEffect(pausado, tempoRestante) {
        if (!pausado && tempoRestante > 0) {
            delay(1000L)
            tempoRestante--
            if (tempoRestante == 0) onTempoEsgotado()
        }
    }

    val min = tempoRestante / 60
    val seg = tempoRestante % 60

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LADO BRANCO
            ScoreActionColumn(
                label = "BRANCO",
                score = placarBranco,
                buttonColor = Color(0xFF4B0082),
                onGol = onGolBranco,
                modifier = Modifier.weight(1f)
            )

            // CENTRO: CRONÔMETRO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.5f)
            ) {
                Text(
                    text = String.format("%02d:%02d", min, seg),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = if (tempoRestante <= 300 && tempoRestante > 0) Color.Red else Color.Black
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { pausado = !pausado }, modifier = Modifier.size(40.dp)) {
                        Icon(if (pausado) Icons.Default.PlayArrow else Icons.Default.Pause, null, modifier = Modifier.size(32.dp))
                    }
                    
                    LinearProgressIndicator(
                        progress = { tempoRestante.toFloat() / tempoInicial.toFloat() },
                        modifier = Modifier.width(120.dp).height(8.dp),
                        color = if (tempoRestante <= 300) Color.Red else Color(0xFF4B0082),
                        trackColor = Color.White
                    )
                }
            }

            // LADO VERMELHO
            ScoreActionColumn(
                label = "VERMELHO",
                score = placarVermelho,
                buttonColor = Color(0xFFC62828),
                onGol = onGolVermelho,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScoreActionColumn(label: String, score: Int, buttonColor: Color, onGol: () -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(score.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color.Black)
        Button(
            onClick = onGol,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.height(36.dp)
        ) {
            Text("GOL", fontWeight = FontWeight.Bold)
        }
    }
}
