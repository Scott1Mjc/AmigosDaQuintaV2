package com.example.amigosdaquinta.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de historico de jogos.
 *
 * Exibe os jogos registrados nos ultimos 30 dias, carregados via
 * [HistoricoViewModel.obterJogosPorData] no [LaunchedEffect] de entrada.
 *
 * Cada item e clicavel e navega para o detalhe do jogo via [onJogoClick].
 *
 * TODO: Implementar filtro de data interativo — atualmente fixo em 30 dias a partir de hoje.
 * TODO: Colocar data da partida junto a hora no display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    viewModel: HistoricoViewModel,
    onNavigateBack: () -> Unit,
    onJogoClick: (Long) -> Unit
) {
    val jogos by viewModel.jogos.collectAsState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.obterJogosPorData(System.currentTimeMillis())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historico de Jogos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Historico de Jogos", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Ultimos 30 dias", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (jogos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum jogo registrado")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(jogos) { jogo ->
                        JogoHistoricoItem(
                            jogo = jogo,
                            timeFormat = timeFormat,
                            onClick = { onJogoClick(jogo.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JogoHistoricoItem(
    jogo: Jogo,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${jogo.numeroJogo} Jogo",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = timeFormat.format(Date(jogo.data)),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BRANCO", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${jogo.placarBranco} x ${jogo.placarVermelho}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text("VERMELHO", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val statusText = when (jogo.status) {
                    StatusJogo.FINALIZADO -> when (jogo.timeVencedor) {
                        TimeColor.BRANCO -> "Branco venceu"
                        TimeColor.VERMELHO -> "Vermelho venceu"
                        null -> "Empate"
                    }
                    StatusJogo.EM_ANDAMENTO -> "Em andamento"
                    StatusJogo.AGUARDANDO -> "Aguardando"
                    StatusJogo.CANCELADO -> "Cancelado"
                }

                Text(
                    text = statusText,
                    color = when {
                        jogo.timeVencedor == TimeColor.BRANCO -> MaterialTheme.colorScheme.primary
                        jogo.timeVencedor == TimeColor.VERMELHO -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${jogo.duracao} min",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}