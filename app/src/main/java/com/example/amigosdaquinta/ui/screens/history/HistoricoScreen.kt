package com.example.amigosdaquinta.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de histórico de jogos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    viewModel: HistoricoViewModel,
    onNavigateBack: () -> Unit,
    onJogoClick: (Long) -> Unit
) {
    val jogos by viewModel.jogos.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.obterJogosPorData(System.currentTimeMillis())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Jogos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
            // Card de informação do histórico - Full Width (Voltando ao anterior)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Histórico de Jogos",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Últimos 30 dias",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (jogos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum jogo registrado nos últimos 30 dias")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jogos) { jogo ->
                        JogoHistoricoItem(
                            jogo = jogo,
                            dateFormat = dateFormat,
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
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // LINHA SUPERIOR: NÚMERO DO JOGO | DATA | HORA
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${jogo.numeroJogo} Jogo",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = dateFormat.format(Date(jogo.data)),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = timeFormat.format(Date(jogo.data)),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LINHA CENTRAL: PLACAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BRANCO",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${jogo.placarBranco} x ${jogo.placarVermelho}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Text(
                    "VERMELHO",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LINHA INFERIOR: RESULTADO | DURAÇÃO
            Box(modifier = Modifier.fillMaxWidth()) {
                val resultadoText = when (jogo.status) {
                    StatusJogo.FINALIZADO -> when (jogo.timeVencedor) {
                        TimeColor.BRANCO -> "Vencedor: Branco"
                        TimeColor.VERMELHO -> "Vencedor: Vermelho"
                        null -> "Empate"
                    }
                    else -> "Em andamento"
                }

                Text(
                    text = resultadoText,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "${jogo.duracao} min",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
