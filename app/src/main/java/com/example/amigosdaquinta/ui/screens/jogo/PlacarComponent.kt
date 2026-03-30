package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Componente Central de Placar e Cronômetro.
 * 
 * Agora utiliza o estado centralizado do SessaoViewModel para manter o tempo
 * sincronizado mesmo entre navegação de telas.
 */
@Composable
fun PlacarComponent(
    placarBranco: Int,
    placarVermelho: Int,
    tempoRestanteSegundos: Int,
    pausado: Boolean,
    duracaoMinutos: Int,
    onGolBranco: () -> Unit,
    onGolVermelho: () -> Unit,
    onAlternarPausa: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tempoInicialTotal = duracaoMinutos * 60
    val min = tempoRestanteSegundos / 60
    val seg = tempoRestanteSegundos % 60

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
            // LADO VERMELHO
            ScoreActionColumn(
                label = "VERMELHO",
                score = placarVermelho,
                buttonColor = Color(0xFFC62828),
                onGol = onGolVermelho,
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
                    color = if (tempoRestanteSegundos <= 300 && tempoRestanteSegundos > 0) Color.Red else Color.Black
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = onAlternarPausa, modifier = Modifier.size(40.dp)) {
                        Icon(
                            if (pausado) Icons.Default.PlayArrow else Icons.Default.Pause, 
                            null, 
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { 
                            if (tempoInicialTotal > 0) tempoRestanteSegundos.toFloat() / tempoInicialTotal.toFloat() 
                            else 0f 
                        },
                        modifier = Modifier.width(120.dp).height(8.dp),
                        color = if (tempoRestanteSegundos <= 300) Color.Red else Color(0xFF4B0082),
                        trackColor = Color.White
                    )
                }
            }

            // LADO BRANCO
            ScoreActionColumn(
                label = "BRANCO",
                score = placarBranco,
                buttonColor = Color(0xFF4B0082),
                onGol = onGolBranco,
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