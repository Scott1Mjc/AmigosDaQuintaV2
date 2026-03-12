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
import com.example.amigosdaquinta.ui.screens.history.DetalhesJogoScreen
import com.example.amigosdaquinta.ui.screens.history.EstatisticasJogadorScreen
import com.example.amigosdaquinta.ui.screens.history.HistoricoScreen
import com.example.amigosdaquinta.ui.screens.home.HomeScreen
import com.example.amigosdaquinta.ui.screens.jogadores.GerenciarJogadoresScreen
import com.example.amigosdaquinta.ui.screens.jogo.JogoScreen
import com.example.amigosdaquinta.ui.screens.jogo.ResultadoScreen
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Definição das rotas de navegação do aplicativo.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
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
    object GerenciarJogadores : Screen("gerenciar_jogadores")
}

/**
 * Grafo de navegação principal.
 * 
 * Centraliza a lógica de transição entre telas e a passagem de parâmetros.
 * Gerencia o estado compartilhado entre as rotas através dos ViewModels.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    jogadoresViewModel: JogadoresViewModel,
    sessaoViewModel: SessaoViewModel,
    historicoViewModel: HistoricoViewModel
) {
    val timeBranco by sessaoViewModel.timeBrancoAtual.collectAsState()
    val timeVermelho by sessaoViewModel.timeVermelhoAtual.collectAsState()
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Tela Inicial
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateToHistorico = { navController.navigate(Screen.Historico.route) },
                onNavigateToGerenciarJogadores = { navController.navigate(Screen.GerenciarJogadores.route) },
                onNavigateToFormacaoManual = { navController.navigate(Screen.Jogo.route) }
            )
        }

        // Gestão de Elenco
        composable(Screen.GerenciarJogadores.route) {
            GerenciarJogadoresScreen(
                viewModel = jogadoresViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Partida em Tempo Real
        composable(Screen.Jogo.route) {
            JogoScreen(
                sessaoViewModel = sessaoViewModel,
                jogadoresViewModel = jogadoresViewModel,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho,
                onFinalizarJogo = { vencedor ->
                    val vStr = when (vencedor) {
                        TimeColor.BRANCO -> "branco"
                        TimeColor.VERMELHO -> "vermelho"
                        null -> "empate"
                    }
                    navController.navigate(Screen.Resultado.createRoute(vStr)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // Tela de Resultado
        composable(Screen.Resultado.route) { backStackEntry ->
            val vStr = backStackEntry.arguments?.getString("vencedor")
            val vencedor = when (vStr) {
                "branco" -> TimeColor.BRANCO
                "vermelho" -> TimeColor.VERMELHO
                else -> null
            }

            ResultadoScreen(
                vencedor = vencedor,
                placarBranco = placarBranco,
                placarVermelho = placarVermelho,
                onProximoJogo = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onEncerrar = {
                    sessaoViewModel.iniciarNovoDia()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Histórico de Partidas
        composable(Screen.Historico.route) {
            HistoricoScreen(
                viewModel = historicoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onJogoClick = { id -> navController.navigate(Screen.DetalhesJogo.createRoute(id)) }
            )
        }

        // Ficha Técnica da Partida
        composable(
            route = Screen.DetalhesJogo.route,
            arguments = listOf(navArgument("jogoId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("jogoId") ?: 0L
            DetalhesJogoScreen(
                jogoId = id,
                viewModel = historicoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Perfil e Estatísticas do Atleta
        composable(
            route = Screen.EstatisticasJogador.route,
            arguments = listOf(navArgument("jogadorId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("jogadorId") ?: 0L
            EstatisticasJogadorScreen(
                jogadorId = id,
                viewModel = historicoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
