package com.example.amigosdaquinta.ui.screens.formacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela de Formação Manual de Partida.
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

    val podeIniciar = timeBranco.size == 11 && timeVermelho.size == 11 &&
            timeBranco.any { it.isPosicaoGoleiro } && timeVermelho.any { it.isPosicaoGoleiro }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escalação Manual", color = Color.Black) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Painel de Instruções
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFEBE8EC)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configuração dos Times", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Selecione 11 atletas para cada lado (mínimo 1 goleiro)", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Área de Escalação (Lado a Lado)
            Row(
                modifier = Modifier.height(500.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeFormadoCard(
                    modifier = Modifier.weight(1f),
                    titulo = "TIME BRANCO",
                    cor = Color(0xFFE8E2FF),
                    jogadores = timeBranco.sortedByDescending { it.isPosicaoGoleiro },
                    completo = timeBranco.size == 11,
                    temGoleiro = timeBranco.any { it.isPosicaoGoleiro },
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
                    cor = Color(0xFFFFE1E1),
                    jogadores = timeVermelho.sortedByDescending { it.isPosicaoGoleiro },
                    completo = timeVermelho.size == 11,
                    temGoleiro = timeVermelho.any { it.isPosicaoGoleiro },
                    onAdicionar = {
                        timeParaAdicionar = TimeColor.VERMELHO
                        showDialog = true
                    },
                    onRemover = { jogador ->
                        timeVermelho = timeVermelho.filter { it.id != jogador.id }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de Confirmação Final
            Button(
                onClick = {
                    sessaoViewModel.criarJogo(timeBranco, timeVermelho)
                    onIniciarJogo()
                },
                enabled = podeIniciar,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
            ) {
                Text(
                    if (podeIniciar) "INICIAR PARTIDA" else "AGUARDANDO ESCALAÇÃO COMPLETA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDialog && timeParaAdicionar != null) {
        val timeAlvo = if (timeParaAdicionar == TimeColor.BRANCO) timeBranco else timeVermelho
        val temGoleiro = timeAlvo.any { it.isPosicaoGoleiro }
        val qtdLinha = timeAlvo.count { !it.isPosicaoGoleiro }
        
        // Bloqueia goleiros se já tem um, ou linha se já tem 10
        // No diálogo passamos apenas bloquearGoleiros, mas na seleção tratamos ambos
        SelecionarJogadorParaTimeDialog(
            jogadores = jogadoresDisponiveis,
            bloquearGoleiros = temGoleiro,
            onDismiss = {
                showDialog = false
                timeParaAdicionar = null
            },
            onSelect = { jogador ->
                val podeAdd = if (jogador.isPosicaoGoleiro) !temGoleiro else qtdLinha < 10
                if (podeAdd) {
                    if (timeParaAdicionar == TimeColor.BRANCO) timeBranco = timeBranco + jogador
                    else timeVermelho = timeVermelho + jogador
                }
                showDialog = false
                timeParaAdicionar = null
            }
        )
    }
}
