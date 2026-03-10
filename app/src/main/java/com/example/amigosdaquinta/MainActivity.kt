package com.example.amigosdaquinta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.amigosdaquinta.data.local.AppDatabase
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository
import com.example.amigosdaquinta.ui.navigation.NavGraph
import com.example.amigosdaquinta.viewmodel.AppViewModelFactory
import com.example.amigosdaquinta.viewmodel.HistoricoViewModel
import com.example.amigosdaquinta.viewmodel.JogadoresViewModel
import com.example.amigosdaquinta.viewmodel.SessaoViewModel

/**
 * Unico ponto de entrada da aplicacao.
 *
 * Responsavel por instanciar o banco, os repositories e a factory de ViewModels.
 * Todos os objetos sao lazy para evitar inicializacao desnecessaria antes do onCreate.
 *
 * Os ViewModels sao obtidos via [viewModels] com a [AppViewModelFactory], garantindo
 * que sejam escopados ao ciclo de vida da Activity e sobrevivam a rotacoes de tela.
 *
 * O tema e aplicado diretamente via [MaterialTheme] sem customizacao por enquanto.
 * Para adicionar cores e tipografia proprias, criar um Theme.kt e substituir aqui.
 */
class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }

    private val jogadorRepository by lazy { JogadorRepository(database.jogadorDao()) }
    private val jogoRepository by lazy { JogoRepository(database.jogoDao(), database.participacaoDao()) }
    private val presencaRepository by lazy { PresencaRepository(database.presencaDao()) }
    private val participacaoRepository by lazy { ParticipacaoRepository(database.participacaoDao()) }

    private val factory by lazy {
        AppViewModelFactory(jogadorRepository, jogoRepository, participacaoRepository, presencaRepository)
    }

    private val jogadoresViewModel: JogadoresViewModel by viewModels { factory }
    private val sessaoViewModel: SessaoViewModel by viewModels { factory }
    private val historicoViewModel: HistoricoViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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