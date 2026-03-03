package com.example.amigosdaquinta.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela de ferramentas de desenvolvimento e testes manuais.
 *
 * Permite popular o banco com jogadores fictícios, adicionar todos à lista de presença
 * e inspecionar o estado atual da sessão.
 *
 * Esta tela nao deve ser acessível em builds de producao.
 * O acesso esta vinculado ao botão de debug na HomeScreen (long press no título).
 *
 * Thresholds exibidos no painel de estado:
 * - 22+: sessao pronta para iniciar jogos.
 * - 30+: regra de rotacao forcada ativa.
 * - 33+: modo rotacao total ativo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    jogadoresViewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    onNavigateBack: () -> Unit
) {
    val jogadores by jogadoresViewModel.jogadores.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug / Testes") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Ferramentas de Teste", style = MaterialTheme.typography.titleLarge)

            Divider()

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Popular Banco de Dados", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Adiciona 40 jogadores de teste (5 goleiros + 35 linha)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { jogadoresViewModel.popularBancoComJogadoresDeTeste() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Adicionar 40 Jogadores")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Lista de Presenca", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Adiciona todos os jogadores cadastrados a lista automaticamente",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            jogadores.forEach { sessaoViewModel.adicionarAListaPresenca(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Adicionar Todos a Lista")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Estado Atual", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Jogadores cadastrados: ${jogadores.size}")
                    Text("Jogadores na lista: ${listaPresenca.size}")

                    val (textoStatus, corStatus) = when {
                        listaPresenca.size >= 33 -> "ROTACAO TOTAL ATIVA (33+)" to MaterialTheme.colorScheme.primary
                        listaPresenca.size >= 30 -> "ROTACAO FORCADA ATIVA (30+)" to MaterialTheme.colorScheme.primary
                        listaPresenca.size >= 22 -> "Pronto para jogar (22+)" to MaterialTheme.colorScheme.primary
                        else -> null to null
                    }

                    if (textoStatus != null && corStatus != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = textoStatus, color = corStatus)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Limpar Dados", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { sessaoViewModel.limparSessaoCompleta() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Limpar Lista de Presenca")
                    }
                }
            }
        }
    }
}