package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Item da lista de jogadores cadastrados na HomeScreen.
 * Exibe nome, posicao e numero de camisa. Sem interacao — apenas leitura.
 */
@Composable
fun JogadorItem(jogador: Jogador) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = jogador.nome,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (jogador.isPosicaoGoleiro) "Goleiro" else "Jogador de linha",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "#${jogador.numeroCamisa}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}