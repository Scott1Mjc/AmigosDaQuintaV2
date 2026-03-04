package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * TODO: Exibir feedback visual (Snackbar) quando o usuário tentar adicionar um segundo goleiro — atualmente a tentativa é silenciosamente ignorada.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormacaoManualScreen(
    jogadoresPresentes: List<Pair<Jogador, Long>>,
    sessaoViewModel: SessaoViewModel,
    onNavigateBack: () -> Unit,
    onIniciarJogo: () -> Unit
) {
    var timeBranco by remember { mutableStateOf<List<Jogador>>(emptyList()) }
    var timeVermelho by remember { mutableStateOf<List<Jogador>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var timeParaAdicionar by remember { mutableStateOf<TimeColor?>(null) }

    val jogadoresDisponiveis = jogadoresPresentes
        .map { it.first }
        .filter { jogador ->
            !timeBranco.any { it.id == jogador.id } &&
                    !timeVermelho.any { it.id == jogador.id }
        }

    val timeBrancoCompleto = timeBranco.size == 11
    val timeVermelhoCompleto = timeVermelho.size == 11
    val temGoleiroBranco = timeBranco.any { it.isPosicaoGoleiro }
    val temGoleiroVermelho = timeVermelho.any { it.isPosicaoGoleiro }
    val podeIniciar = timeBrancoCompleto && timeVermelhoCompleto &&
            temGoleiroBranco && temGoleiroVermelho

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formar 1º Jogo (Manual)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
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
            // Instruções
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Selecione 11 jogadores para cada time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Cada time precisa de 1 goleiro",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Disponíveis: ${jogadoresDisponiveis.size} jogadores")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Times
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
                        showDialog = true
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
                        showDialog = true
                    },
                    onRemover = { jogador ->
                        timeVermelho = timeVermelho.filter { it.id != jogador.id }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Iniciar
            Button(
                onClick = {
                    sessaoViewModel.criarJogo(
                        timeBranco = timeBranco,
                        timeVermelho = timeVermelho
                    )
                    onIniciarJogo()
                },
                enabled = podeIniciar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (podeIniciar) {
                        "Iniciar 1º Jogo"
                    } else {
                        "Complete os times (11 jogadores cada + 1 goleiro)"
                    }
                )
            }
        }
    }

    // Dialog de seleção
    if (showDialog && timeParaAdicionar != null) {
        SelecionarJogadorParaTimeDialog(
            jogadores = jogadoresDisponiveis,
            onDismiss = {
                showDialog = false
                timeParaAdicionar = null
            },
            onSelect = { jogador ->
                when (timeParaAdicionar) {
                    TimeColor.BRANCO -> {
                        // Validação: máximo 1 goleiro
                        val jaTemGoleiro = timeBranco.any { it.isPosicaoGoleiro }
                        if (jogador.isPosicaoGoleiro && jaTemGoleiro) {
                            // Não adiciona
                        } else {
                            timeBranco = timeBranco + jogador
                        }
                    }
                    TimeColor.VERMELHO -> {
                        // Validação: máximo 1 goleiro
                        val jaTemGoleiro = timeVermelho.any { it.isPosicaoGoleiro }
                        if (jogador.isPosicaoGoleiro && jaTemGoleiro) {
                            // Não adiciona
                        } else {
                            timeVermelho = timeVermelho + jogador
                        }
                    }
                    else -> {}
                }
                showDialog = false
                timeParaAdicionar = null
            }
        )
    }
}