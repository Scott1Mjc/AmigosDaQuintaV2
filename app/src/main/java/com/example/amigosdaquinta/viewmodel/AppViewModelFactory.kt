package com.example.amigosdaquinta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository

/**
 * Factory responsavel por instanciar todos os ViewModels do app com suas dependencias.
 *
 * Centraliza a criacao dos ViewModels evitando o uso de construtores sem parametros.
 * Deve ser registrada no [ViewModelProvider] via [androidx.activity.viewModels] ou equivalente.
 *
 * Qualquer novo ViewModel que precise de injecao de dependencias deve ser adicionado aqui.
 * Caso contrario, o sistema lanca [IllegalArgumentException] com o nome da classe nao mapeada.
 */
class AppViewModelFactory(
    private val jogadorRepository: JogadorRepository,
    private val jogoRepository: JogoRepository,
    private val presencaRepository: PresencaRepository,
    private val participacaoRepository: ParticipacaoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(JogadoresViewModel::class.java) ->
                JogadoresViewModel(jogadorRepository) as T

            modelClass.isAssignableFrom(SessaoViewModel::class.java) ->
                SessaoViewModel(jogoRepository, presencaRepository) as T

            modelClass.isAssignableFrom(HistoricoViewModel::class.java) ->
                HistoricoViewModel(jogoRepository, participacaoRepository, jogadorRepository) as T

            else -> throw IllegalArgumentException("ViewModel nao mapeado na factory: ${modelClass.name}")
        }
    }
}