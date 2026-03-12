package com.example.amigosdaquinta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.amigosdaquinta.data.local.AppDatabase
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository
import com.example.amigosdaquinta.ui.navigation.NavGraph
import com.example.amigosdaquinta.ui.theme.AmigosDaQuintaTheme
import com.example.amigosdaquinta.viewmodel.AppViewModelFactory
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Ponto de entrada principal do aplicativo Amigos da Quinta.
 * 
 * Responsável pela inicialização da infraestrutura do app:
 * - Banco de Dados (Room)
 * - Repositórios
 * - ViewModels (via Injeção de Dependência manual com Factory)
 * - Navegação e Tema
 */
class MainActivity : ComponentActivity() {

    // Inicialização tardia dos componentes de dados
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val jogadorRepo by lazy { JogadorRepository(database.jogadorDao()) }
    private val jogoRepo by lazy { JogoRepository(database.jogoDao(), database.participacaoDao()) }
    private val presencaRepo by lazy { PresencaRepository(database.presencaDao()) }
    private val partRepo by lazy { ParticipacaoRepository(database.participacaoDao()) }

    // Provedor de ViewModels
    private val factory by lazy { AppViewModelFactory(jogadorRepo, jogoRepo, partRepo, presencaRepo) }

    // Instâncias únicas de ViewModel para toda a sessão
    private val jogadoresViewModel: JogadoresViewModel by viewModels { factory }
    private val sessaoViewModel: SessaoViewModel by viewModels { factory }
    private val historicoViewModel: HistoricoViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmigosDaQuintaTheme {
                Surface {
                    NavGraph(
                        navController = rememberNavController(),
                        jogadoresViewModel = jogadoresViewModel,
                        sessaoViewModel = sessaoViewModel,
                        historicoViewModel = historicoViewModel
                    )
                }
            }
        }
    }
}
