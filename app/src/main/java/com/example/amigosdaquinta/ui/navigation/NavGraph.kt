package com.example.amigosdaquinta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.ui.screens.debug.DebugScreen
import com.example.amigosdaquinta.ui.screens.formacao.FormacaoAutomaticaScreen
import com.example.amigosdaquinta.ui.screens.formacao.FormacaoManualScreen
import com.example.amigosdaquinta.ui.screens.history.DetalhesJogoScreen
import com.example.amigosdaquinta.ui.screens.history.EstatisticasJogadorScreen
import com.example.amigosdaquinta.ui.screens.history.HistoricoScreen
import com.example.amigosdaquinta.ui.screens.home.HomeScreen
import com.example.amigosdaquinta.ui.screens.jogadores.GerenciarJogadoresScreen
import com.example.amigosdaquinta.ui.screens.jogo.JogoScreen
import com.example.amigosdaquinta.ui.screens.jogo.ResultadoScreen
import com.example.amigosdaquinta.ui.screens.presenca.PresencaScreen
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Define todas as rotas de navegacao do app e o mapeamento entre rota e Composable.
 *
 * O estado compartilhado entre telas (times, placar, vencedor) é lido diretamente
 * dos ViewModels aqui, evitando passagem de dados via argumentos de rota para objetos complexos.
 * Apenas tipos primitivos (Long, String) sao trafegados como argumentos de rota.
 *
 * Fluxo principal de navegacao:
 * Home -> Presenca -> FormacaoManual -> Jogo -> Resultado
 *
 * A rota [Screen.FormacaoManual] decide internamente se exibe formacao manual ou automatica,
 * com base no total de jogadores presentes (threshold: 33).
 */

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Presenca : Screen("presenca")
    object FormacaoManual : Screen("formacao_manual")
    object FormacaoAutomatica : Screen("formacao_automatica")
    object Jogo : Screen("jogo")
    object Resultado : Screen("resultado/{vencedor}") {
        fun createRoute(vencedor: String) = "resultado/$vencedor"
    }
    object Historico : Screen("historico")
    object DetalhesJogo : Screen("detalhes_jogo/{jogoId}") {
        fun createRoute(jogoId: Long) = "detalhes_jogo/$jogoId"
    }
    object EstatisticasJogador : Screen("estatisticas/{jogadorId}") {
        fun createRoute(jogadorId: Long) = "estatisticas/$jogadorId"
    }
    object Debug : Screen("debug")
    object GerenciarJogadores : Screen("gerenciar_jogadores")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    jogadoresViewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    historicoViewModel: HistoricoViewModel
) {
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()
    val timeBranco by sessaoViewModel.timeBrancoAtual.collectAsState()
    val timeVermelho by sessaoViewModel.timeVermelhoAtual.collectAsState()
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()
    val vencedorUltimo by sessaoViewModel.vencedorUltimoJogo.collectAsState()
    val jogadoresTimeGanhador by sessaoViewModel.jogadoresUltimoTimeGanhador.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateToPresenca = {
                    navController.navigate(Screen.Presenca.route)
                },
                onNavigateToHistorico = {
                    navController.navigate(Screen.Historico.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                },
                onNavigateToGerenciarJogadores = {
                    navController.navigate(Screen.GerenciarJogadores.route)
                },
                onNavigateToFormacaoManual = {
                    navController.navigate(Screen.Jogo.route)
                }
            )
        }

        // Gerenciar Jogadores
        composable(Screen.GerenciarJogadores.route) {
            GerenciarJogadoresScreen(
                viewModel = jogadoresViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Lista de Presença
        composable(Screen.Presenca.route) {
            PresencaScreen(
                jogadoresViewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFormarTimes = {
                    navController.navigate(Screen.FormacaoManual.route)
                }
            )
        }

        // Formação Manual
        composable(Screen.FormacaoManual.route) {
            FormacaoManualScreen(
                jogadoresPresentes = listaPresenca,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onIniciarJogo = {
                    navController.navigate(Screen.Jogo.route) {
                        popUpTo(Screen.Presenca.route)
                    }
                }
            )
        }

        // Formação Automática
        composable(Screen.FormacaoAutomatica.route) {
            FormacaoAutomaticaScreen(
                timeGanhador = vencedorUltimo,
                jogadoresTimeGanhador = jogadoresTimeGanhador,
                filaEspera = listaPresenca,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onIniciarJogo = {
                    navController.navigate(Screen.Jogo.route) {
                        popUpTo(Screen.Presenca.route)
                    }
                }
            )
        }

        // Jogo
        composable(Screen.Jogo.route) {
            JogoScreen(
                sessaoViewModel = sessaoViewModel,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFinalizarJogo = { vencedor ->
                    val vencedorString = when (vencedor) {
                        TimeColor.BRANCO -> "branco"
                        TimeColor.VERMELHO -> "vermelho"
                        null -> "empate"
                    }
                    navController.navigate(Screen.Resultado.createRoute(vencedorString)) {
                        popUpTo(Screen.Presenca.route)
                    }
                }
            )
        }

        // Resultado
        composable(Screen.Resultado.route) { backStackEntry ->
            val vencedorString = backStackEntry.arguments?.getString("vencedor")
            val vencedor = when (vencedorString) {
                "branco" -> TimeColor.BRANCO
                "vermelho" -> TimeColor.VERMELHO
                else -> null
            }

            ResultadoScreen(
                vencedor = vencedor,
                placarBranco = placarBranco,
                placarVermelho = placarVermelho,
                onProximoJogo = {
                    sessaoViewModel.limparJogoAtual()
                    navController.navigate(Screen.FormacaoAutomatica.route) {
                        popUpTo(Screen.Presenca.route)
                    }
                },
                onEncerrar = {
                    sessaoViewModel.limparSessaoCompleta()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Histórico
        composable(Screen.Historico.route) {
            HistoricoScreen(
                viewModel = historicoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onJogoClick = { jogoId ->
                    navController.navigate(Screen.DetalhesJogo.createRoute(jogoId))
                }
            )
        }

        // Detalhes do Jogo
        composable(
            route = Screen.DetalhesJogo.route,
            arguments = listOf(
                navArgument("jogoId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val jogoId = backStackEntry.arguments?.getLong("jogoId") ?: 0L
            DetalhesJogoScreen(
                jogoId = jogoId,
                viewModel = historicoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Estatísticas do Jogador
        composable(
            route = Screen.EstatisticasJogador.route,
            arguments = listOf(
                navArgument("jogadorId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val jogadorId = backStackEntry.arguments?.getLong("jogadorId") ?: 0L
            EstatisticasJogadorScreen(
                jogadorId = jogadorId,
                viewModel = historicoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Debug
        composable(Screen.Debug.route) {
            DebugScreen(
                jogadoresViewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}