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
import com.example.amigosdaquinta.ui.screens.jogo.JogoScreen
import com.example.amigosdaquinta.ui.screens.jogo.ResultadoScreen
import com.example.amigosdaquinta.ui.screens.presenca.PresencaScreen
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Define todas as rotas de navegacao do app e o mapeamento entre rota e Composable.
 *
 * O estado compartilhado entre telas (times, placar, vencedor) e lido diretamente
 * dos ViewModels aqui, evitando passagem de dados via argumentos de rota para objetos complexos.
 * Apenas tipos primitivos (Long, String) sao trafegados como argumentos de rota.
 *
 * Fluxo principal de navegacao:
 * Home -> Presenca -> FormacaoManual (ou FormacaoAutomatica se 33+) -> Jogo -> Resultado
 *                                                      ^                            |
 *                                                      |______ (proximo jogo) _____/
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
    val duracao by sessaoViewModel.duracaoAtual.collectAsState()
    val placarBranco by sessaoViewModel.placarBranco.collectAsState()
    val placarVermelho by sessaoViewModel.placarVermelho.collectAsState()
    val vencedorUltimo by sessaoViewModel.vencedorUltimoJogo.collectAsState()
    val jogadoresTimeGanhador by sessaoViewModel.jogadoresUltimoTimeGanhador.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = jogadoresViewModel,
                onNavigateToPresenca = { navController.navigate(Screen.Presenca.route) },
                onNavigateToHistorico = { navController.navigate(Screen.Historico.route) },
                onNavigateToDebug = { navController.navigate(Screen.Debug.route) }
            )
        }

        composable(Screen.Presenca.route) {
            PresencaScreen(
                jogadoresViewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onFormarTimes = { navController.navigate(Screen.FormacaoManual.route) }
            )
        }

        /**
         * Ponto de decisao do primeiro jogo:
         * - 33+ jogadores: encaminha para formacao automatica com rotacao total.
         * - Menos de 33: formacao manual pelo usuario.
         */
        composable(Screen.FormacaoManual.route) {
            if (listaPresenca.size >= 33) {
                FormacaoAutomaticaScreen(
                    timeGanhador = null,
                    jogadoresTimeGanhador = emptyList(),
                    filaEspera = listaPresenca,
                    sessaoViewModel = sessaoViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onIniciarJogo = {
                        navController.navigate(Screen.Jogo.route) {
                            popUpTo(Screen.Presenca.route)
                        }
                    }
                )
            } else {
                FormacaoManualScreen(
                    jogadoresPresentes = listaPresenca,
                    sessaoViewModel = sessaoViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onIniciarJogo = {
                        navController.navigate(Screen.Jogo.route) {
                            popUpTo(Screen.Presenca.route)
                        }
                    }
                )
            }
        }

        composable(Screen.FormacaoAutomatica.route) {
            FormacaoAutomaticaScreen(
                timeGanhador = vencedorUltimo,
                jogadoresTimeGanhador = jogadoresTimeGanhador,
                filaEspera = listaPresenca,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onIniciarJogo = {
                    navController.navigate(Screen.Jogo.route) {
                        popUpTo(Screen.Presenca.route)
                    }
                }
            )
        }

        composable(Screen.Jogo.route) {
            JogoScreen(
                sessaoViewModel = sessaoViewModel,
                duracaoMinutos = duracao,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho,
                onNavigateBack = { navController.popBackStack() },
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

        composable(Screen.Resultado.route) { backStackEntry ->
            val vencedor = when (backStackEntry.arguments?.getString("vencedor")) {
                "branco" -> TimeColor.BRANCO
                "vermelho" -> TimeColor.VERMELHO
                else -> null
            }

            ResultadoScreen(
                vencedor = vencedor,
                placarBranco = placarBranco,
                placarVermelho = placarVermelho,
                duracaoMinutos = duracao,
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

        composable(Screen.Historico.route) {
            HistoricoScreen(
                viewModel = historicoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onJogoClick = { jogoId ->
                    navController.navigate(Screen.DetalhesJogo.createRoute(jogoId))
                }
            )
        }

        composable(
            route = Screen.DetalhesJogo.route,
            arguments = listOf(navArgument("jogoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val jogoId = backStackEntry.arguments?.getLong("jogoId") ?: 0L
            DetalhesJogoScreen(
                jogoId = jogoId,
                viewModel = historicoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EstatisticasJogador.route,
            arguments = listOf(navArgument("jogadorId") { type = NavType.LongType })
        ) { backStackEntry ->
            val jogadorId = backStackEntry.arguments?.getLong("jogadorId") ?: 0L
            EstatisticasJogadorScreen(
                jogadorId = jogadorId,
                viewModel = historicoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Debug.route) {
            DebugScreen(
                jogadoresViewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}