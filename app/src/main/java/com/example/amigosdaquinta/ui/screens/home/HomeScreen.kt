package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel

/**
 * Tela principal do app.
 *
 * Exibe a lista de jogadores cadastrados, contador de status para formação de times
 * e navegação para as demais telas.
 *
 * Long press no título aciona [JogadoresViewModel.popularBancoComJogadoresDeTeste],
 * funcionalidade de desenvolvimento que deve ser removida antes do build de produção.
 *
 * A ordenação por número de camisa é derivada via [derivedStateOf] para evitar
 * recomposições desnecessárias quando o estado muda mas a ordem não.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JogadoresViewModel,
    onNavigateToPresenca: () -> Unit = {},
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {}
) {
    val jogadores by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val jogadoresOrdenados by remember(jogadores) {
        derivedStateOf { jogadores.sortedBy { it.numeroCamisa } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gerenciador de Futebol",
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { viewModel.popularBancoComJogadoresDeTeste() }
                            )
                        }
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToDebug) {
                        Text("D")
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
            Text(
                text = "Jogadores Cadastrados (${jogadoresOrdenados.size})",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                jogadoresOrdenados.isEmpty() -> {
                    EmptyJogadoresState()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = jogadoresOrdenados, key = { it.id }) { jogador ->
                            JogadorItem(jogador)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (jogadoresOrdenados.isNotEmpty()) {
                Button(
                    onClick = onNavigateToPresenca,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Lista de Presenca")
                }

                OutlinedButton(
                    onClick = onNavigateToHistorico,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Historico")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(text = "Times Formados", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))

            TimesStatusCard(totalJogadores = jogadoresOrdenados.size)
        }
    }

    if (showDialog) {
        AdicionarJogadorDialog(
            onDismiss = { showDialog = false },
            onConfirm = { nome, numero, isGoleiro ->
                viewModel.adicionarJogador(nome, numero, isGoleiro)
                showDialog = false
            }
        )
    }
}

@Composable
private fun EmptyJogadoresState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bola", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nenhum jogador cadastrado ainda.")
        Text(
            text = "Clique no botao + para adicionar.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TimesStatusCard(totalJogadores: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (totalJogadores >= 22) {
                Text("Jogadores suficientes para formar times!")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Time Branco: 11 jogadores")
                Text("Time Vermelho: 11 jogadores")
            } else {
                Text("Aguardando mais jogadores...")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Faltam ${22 - totalJogadores} jogadores")
            }
        }
    }
}