package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela de formação manual do primeiro jogo.
 *
 * O usuário seleciona individualmente os 11 jogadores de cada time.
 * Validações ativas: máximo 1 goleiro por time, exatamente 11 jogadores por time.
 *
 * Os estados [timeBranco] e [timeVermelho] usam [rememberSaveable] para sobreviver
 * a rotações de tela sem perder o progresso da montagem.
 *
 * Ao tocar em voltar com progresso não salvo, um dialog de confirmação é exibido.
 *
 * TODO: Exibir feedback visual (Snackbar) quando o usuário tentar adicionar um segundo
 * goleiro — atualmente a tentativa é silenciosamente ignorada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormacaoManualScreen(
    jogadoresPresentes: List<Pair<Jogador, Long>>,
    sessaoViewModel: SessaoViewModel,
    onNavigateBack: () -> Unit,
    onIniciarJogo: () -> Unit
) {
    var timeBranco by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var timeVermelho by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var showSelecionarDialog by remember { mutableStateOf(false) }
    var timeParaAdicionar by remember { mutableStateOf<TimeColor?>(null) }
    var showConfirmBackDialog by remember { mutableStateOf(false) }

    val jogadoresDisponiveis = remember(timeBranco, timeVermelho, jogadoresPresentes) {
        val idsEmTime = (timeBranco + timeVermelho).map { it.id }.toSet()
        jogadoresPresentes.map { it.first }.filter { it.id !in idsEmTime }
    }

    val timeBrancoCompleto = timeBranco.size == 11
    val timeVermelhoCompleto = timeVermelho.size == 11
    val temGoleiroBranco = timeBranco.any { it.isPosicaoGoleiro }
    val temGoleiroVermelho = timeVermelho.any { it.isPosicaoGoleiro }
    val podeIniciar = timeBrancoCompleto && timeVermelhoCompleto && temGoleiroBranco && temGoleiroVermelho
    val temProgresso = timeBranco.isNotEmpty() || timeVermelho.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formar 1 Jogo (Manual)") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (temProgresso) showConfirmBackDialog = true else onNavigateBack()
                        }
                    ) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selecione 11 jogadores para cada time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Cada time precisa de 1 goleiro",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Disponiveis: ${jogadoresDisponiveis.size} jogadores")
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeFormadoCard(
                    modifier = Modifier.weight(1f),
                    titulo = "TIME BRANCO",
                    cor = MaterialTheme.colorScheme.primaryContainer,
                    jogadores = timeBranco,
                    completo = timeBrancoCompleto,
                    temGoleiro = temGoleiroBranco,
                    onAdicionar = {
                        timeParaAdicionar = TimeColor.BRANCO
                        showSelecionarDialog = true
                    },
                    onRemover = { jogador ->
                        timeBranco = timeBranco.filter { it.id != jogador.id }
                    }
                )

                TimeFormadoCard(
                    modifier = Modifier.weight(1f),
                    titulo = "TIME VERMELHO",
                    cor = MaterialTheme.colorScheme.errorContainer,
                    jogadores = timeVermelho,
                    completo = timeVermelhoCompleto,
                    temGoleiro = temGoleiroVermelho,
                    onAdicionar = {
                        timeParaAdicionar = TimeColor.VERMELHO
                        showSelecionarDialog = true
                    },
                    onRemover = { jogador ->
                        timeVermelho = timeVermelho.filter { it.id != jogador.id }
                    }
                )
            }

            Button(
                onClick = {
                    sessaoViewModel.criarPrimeiroJogo(
                        timeBranco = timeBranco,
                        timeVermelho = timeVermelho,
                        duracao = 30
                    )
                    onIniciarJogo()
                },
                enabled = podeIniciar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (podeIniciar) {
                        "Iniciar 1 Jogo - 30 minutos"
                    } else {
                        buildString {
                            append("Complete os times")
                            if (!timeBrancoCompleto || !timeVermelhoCompleto) {
                                append(" (B: ${timeBranco.size}/11, V: ${timeVermelho.size}/11)")
                            }
                            if (!temGoleiroBranco) append(" - Falta goleiro Branco")
                            if (!temGoleiroVermelho) append(" - Falta goleiro Vermelho")
                        }
                    }
                )
            }
        }
    }

    if (showSelecionarDialog && timeParaAdicionar != null) {
        SelecionarJogadorParaTimeDialog(
            jogadores = jogadoresDisponiveis,
            onDismiss = {
                showSelecionarDialog = false
                timeParaAdicionar = null
            },
            onSelect = { jogador ->
                when (timeParaAdicionar) {
                    TimeColor.BRANCO -> {
                        val jaTemGoleiro = timeBranco.any { it.isPosicaoGoleiro }
                        if (!jogador.isPosicaoGoleiro || !jaTemGoleiro) {
                            timeBranco = timeBranco + jogador
                        }
                    }
                    TimeColor.VERMELHO -> {
                        val jaTemGoleiro = timeVermelho.any { it.isPosicaoGoleiro }
                        if (!jogador.isPosicaoGoleiro || !jaTemGoleiro) {
                            timeVermelho = timeVermelho + jogador
                        }
                    }
                    else -> {}
                }
                showSelecionarDialog = false
                timeParaAdicionar = null
            }
        )
    }

    if (showConfirmBackDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmBackDialog = false },
            title = { Text("Descartar formacao?") },
            text = { Text("Os times que voce formou serao perdidos. Deseja voltar mesmo assim?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmBackDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Voltar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmBackDialog = false }) {
                    Text("Continuar Formando")
                }
            }
        )
    }
}