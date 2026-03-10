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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Componente unificado de placar e cronômetro otimizado para economizar espaço.
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
    var tempoRestanteSegundos by rememberSaveable { mutableIntStateOf(duracaoMinutos * 60) }
    var estaPausado by rememberSaveable { mutableStateOf(false) }
    val tempoInicial by rememberSaveable { mutableIntStateOf(duracaoMinutos * 60) }

    LaunchedEffect(estaPausado, tempoRestanteSegundos) {
        if (!estaPausado && tempoRestanteSegundos > 0) {
            delay(1000L)
            tempoRestanteSegundos--
            if (tempoRestanteSegundos == 0) onTempoEsgotado()
        }
    }

    val minutos = tempoRestanteSegundos / 60
    val segundos = tempoRestanteSegundos % 60

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // TIME BRANCO + BOTÃO GOL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text("BRANCO", style = MaterialTheme.typography.titleSmall)
                Text(
                    placarBranco.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onGolBranco,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("+1 GOL", style = MaterialTheme.typography.labelSmall)
                }
            }

            // CRONÔMETRO CENTRAL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    text = String.format("%02d:%02d", minutos, segundos),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (tempoRestanteSegundos <= 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { estaPausado = !estaPausado },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (estaPausado) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { tempoRestanteSegundos.toFloat() / tempoInicial.toFloat() },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // TIME VERMELHO + BOTÃO GOL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text("VERMELHO", style = MaterialTheme.typography.titleSmall)
                Text(
                    placarVermelho.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onGolVermelho,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("+1 GOL", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
