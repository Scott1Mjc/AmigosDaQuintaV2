package com.example.amigosdaquinta.ui.screens.presenca

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de lista de presença do dia.
 *
 * Registra a ordem de chegada dos jogadores para a sessão atual.
 * A sessão é inicializada via [SessaoViewModel.iniciarNovoDia] caso ainda não exista,
 * garantindo que a tela seja sempre exibida com um contexto de sessão válido.
 *
 * Thresholds de comportamento controlados aqui:
 * - 22+ jogadores: habilita o botão de formar times.
 * - 33+ jogadores: exibe aviso de modo rotação total (todos jogam por ordem de chegada).
 *
 * O dialog de seleção filtra jogadores já presentes para evitar duplicatas na lista.
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
    val sessao by sessaoViewModel.sessaoAtual.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    val modoRotacaoTotal = listaPresenca.size >= 33
    var showDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dataAtual = remember { dateFormat.format(Date()) }

    LaunchedEffect(Unit) {
        if (sessao == null) sessaoViewModel.iniciarNovoDia()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Presenca - $dataAtual") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            PresencaStatusCard(
                total = listaPresenca.size,
                modoRotacaoTotal = modoRotacaoTotal
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (listaPresenca.isEmpty()) {
                EmptyPresencaState()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (listaPresenca.size >= 22) {
                        "Formar 1 Jogo (${listaPresenca.size} jogadores)"
                    } else {
                        "Aguardando jogadores (${listaPresenca.size}/22)"
                    }
                )
            }
        }
    }

    if (showDialog) {
        val jogadoresNaLista = listaPresenca.map { it.first.id }.toSet()
        SelecionarJogadorDialog(
            jogadores = jogadores.filter { it.id !in jogadoresNaLista },
            onDismiss = { showDialog = false },
            onSelect = { jogador ->
                sessaoViewModel.adicionarAListaPresenca(jogador)
                showDialog = false
            }
        )
    }
}

@Composable
private fun PresencaStatusCard(total: Int, modoRotacaoTotal: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Total: $total jogadores",
                style = MaterialTheme.typography.titleMedium
            )

            if (modoRotacaoTotal) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "MODO ROTACAO TOTAL ATIVADO",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "33+ jogadores: todos jogam por ordem de chegada",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (total >= 22) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pronto para formar times!",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyPresencaState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Jogadores", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nenhum jogador na lista ainda")
        Text(
            text = "Clique no + para adicionar",
            style = MaterialTheme.typography.bodySmall
        )
    }
}