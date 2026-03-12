package com.example.amigosdaquinta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository

/**
 * Fábrica de ViewModels para o aplicativo Amigos da Quinta.
 * 
 * Permite a injeção manual de dependências (repositórios) nos ViewModels,
 * garantindo que as instâncias corretas sejam criadas pelo ViewModelProvider.
 */
@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val jogadorRepository: JogadorRepository,
    private val jogoRepository: JogoRepository,
    private val participacaoRepository: ParticipacaoRepository,
    private val presencaRepository: PresencaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(JogadoresViewModel::class.java) -> {
                JogadoresViewModel(jogadorRepository) as T
            }

            modelClass.isAssignableFrom(SessaoViewModel::class.java) -> {
                SessaoViewModel(
                    jogoRepository = jogoRepository,
                    participacaoRepository = participacaoRepository,
                    presencaRepository = presencaRepository,
                    jogadorRepository = jogadorRepository
                ) as T
            }

            modelClass.isAssignableFrom(HistoricoViewModel::class.java) -> {
                HistoricoViewModel(
                    jogoRepository = jogoRepository,
                    participacaoRepository = participacaoRepository,
                    jogadorRepository = jogadorRepository
                ) as T
            }

            else -> throw IllegalArgumentException("Classe ViewModel não encontrada: ${modelClass.name}")
        }
    }
}
