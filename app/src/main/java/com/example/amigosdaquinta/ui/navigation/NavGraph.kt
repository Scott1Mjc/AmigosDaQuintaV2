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
import com.example.amigosdaquinta.ui.screens.formacao.FormacaoAutomaticaScreen
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
    object FormacaoAutomatica : Screen("formacao_automatica")
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
    val listaPresenca by sessaoViewModel.listaPresenca.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateToHistorico = { navController.navigate(Screen.Historico.route) },
                onNavigateToGerenciarJogadores = { navController.navigate(Screen.GerenciarJogadores.route) },
                onNavigateToJogo = { 
                    navController.navigate(Screen.Jogo.route) 
                }
            )
        }

        composable(Screen.GerenciarJogadores.route) {
            GerenciarJogadoresScreen(
                viewModel = jogadoresViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FormacaoAutomatica.route) {
            val timeGanhador = when {
                timeBranco.isNotEmpty() -> TimeColor.BRANCO
                timeVermelho.isNotEmpty() -> TimeColor.VERMELHO
                else -> null
            }
            val jogadoresGanhadores = if (timeGanhador == TimeColor.BRANCO) timeBranco else timeVermelho

            FormacaoAutomaticaScreen(
                timeGanhador = timeGanhador,
                jogadoresTimeGanhador = jogadoresGanhadores,
                filaEspera = listaPresenca,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onIniciarJogo = {
                    navController.navigate(Screen.Jogo.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Jogo.route) {
            JogoScreen(
                sessaoViewModel = sessaoViewModel,
                jogadoresViewModel = jogadoresViewModel,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho,
                onNavigateBack = { navController.popBackStack() },
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
                    navController.navigate(Screen.FormacaoAutomatica.route) {
                        popUpTo(Screen.Home.route)
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

        composable(Screen.Historico.route) {
            HistoricoScreen(
                viewModel = historicoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onJogoClick = { id -> navController.navigate(Screen.DetalhesJogo.createRoute(id)) }
            )
        }

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
