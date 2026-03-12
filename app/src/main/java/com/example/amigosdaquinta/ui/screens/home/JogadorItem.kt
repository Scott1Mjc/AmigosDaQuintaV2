package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Representação visual de um jogador em listagens simples.
 * 
 * Exibe o nome, a posição principal e o número da camisa do atleta em um card limpo.
 *
 * @param jogador Instância da entidade Jogador a ser exibida.
 */
@Composable
fun JogadorItem(jogador: Jogador) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogador.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Jogador de Linha",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            // Badge de número padronizado
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color(0xFFF0EDFF)
            ) {
                Text(
                    text = "#${jogador.numeroCamisa}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4B0082),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
