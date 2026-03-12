package com.example.amigosdaquinta.ui.components

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
import kotlinx.coroutines.delay

/**
 * Componente de Cronômetro Regressivo.
 * 
 * Gerencia a contagem de tempo da partida, emitindo alertas visuais nos minutos finais
 * e acionando o callback de encerramento ao atingir zero.
 */
@Composable
fun CronometroComponent(
    duracaoMinutos: Int,
    onTempoEsgotado: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempoRestante by remember { mutableIntStateOf(duracaoMinutos * 60) }
    var pausado by remember { mutableStateOf(false) }
    val tempoInicial = remember { duracaoMinutos * 60 }

    val alerta = tempoRestante <= 300 && tempoRestante > 0

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
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (tempoRestante == 0) "FIM DE JOGO" else if (alerta) "⚠️ MINUTOS FINAIS" else "CRONÔMETRO",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (tempoRestante == 0) Color.Red else Color.DarkGray
            )

            Text(
                text = String.format("%02d:%02d", min, seg),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = if (tempoRestante == 0) Color.Red else Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { tempoRestante.toFloat() / tempoInicial.toFloat() },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = if (alerta) Color.Red else Color(0xFF4B0082),
                trackColor = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (tempoRestante > 0) {
                Button(
                    onClick = { pausado = !pausado },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Icon(if (pausado) Icons.Default.PlayArrow else Icons.Default.Pause, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (pausado) "RETOMAR" else "PAUSAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
