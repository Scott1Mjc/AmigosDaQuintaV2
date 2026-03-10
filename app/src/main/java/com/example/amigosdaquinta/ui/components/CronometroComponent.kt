package com.example.amigosdaquinta.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Componente de cronômetro regressivo para partidas de futebol.
 *
 * Funcionalidades:
 * - Contagem regressiva configurável (30min ou 15min)
 * - Pausar/Retomar
 * - Finalização automática ao chegar a zero
 * - Aviso visual quando faltam 5 minutos
 *
 * @param duracaoMinutos Duração total em minutos
 * @param onTempoEsgotado Callback chamado quando tempo chega a zero
 * @param modifier Modificador do componente
 */
@Composable
fun CronometroComponent(
    duracaoMinutos: Int,
    onTempoEsgotado: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado do tempo (em segundos)
    var tempoRestanteSegundos by remember { mutableIntStateOf(duracaoMinutos * 60) }
    var estaPausado by remember { mutableStateOf(false) }
    var tempoInicial by remember { mutableIntStateOf(duracaoMinutos * 60) }

    // Aviso quando faltam 5 minutos
    val tempoAcabando = tempoRestanteSegundos <= 300 && tempoRestanteSegundos > 0

    // Efeito para contar o tempo
    LaunchedEffect(estaPausado, tempoRestanteSegundos) {
        if (!estaPausado && tempoRestanteSegundos > 0) {
            delay(1000L)
            tempoRestanteSegundos--

            // Quando chegar a zero, finalizar automaticamente
            if (tempoRestanteSegundos == 0) {
                onTempoEsgotado()
            }
        }
    }

    // Calcular minutos e segundos
    val minutos = tempoRestanteSegundos / 60
    val segundos = tempoRestanteSegundos % 60

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                tempoRestanteSegundos == 0 -> MaterialTheme.colorScheme.errorContainer
                tempoAcabando -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = if (tempoRestanteSegundos == 0) {
                    "TEMPO ESGOTADO!"
                } else if (tempoAcabando) {
                    "⚠️ ÚLTIMOS 5 MINUTOS"
                } else {
                    "Tempo de Jogo"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Display do tempo
            Text(
                text = String.format("%02d:%02d", minutos, segundos),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    tempoRestanteSegundos == 0 -> MaterialTheme.colorScheme.error
                    tempoAcabando -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // Barra de progresso
            LinearProgressIndicator(
                progress = { tempoRestanteSegundos.toFloat() / tempoInicial.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    tempoAcabando -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
            )

            // Botão Pausar/Retomar
            if (tempoRestanteSegundos > 0) {
                Button(
                    onClick = { estaPausado = !estaPausado },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (estaPausado) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (estaPausado) "Retomar" else "Pausar",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}