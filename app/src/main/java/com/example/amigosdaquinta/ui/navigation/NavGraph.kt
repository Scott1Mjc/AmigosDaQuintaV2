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
import com.example.amigosdaquinta.ui.screens.jogo.JogadoresSairamDurantePartidaScreen
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
    object JogadoresSairam : Screen("jogadores_sairam")
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
                onNavigateToHistorico = { 
                    if (navController.currentDestination?.route == Screen.Home.route) {
                        navController.navigate(Screen.Historico.route)
                    }
                },
                onNavigateToGerenciarJogadores = { 
                    if (navController.currentDestination?.route == Screen.Home.route) {
                        navController.navigate(Screen.GerenciarJogadores.route)
                    }
                },
                onNavigateToJogo = { 
                    if (navController.currentDestination?.route == Screen.Home.route) {
                        navController.navigate(Screen.Jogo.route) 
                    }
                },
                onNavigateToFormacao = {
                    if (navController.currentDestination?.route == Screen.Home.route) {
                        navController.navigate(Screen.FormacaoAutomatica.route)
                    }
                }
            )
        }

        composable(Screen.GerenciarJogadores.route) {
            GerenciarJogadoresScreen(
                viewModel = jogadoresViewModel,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = { 
                    if (navController.currentDestination?.route == Screen.GerenciarJogadores.route) {
                        navController.popBackStack(Screen.Home.route, false)
                    }
                }
            )
        }

        composable(Screen.FormacaoAutomatica.route) {
            val numJogo by sessaoViewModel.numeroDoProximoJogo.collectAsState()
            
            // Lógica para determinar quem é o "ganhador" (ou time que fica) para o preview
            val timeGanhador = if (numJogo == 1) {
                // No 1º jogo, passamos Branco como referência para mostrar ambos os times formados manualmente
                TimeColor.BRANCO 
            } else {
                when {
                    timeBranco.isNotEmpty() -> TimeColor.BRANCO
                    timeVermelho.isNotEmpty() -> TimeColor.VERMELHO
                    else -> null
                }
            }
            
            val jogadoresGanhadores = if (timeGanhador == TimeColor.BRANCO) timeBranco else timeVermelho

            FormacaoAutomaticaScreen(
                timeGanhador = if (numJogo == 1) null else timeGanhador,
                jogadoresTimeGanhador = jogadoresGanhadores,
                filaEspera = listaPresenca,
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = {
                    if (navController.currentDestination?.route == Screen.FormacaoAutomatica.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                onIniciarJogo = {
                    if (navController.currentDestination?.route == Screen.FormacaoAutomatica.route) {
                        navController.navigate(Screen.Jogo.route) {
                            popUpTo(Screen.Home.route)
                        }
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
                onNavigateBack = { 
                    if (navController.currentDestination?.route == Screen.Jogo.route) {
                        navController.popBackStack()
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onFinalizarJogo = { vencedor ->
                    val vStr = when (vencedor) {
                        TimeColor.BRANCO -> "branco"
                        TimeColor.VERMELHO -> "vermelho"
                        null -> "empate"
                    }
                    if (navController.currentDestination?.route == Screen.Jogo.route) {
                        navController.navigate(Screen.Resultado.createRoute(vStr)) {
                            popUpTo(Screen.Home.route)
                        }
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
                    if (navController.currentDestination?.route?.startsWith("resultado") == true) {
                        sessaoViewModel.prepararProximaPartida(vencedor)
                        navController.navigate(Screen.FormacaoAutomatica.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onJogadoresSairam = {
                    navController.navigate(Screen.JogadoresSairam.route)
                },
                onEncerrar = {
                    if (navController.currentDestination?.route?.startsWith("resultado") == true) {
                        sessaoViewModel.iniciarNovoDia()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.JogadoresSairam.route) {
            JogadoresSairamDurantePartidaScreen(
                sessaoViewModel = sessaoViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Historico.route) {
            HistoricoScreen(
                viewModel = historicoViewModel,
                onNavigateBack = { 
                    if (navController.currentDestination?.route == Screen.Historico.route) {
                        navController.popBackStack(Screen.Home.route, false)
                    }
                },
                onJogoClick = { id -> 
                    if (navController.currentDestination?.route == Screen.Historico.route) {
                        navController.navigate(Screen.DetalhesJogo.createRoute(id)) 
                    }
                }
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
                onNavigateBack = { 
                    if (navController.currentDestination?.route?.startsWith("detalhes_jogo") == true) {
                        navController.popBackStack()
                    }
                }
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
                onNavigateBack = { 
                    if (navController.currentDestination?.route?.startsWith("estatisticas") == true) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
