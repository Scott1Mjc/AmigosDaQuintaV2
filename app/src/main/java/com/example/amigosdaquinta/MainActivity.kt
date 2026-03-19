package com.example.amigosdaquinta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
 */
class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val jogadorRepo by lazy { JogadorRepository(database.jogadorDao()) }
    private val jogoRepo by lazy { JogoRepository(database.jogoDao(), database.participacaoDao()) }
    private val presencaRepo by lazy { PresencaRepository(database.presencaDao()) }
    private val partRepo by lazy { ParticipacaoRepository(database.participacaoDao()) }

    private val factory by lazy { AppViewModelFactory(jogadorRepo, jogoRepo, partRepo, presencaRepo) }

    private val jogadoresViewModel: JogadoresViewModel by viewModels { factory }
    private val sessaoViewModel: SessaoViewModel by viewModels { factory }
    private val historicoViewModel: HistoricoViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ativa o Edge-to-Edge para tornar o app 100% responsivo em qualquer tela
        enableEdgeToEdge()

        setContent {
            AmigosDaQuintaTheme {
                // Surface preenche toda a tela e aplica padding seguro para evitar recortes em câmeras/notches
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
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
