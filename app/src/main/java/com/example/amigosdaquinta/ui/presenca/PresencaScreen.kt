package com.example.amigosdaquinta.ui.presenca

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de Gestão de Presença.
 * 
 * Registra a ordem de chegada dos atletas para a sessão atual.
 * Exibe contagem total e validação para início da formação de times.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresencaScreen(
    jogadoresViewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    onNavigateBack: () -> Unit,
    onFormarTimes: () -> Unit = {}
) {
    val jogadores by jogadoresViewModel.jogadores.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dataAtual = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Presença - $dataAtual", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF4B0082),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Adicionar Presença")
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            PresencaSummaryCard(total = listaPresenca.size)

            Spacer(modifier = Modifier.height(16.dp))

            if (listaPresenca.isEmpty()) {
                EmptyPresencaView()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = listaPresenca,
                        key = { _, item -> item.first.id }
                    ) { index, item ->
                        PresencaItem(
                            ordem = index + 1,
                            jogador = item.first,
                            horario = timeFormat.format(Date(item.second)),
                            onRemove = { sessaoViewModel.removerDaListaPresenca(item.first.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFormarTimes,
                enabled = listaPresenca.size >= 22,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
            ) {
                Text(
                    if (listaPresenca.size >= 22) "FORMAR TIMES (${listaPresenca.size})" else "AGUARDANDO MÍNIMO (22)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDialog) {
        val idsPresentes = listaPresenca.map { it.first.id }.toSet()
        SelecionarJogadorDialog(
            jogadores = jogadores.filter { it.id !in idsPresentes },
            onDismiss = { showDialog = false },
            onSelect = {
                sessaoViewModel.adicionarAListaPresenca(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun PresencaSummaryCard(total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumo de Presença", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Total de $total jogadores confirmados", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
private fun EmptyPresencaView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nenhum atleta na lista", color = Color.Gray)
    }
}
