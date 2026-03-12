package com.example.amigosdaquinta.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Painel de Ferramentas de Desenvolvimento.
 * 
 * Centraliza funcionalidades de teste, como a população em massa da lista de presença
 * e o reset completo do estado da sessão.
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
                title = { Text("Painel de Debug", color = Color.Black) },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Utilidades de Teste", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Card: Gerenciamento de Presença em Lote
            DebugActionCard(
                titulo = "Lista de Presença",
                descricao = "Adiciona todos os atletas cadastrados à fila de uma só vez.",
                buttonText = "CONFIRMAR TODOS",
                onAction = { jogadores.forEach { sessaoViewModel.adicionarAListaPresenca(it) } }
            )

            // Card: Monitoramento de Estado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Métricas da Sessão", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Atletas Cadastrados: ${jogadores.size}", style = MaterialTheme.typography.bodyMedium)
                    Text("• Atletas na Fila: ${listaPresenca.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Card: Reset de Fábrica
            DebugActionCard(
                titulo = "Reset de Dados",
                descricao = "Limpa permanentemente a fila de presença e o jogo atual.",
                buttonText = "LIMPAR SESSÃO",
                buttonColor = Color.Red.copy(alpha = 0.8f),
                onAction = { sessaoViewModel.iniciarNovoDia() }
            )
        }
    }
}

@Composable
private fun DebugActionCard(titulo: String, descricao: String, buttonText: String, buttonColor: Color = Color(0xFF4B0082), onAction: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(descricao, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = MaterialTheme.shapes.small
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
