package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Tela de Resultado Final da Partida.
 *
 * Apresenta o placar definitivo e o vencedor, oferecendo opções para 
 * dar continuidade à sessão (próximo jogo) ou encerrar as atividades do dia.
 */
@Composable
fun ResultadoScreen(
    vencedor: TimeColor?,
    placarBranco: Int,
    placarVermelho: Int,
    onProximoJogo: () -> Unit,
    onEncerrar: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth > 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FIM DE JOGO",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card de Placar Final com estilo padronizado
            Surface(
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.7f else 1f),
                shape = MaterialTheme.shapes.large,
                color = Color(0xFFEBE8EC)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PLACAR FINAL", style = MaterialTheme.typography.titleMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Invertido: Vermelho à esquerda, Branco à direita
                        TeamScoreColumn(label = "VERMELHO", score = placarVermelho)
                        Text("x", style = MaterialTheme.typography.displayMedium, color = Color.Gray)
                        TeamScoreColumn(label = "BRANCO", score = placarBranco)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mensagem de Vencedor
            val (resultadoTexto, corResultado) = when (vencedor) {
                TimeColor.BRANCO -> "TIME BRANCO VENCEU!" to Color.Black
                TimeColor.VERMELHO -> "TIME VERMELHO VENCEU!" to Color.Black
                null -> "EMPATE TÉCNICO!" to Color.Gray
            }

            Text(
                text = resultadoTexto,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = corResultado,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Ações de Continuidade - Ajuste de largura para tablets
            Column(
                modifier = Modifier.fillMaxWidth(if (isTablet) 0.5f else 1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onProximoJogo,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
                ) {
                    Text("FORMAR PRÓXIMO JOGO", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEncerrar,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Text("ENCERRAR SESSÃO DO DIA", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun TeamScoreColumn(label: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Text(score.toString(), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = Color.Black)
    }
}
