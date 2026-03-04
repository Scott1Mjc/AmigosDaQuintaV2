package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente de placar interativo da partida.
 *
 * Exibe o placar atual de ambos os times e dois botoes para registrar gols.
 * Cada toque em um botao incrementa o placar no [SessaoViewModel] via callback,
 * e o componente reflete o novo valor na proxima recomposicao.
 *
 * Nao possui estado proprio — e totalmente controlado pelos parametros recebidos.
 */
@Composable
fun PlacarComponent(
    placarBranco: Int,
    placarVermelho: Int,
    onGolBranco: () -> Unit,
    onGolVermelho: () -> Unit,
    modifier: Modifier
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "BRANCO", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$placarBranco",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 48.sp
                    )
                    Text(text = " x ", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "$placarVermelho",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 48.sp
                    )
                }
                Text(text = "VERMELHO", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGolBranco,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+1 GOL BRANCO")
                }
                Button(
                    onClick = onGolVermelho,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("+1 GOL VERMELHO")
                }
            }
        }
    }
}