package com.example.amigosdaquinta.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amigosdaquinta.data.local.JogadoresIniciais
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.repository.JogadorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel responsável pelo gerenciamento de jogadores.
 */
class JogadoresViewModel(
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    private val _jogadores = MutableStateFlow<List<Jogador>>(emptyList())
    val jogadores: StateFlow<List<Jogador>> = _jogadores.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Carrega os jogadores e verifica se precisa popular o banco silenciosamente
        carregarJogadores()
        verificarEPopularBanco()
    }

    /**
     * Verifica se o banco está vazio e popula silenciosamente com os jogadores iniciais.
     */
    private fun verificarEPopularBanco() {
        viewModelScope.launch {
            try {
                // Tenta obter o primeiro valor da lista de ativos para checar se está vazio
                val count = jogadorRepository.contarAtivos()
                if (count == 0) {
                    Log.d(TAG, "Banco vazio detectado no primeiro load. Populando silenciosamente...")
                    popularBancoComJogadoresIniciais()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro na verificação inicial do banco", e)
            }
        }
    }

    private fun carregarJogadores() {
        viewModelScope.launch {
            _isLoading.value = true
            jogadorRepository.obterTodosAtivos().collect { lista ->
                _jogadores.value = lista
                _isLoading.value = false
            }
        }
    }

    fun buscarPorNome(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                // Se a busca for limpa, volta a observar todos os ativos
                jogadorRepository.obterTodosAtivos().take(1).collect { _jogadores.value = it }
            } else {
                jogadorRepository.buscarPorNome(query).collect { _jogadores.value = it }
            }
        }
    }

    /**
     * Popula o banco com os jogadores iniciais sem necessidade de intervenção do usuário.
     */
    fun popularBancoComJogadoresIniciais() {
        viewModelScope.launch {
            try {
                JogadoresIniciais.lista.forEach { jogador ->
                    jogadorRepository.inserir(jogador)
                }
                Log.d(TAG, "População silenciosa concluída: ${JogadoresIniciais.total} jogadores inseridos.")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao popular banco silenciosamente", e)
            }
        }
    }

    fun adicionarJogador(nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            val novoJogador = Jogador(nome = nome.trim(), numeroCamisa = numero, isPosicaoGoleiro = isGoleiro)
            jogadorRepository.inserir(novoJogador)
        }
    }

    fun editarJogador(id: Long, nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            val jogador = jogadorRepository.obterPorId(id) ?: return@launch
            jogadorRepository.atualizar(jogador.copy(nome = nome.trim(), numeroCamisa = numero, isPosicaoGoleiro = isGoleiro))
        }
    }

    fun removerJogador(id: Long) {
        viewModelScope.launch {
            jogadorRepository.marcarComoInativo(id)
        }
    }

    companion object {
        private const val TAG = "JogadoresViewModel"
    }
}