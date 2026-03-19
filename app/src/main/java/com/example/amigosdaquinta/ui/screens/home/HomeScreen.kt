package com.example.amigosdaquinta.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Tela Principal do sistema Amigos da Quinta.
 *
 * Layout Vertical (Retrato):
 * - Lista de Chegada (40% altura)
 * - Card Time Vermelho (30% altura)
 * - Card Time Branco (30% altura)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToGerenciarJogadores: () -> Unit = {},
    onNavigateToJogo: () -> Unit = {}
) {
    val context = LocalContext.current
    val jogadoresBanco by viewModel.jogadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var timeBranco by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var timeVermelho by rememberSaveable { mutableStateOf<List<Jogador>>(emptyList()) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var modoRapido by rememberSaveable { mutableStateOf(false) }

    // Intercepta o botão voltar do sistema
    BackHandler {
        showExitDialog = true
    }

    // Dispara a busca no banco com debounce
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(300) // Aguarda 300ms antes de buscar
            viewModel.buscarPorNome(searchQuery)
        }
    }

    // Lógica de exibição refinada
    val jogadoresExibidos by remember(jogadoresBanco, listaPresenca, searchQuery, modoRapido) {
        derivedStateOf {
            if (modoRapido || searchQuery.isBlank()) {
                // Modo rápido ou sem busca: mostra fila de presença
                listaPresenca.sortedBy { it.second }.map { it.first }
            } else {
                // Modo busca: filtra localmente primeiro, depois usa banco
                val presentesIds = listaPresenca.map { it.first.id }.toSet()
                val todosJogadores = (listaPresenca.map { it.first } + jogadoresBanco).distinctBy { it.id }

                todosJogadores
                    .filter { jogador ->
                        jogador.nome.contains(searchQuery, ignoreCase = true) ||
                                jogador.numeroCamisa.toString().contains(searchQuery)
                    }
                    .sortedWith(
                        compareByDescending<Jogador> { presentesIds.contains(it.id) }
                            .thenBy { it.nome }
                    )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos da Quinta", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (modoRapido) "Modo Rápido" else "Modo Normal",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (modoRapido) Color(0xFF4B0082) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = modoRapido,
                            onCheckedChange = { modoRapido = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4B0082),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            HomeFabMenu(
                expanded = showFabMenu,
                onToggle = { showFabMenu = !showFabMenu },
                onNavigateToHistorico = onNavigateToHistorico,
                onNavigateToGerenciarJogadores = onNavigateToGerenciarJogadores
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        // ✅ LAYOUT CORRETO - Lista (esquerda) + Times (direita empilhados)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =====================================================================
            // COLUNA ESQUERDA: LISTA DE CHEGADA (INTEIRA)
            // =====================================================================
            FilaChegadaCard(
                modifier = Modifier
                    .weight(1f) // 50% da largura
                    .fillMaxHeight(),
                jogadores = jogadoresExibidos,
                jogadoresResultados = jogadoresBanco,
                listaPresenca = listaPresenca,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho,
                isLoading = isLoading,
                searchQuery = searchQuery,
                modoRapido = modoRapido,
                onSearchChange = { searchQuery = it },
                onConfirmarPresenca = { jogador, confirmado ->
                    if (confirmado) {
                        sessaoViewModel.adicionarAListaPresenca(jogador)
                        searchQuery = ""
                    } else {
                        sessaoViewModel.removerDaListaPresenca(jogador.id)
                    }
                },
                onAdicionarAoTime = { jogador, time ->
                    sessaoViewModel.adicionarAListaPresenca(jogador)
                    searchQuery = ""

                    if (time == "BRANCO") {
                        if (timeBranco.size < 11 && !timeBranco.any { it.id == jogador.id } && !timeVermelho.any { it.id == jogador.id }) {
                            if (jogador.isPosicaoGoleiro) {
                                if (!timeBranco.any { it.isPosicaoGoleiro }) {
                                    timeBranco = timeBranco + jogador
                                }
                            } else {
                                if (timeBranco.count { !it.isPosicaoGoleiro } < 10) {
                                    timeBranco = timeBranco + jogador
                                }
                            }
                        }
                    } else {
                        if (timeVermelho.size < 11 && !timeVermelho.any { it.id == jogador.id } && !timeBranco.any { it.id == jogador.id }) {
                            if (jogador.isPosicaoGoleiro) {
                                if (!timeVermelho.any { it.isPosicaoGoleiro }) {
                                    timeVermelho = timeVermelho + jogador
                                }
                            } else {
                                if (timeVermelho.count { !it.isPosicaoGoleiro } < 10) {
                                    timeVermelho = timeVermelho + jogador
                                }
                            }
                        }
                    }
                }
            )

            // =====================================================================
            // COLUNA DIREITA: TIMES (EMPILHADOS COM ESPAÇAMENTO)
            // =====================================================================
            Column(
                modifier = Modifier
                    .weight(1f) // 50% da largura
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ ESPAÇO ENTRE OS CARDS
            ) {
                // TIME VERMELHO (metade superior)
                TimeCardEscalacao(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // 50% da altura
                    titulo = "Time Vermelho",
                    corFundo = Color(0xFFFFE1E1),
                    jogadores = timeVermelho,
                    onRemover = { jogador ->
                        timeVermelho = timeVermelho.filter { it.id != jogador.id }
                    }
                )

                // TIME BRANCO (metade inferior)
                TimeCardEscalacao(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // 50% da altura
                    titulo = "Time Branco",
                    corFundo = Color(0xFFE8E2FF),
                    jogadores = timeBranco,
                    onRemover = { jogador ->
                        timeBranco = timeBranco.filter { it.id != jogador.id }
                    }
                )

                // BOTÃO INICIAR SESSÃO
                if (timeBranco.size == 11 && timeVermelho.size == 11) {
                    Button(
                        onClick = {
                            val todosJogadores = (timeBranco + timeVermelho).distinct()
                            todosJogadores.forEach { sessaoViewModel.adicionarAListaPresenca(it) }
                            sessaoViewModel.criarJogo(timeBranco, timeVermelho)
                            onNavigateToJogo()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
                    ) {
                        Text(
                            "INICIAR SESSÃO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Diálogo de Confirmação de Saída
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Sair do Programa", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente fechar o Amigos da Quinta?") },
            confirmButton = {
                Button(
                    onClick = { (context as? android.app.Activity)?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SAIR", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}

/**
 * Card da Fila de Chegada com busca e seleção.
 *
 * @param modifier Modificador com altura definida (weight)
 */
@Composable
private fun FilaChegadaCard(
    modifier: Modifier = Modifier,
    jogadores: List<Jogador>,
    jogadoresResultados: List<Jogador>,
    listaPresenca: List<Pair<Jogador, Long>>,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    isLoading: Boolean,
    searchQuery: String,
    modoRapido: Boolean,
    onSearchChange: (String) -> Unit,
    onConfirmarPresenca: (Jogador, Boolean) -> Unit,
    onAdicionarAoTime: (Jogador, String) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFEBE8EC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Título
            Text(
                "Lista de Chegada",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de pesquisa
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar jogador...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Row {
                        // ✅ SETA → para adicionar à lista rapidamente
                        if (searchQuery.isNotEmpty() && jogadoresResultados.isNotEmpty()) {
                            val primeiroJogador = jogadoresResultados.firstOrNull()
                            if (primeiroJogador != null) {
                                IconButton(
                                    onClick = {
                                        onConfirmarPresenca(primeiroJogador, true)
                                        onSearchChange("")
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        "Adicionar à lista",
                                        tint = Color(0xFF4B0082)
                                    )
                                }
                            }
                        }

                        // Botão X para limpar
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, "Limpar")
                            }
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de jogadores
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = jogadores,
                        key = { it.id }
                    ) { jogador ->
                        val jaConfirmado = listaPresenca.any { it.first.id == jogador.id }
                        val ordem = if (jaConfirmado) {
                            listaPresenca.indexOfFirst { it.first.id == jogador.id } + 1
                        } else {
                            null
                        }
                        val noTime = timeBranco.any { it.id == jogador.id } ||
                                timeVermelho.any { it.id == jogador.id }

                        ItemFilaChegada(
                            jogador = jogador,
                            confirmado = jaConfirmado,
                            noTime = noTime,
                            ordem = ordem,
                            modoRapido = modoRapido,
                            timeBranco = timeBranco,
                            timeVermelho = timeVermelho,
                            onToggle = { onConfirmarPresenca(jogador, it) },
                            onAdicionarBranco = { onAdicionarAoTime(jogador, "BRANCO") },
                            onAdicionarVermelho = { onAdicionarAoTime(jogador, "VERMELHO") }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Item individual da fila de chegada.
 */
@Composable
private fun ItemFilaChegada(
    jogador: Jogador,
    confirmado: Boolean,
    noTime: Boolean,
    ordem: Int?,
    modoRapido: Boolean,
    timeBranco: List<Jogador>,
    timeVermelho: List<Jogador>,
    onToggle: (Boolean) -> Unit,
    onAdicionarBranco: () -> Unit,
    onAdicionarVermelho: () -> Unit
) {
    // Lógica de desabilitação dos botões
    val temGoleiroBranco = timeBranco.any { it.isPosicaoGoleiro }
    val temGoleiroVermelho = timeVermelho.any { it.isPosicaoGoleiro }
    val linhasBranco = timeBranco.count { !it.isPosicaoGoleiro }
    val linhasVermelho = timeVermelho.count { !it.isPosicaoGoleiro }

    val podeAdicionarBranco = if (jogador.isPosicaoGoleiro) {
        !temGoleiroBranco && timeBranco.size < 11
    } else {
        linhasBranco < 10 && timeBranco.size < 11
    }

    val podeAdicionarVermelho = if (jogador.isPosicaoGoleiro) {
        !temGoleiroVermelho && timeVermelho.size < 11
    } else {
        linhasVermelho < 10 && timeVermelho.size < 11
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = confirmado,
                onCheckedChange = onToggle
            )

            // Ordem de chegada
            Text(
                text = if (confirmado) "${ordem}°" else "-",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(30.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = if (confirmado) Color(0xFF4B0082) else Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Info do jogador
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    jogador.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    if (jogador.isPosicaoGoleiro) "Goleiro" else "Linha",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Badge número
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color(0xFFF0EDFF)
            ) {
                Text(
                    "#${jogador.numeroCamisa}",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4B0082),
                    fontWeight = FontWeight.Bold
                )
            }

            // ✅ SEPARADOR | e BOTÕES V/B - SEMPRE VISÍVEIS
            Spacer(modifier = Modifier.width(8.dp))

            // Separador |
            Text(
                "|",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botão V (Vermelho)
            Text(
                text = "V",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (podeAdicionarVermelho && confirmado && !noTime) Color.Red else Color.LightGray,
                modifier = Modifier.clickable(
                    enabled = podeAdicionarVermelho && confirmado && !noTime,
                    onClick = onAdicionarVermelho
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Botão B (Branco)
            Text(
                text = "B",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (podeAdicionarBranco && confirmado && !noTime) Color(0xFF4B0082) else Color.LightGray,
                modifier = Modifier.clickable(
                    enabled = podeAdicionarBranco && confirmado && !noTime,
                    onClick = onAdicionarBranco
                )
            )
        }
    }
}

/**
 * Card de escalação de time.
 *
 * @param modifier Modificador com altura definida (weight)
 */
@Composable
private fun TimeCardEscalacao(
    modifier: Modifier = Modifier,
    titulo: String,
    corFundo: Color,
    jogadores: List<Jogador>,
    onRemover: (Jogador) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = corFundo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${jogadores.size}/11",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de jogadores
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(jogadores) { jogador ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (jogador.isPosicaoGoleiro) "[GOL] ${jogador.nome}" else jogador.nome,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { onRemover(jogador) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Menu FAB com opções de Histórico e Gerenciar Jogadores.
 */
@Composable
private fun HomeFabMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToHistorico: () -> Unit,
    onNavigateToGerenciarJogadores: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(visible = expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabMenuItem("Histórico", Icons.Default.History, onNavigateToHistorico)
                FabMenuItem("Jogadores", Icons.Default.People, onNavigateToGerenciarJogadores)
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = Color(0xFF4B0082),
            contentColor = Color.White
        ) {
            Icon(
                if (expanded) Icons.Default.Close else Icons.Default.Menu,
                contentDescription = null
            )
        }
    }
}

/**
 * Item do menu FAB.
 */
@Composable
private fun FabMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = Color.White,
            shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = Color.White
        ) {
            Icon(icon, contentDescription = null)
        }
    }
}