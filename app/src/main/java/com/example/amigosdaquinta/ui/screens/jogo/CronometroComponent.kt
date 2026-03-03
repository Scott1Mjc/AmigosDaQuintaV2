package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente visual do cronometro regressivo.
 *
 * Exibe o tempo restante no formato MM:SS e uma barra de progresso
 * que diminui proporcionalmente ao tempo decorrido.
 *
 * Nao controla o tick do cronometro — isso e responsabilidade da JogoScreen
 * via LaunchedEffect. Este componente e puramente de apresentacao.
 *
 * @param tempoRestante Segundos restantes no jogo.
 * @param totalSegundos Duracao total do jogo em segundos (usado para calcular o progresso).
 */
@Composable
fun CronometroComponent(
    tempoRestante: Int,
    totalSegundos: Int
) {
    val minutos = tempoRestante / 60
    val segundos = tempoRestante % 60
    val progresso = if (totalSegundos > 0) tempoRestante.toFloat() / totalSegundos else 0f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Tempo Restante", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%02d:%02d", minutos, segundos),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 56.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progresso,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
        }
    }
}