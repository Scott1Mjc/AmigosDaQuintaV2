package com.example.amigosdaquinta.ui.screens.jogadores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.ui.screens.home.AdicionarJogadorDialog

/**
 * Tela para gestão do elenco (CRUD de Jogadores).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarJogadoresScreen(
    viewModel: JogadoresViewModel,
    onNavigateBack: () -> Unit
) {
    val jogadores by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erroMensagem by viewModel.erroMensagem.collectAsState()

    var showAdicionarDialog by remember { mutableStateOf(false) }
    var jogadorParaEditar by remember { mutableStateOf<Jogador?>(null) }
    var jogadorParaRemover by remember { mutableStateOf<Jogador?>(null) }

    // Garante que a lista de jogadores mostrada nesta tela não seja afetada por buscas residuais da HomeScreen
    DisposableEffect(Unit) {
        viewModel.buscarPorNome("") // Reseta qualquer busca ao entrar na tela
        onDispose { }
    }

    val jogadoresOrdenados by remember(jogadores) {
        derivedStateOf { jogadores.sortedBy { it.numeroCamisa } }
    }
    
    val numerosEmUso = remember(jogadores) {
        jogadores.map { it.numeroCamisa }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Jogadores", color = Color.Black) },
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
                onClick = { showAdicionarDialog = true },
                containerColor = Color(0xFF4B0082),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Adicionar Jogador")
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val columns = if (maxWidth > 600.dp) 2 else 1
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4B0082))
            } else if (jogadores.isEmpty()) {
                EmptyJogadoresState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    item(span = { GridItemSpan(columns) }) {
                        TotalJogadoresCard(total = jogadores.size)
                    }

                    items(items = jogadoresOrdenados, key = { it.id }) { jogador ->
                        JogadorGerenciarItem(
                            jogador = jogador,
                            onEditar = { jogadorParaEditar = jogador },
                            onRemover = { jogadorParaRemover = jogador }
                        )
                    }
                }
            }
        }
    }

    // region Dialogs de Gestão

    if (showAdicionarDialog) {
        AdicionarJogadorDialog(
            numerosExistentes = numerosEmUso,
            onDismiss = { showAdicionarDialog = false },
            onConfirm = { nome, numero, isGoleiro ->
                viewModel.adicionarJogador(nome, numero, isGoleiro)
                showAdicionarDialog = false
            }
        )
    }

    jogadorParaEditar?.let { jogador ->
        EditarJogadorDialog(
            jogador = jogador,
            numerosEmUso = numerosEmUso,
            onDismiss = { jogadorParaEditar = null },
            onConfirm = { atualizado: Jogador ->
                viewModel.editarJogador(atualizado.id, atualizado.nome, atualizado.numeroCamisa, atualizado.isPosicaoGoleiro)
                jogadorParaEditar = null
            }
        )
    }

    jogadorParaRemover?.let { jogador ->
        ConfirmarRemocaoDialog(
            jogador = jogador,
            onDismiss = { jogadorParaRemover = null },
            onConfirm = {
                viewModel.removerJogador(jogador.id)
                jogadorParaRemover = null
            }
        )
    }

    // endregion
    
    // Alerta de Erro
    erroMensagem?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.limparErro() },
            title = { Text("Atenção") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.limparErro() }) {
                    Text("OK", color = Color(0xFF4B0082), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TotalJogadoresCard(total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Elenco Cadastrado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Total de $total atletas ativos", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
private fun JogadorGerenciarItem(jogador: Jogador, onEditar: () -> Unit, onRemover: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jogador.nome, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFF0EDFF)) {
                    Text(
                        "#${jogador.numeroCamisa}", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                        style = MaterialTheme.typography.labelLarge, 
                        color = Color(0xFF4B0082), 
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onEditar, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, "Editar", tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onRemover, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, "Remover", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyJogadoresState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nenhum jogador cadastrado", style = MaterialTheme.typography.titleMedium)
        Text("Adicione atletas para começar a sessão", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
private fun ConfirmarRemocaoDialog(jogador: Jogador, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remover Jogador") },
        text = { Text("Deseja realmente remover ${jogador.nome}? Ele não aparecerá mais nas listas de presença.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Remover") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    )
}
