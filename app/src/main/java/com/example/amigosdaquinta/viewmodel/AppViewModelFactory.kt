package com.example.amigosdaquinta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository

/**
 * Factory para criação de ViewModels com dependências.
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

            else -> throw IllegalArgumentException("ViewModel class not found: ${modelClass.name}")
        }
    }
}