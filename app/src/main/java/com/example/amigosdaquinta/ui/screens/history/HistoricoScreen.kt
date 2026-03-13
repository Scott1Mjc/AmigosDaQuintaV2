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
import androidx.compose.ui.graphics.Color
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
 * Tela de Histórico de Jogos.
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
                title = { Text("Histórico de Jogos", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            HistoricoHeader()

            Spacer(modifier = Modifier.height(16.dp))

            if (jogos.isEmpty()) {
                EmptyHistoricoState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
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
private fun HistoricoHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Partidas Recentes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Últimos 30 dias de atividades", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
private fun JogoHistoricoItem(jogo: Jogo, dateFormat: SimpleDateFormat, timeFormat: SimpleDateFormat, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFF3F0F5)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Cabeçalho com data centralizada usando Box para precisão
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "${jogo.numeroJogo}° Jogo",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                
                Text(
                    text = dateFormat.format(Date(jogo.data)),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = timeFormat.format(Date(jogo.data)),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.DarkGray,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("BRANCO", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.Gray)
                Text("  ${jogo.placarBranco} x ${jogo.placarVermelho}  ", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("VERMELHO", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Start, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val statusText = when (jogo.status) {
                    StatusJogo.FINALIZADO -> when (jogo.timeVencedor) {
                        TimeColor.BRANCO -> "Vitória: Branco"
                        TimeColor.VERMELHO -> "Vitória: Vermelho"
                        null -> "Empate"
                    }
                    else -> "Em andamento"
                }
                Text(statusText, style = MaterialTheme.typography.labelLarge, color = Color.Black, fontWeight = FontWeight.Medium)
                Text("${jogo.duracao} min", style = MaterialTheme.typography.labelLarge, color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun EmptyHistoricoState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nenhum jogo registrado recentemente", color = Color.Gray)
    }
}
