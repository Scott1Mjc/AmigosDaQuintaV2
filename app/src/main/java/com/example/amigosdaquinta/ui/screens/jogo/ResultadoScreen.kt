package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Tela de resultado exibida apos o encerramento de uma partida.
 *
 * Apresenta vencedor e placar final, e oferece duas acoes:
 * formar o proximo jogo (mantendo a sessao) ou encerrar a sessao do dia.
 *
 * Não possui logica propria — recebe todos os dados como parametros
 * e delega as acoes via callbacks.
 * Tela de resultado do jogo finalizado.
 */

@Composable
fun ResultadoScreen(
    vencedor: TimeColor?,
    placarBranco: Int,
    placarVermelho: Int,
    onProximoJogo: () -> Unit,
    onEncerrar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Jogo Finalizado",
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Placar final
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Placar Final",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "BRANCO",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            placarBranco.toString(),
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "VERMELHO",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            placarVermelho.toString(),
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resultado
        Text(
            text = when (vencedor) {
                TimeColor.BRANCO -> "Time BRANCO venceu!"
                TimeColor.VERMELHO -> "Time VERMELHO venceu!"
                null -> "EMPATE!"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = when (vencedor) {
                TimeColor.BRANCO -> MaterialTheme.colorScheme.primary
                TimeColor.VERMELHO -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botões
        Button(
            onClick = onProximoJogo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Formar Próximo Jogo")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onEncerrar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Encerrar Sessão")
        }
    }
}