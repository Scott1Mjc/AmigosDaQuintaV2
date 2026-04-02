package com.example.amigosdaquinta.ui.screens.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela para apontar jogadores que saíram durante a partida.
 * Permite remover jogadores que estavam escalados, tirando-os totalmente da sessão e da fila de presença.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogadoresSairamDurantePartidaScreen(
    sessaoViewModel: SessaoViewModel,
    onNavigateBack: () -> Unit
) {
    val timeBranco by sessaoViewModel.timeBrancoAtual.collectAsState()
    val timeVermelho by sessaoViewModel.timeVermelhoAtual.collectAsState()
    val jogadoresSaindo = remember { mutableStateListOf<Long>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quem saiu da partida?") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = {
                        if (jogadoresSaindo.isNotEmpty()) {
                            sessaoViewModel.removerVariosDaListaPresenca(jogadoresSaindo.toList())
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONFIRMAR REMOÇÃO", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Selecione os atletas que foram embora ou se lesionaram. Eles serão removidos permanentemente desta sessão.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (timeVermelho.isNotEmpty()) {
                item {
                    Text("TIME VERMELHO", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Red)
                }
                items(timeVermelho, key = { it.id }) { jogador ->
                    JogadorSairItem(
                        jogador = jogador,
                        isSelected = jogadoresSaindo.contains(jogador.id),
                        onToggle = {
                            if (jogadoresSaindo.contains(jogador.id)) jogadoresSaindo.remove(jogador.id)
                            else jogadoresSaindo.add(jogador.id)
                        }
                    )
                }
            }

            if (timeBranco.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TIME BRANCO", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4B0082))
                }
                items(timeBranco, key = { it.id }) { jogador ->
                    JogadorSairItem(
                        jogador = jogador,
                        isSelected = jogadoresSaindo.contains(jogador.id),
                        onToggle = {
                            if (jogadoresSaindo.contains(jogador.id)) jogadoresSaindo.remove(jogador.id)
                            else jogadoresSaindo.add(jogador.id)
                        }
                    )
                }
            }
            
            if (timeBranco.isEmpty() && timeVermelho.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum jogador escalado encontrado.")
                    }
                }
            }
        }
    }
}

@Composable
private fun JogadorSairItem(
    jogador: Jogador,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFEBEE) else Color.White
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.Red) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.PersonRemove else Icons.Default.Check,
                contentDescription = null,
                tint = if (isSelected) Color.Red else Color.LightGray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (if (jogador.isPosicaoGoleiro) "[GOL] " else "") + jogador.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("Camisa #${jogador.numeroCamisa}", style = MaterialTheme.typography.bodySmall)
            }
            if (isSelected) {
                Text("SAIU", color = Color.Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
