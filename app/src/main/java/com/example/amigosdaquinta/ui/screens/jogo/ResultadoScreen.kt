package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Tela de resultado exibida apos o encerramento de uma partida.
 *
 * Apresenta vencedor, placar final e duracao, e oferece duas acoes:
 * formar o proximo jogo (mantendo a sessao) ou encerrar a sessao do dia.
 *
 * Nao possui logica propria — recebe todos os dados como parametros
 * e delega as acoes via callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoScreen(
    vencedor: TimeColor?,
    placarBranco: Int,
    placarVermelho: Int,
    duracaoMinutos: Int,
    onProximoJogo: () -> Unit,
    onEncerrar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Resultado Final") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val tituloVencedor = when (vencedor) {
                        TimeColor.BRANCO -> "TIME BRANCO VENCEU!"
                        TimeColor.VERMELHO -> "TIME VERMELHO VENCEU!"
                        null -> "EMPATE!"
                    }

                    Text(
                        text = tituloVencedor,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Placar Final:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$placarBranco x $placarVermelho",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 56.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Duracao: $duracaoMinutos minutos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onProximoJogo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Formar Proximo Jogo")
                }

                OutlinedButton(
                    onClick = onEncerrar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Encerrar Sessao")
                }
            }
        }
    }
}