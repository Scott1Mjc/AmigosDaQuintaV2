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
 * ViewModel responsável pela gestão do cadastro e listagem geral de jogadores.
 *
 * Cuida das operações de inclusão, edição, busca e inativação de atletas,
 * além de garantir a população inicial do banco de dados em novas instalações.
 *
 * @property jogadorRepository Repositório para operações de persistência de jogadores.
 */
class JogadoresViewModel(
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    private val _jogadores = MutableStateFlow<List<Jogador>>(emptyList())
    /** Lista de jogadores ativos exibida na interface. */
    val jogadores: StateFlow<List<Jogador>> = _jogadores.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** Estado que indica se há uma operação de carregamento em curso. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        carregarJogadores()
        verificarEPopularBanco()
    }

    /**
     * Verifica se o banco de dados está vazio e realiza a carga inicial silenciosa.
     */
    private fun verificarEPopularBanco() {
        viewModelScope.launch {
            try {
                val count = jogadorRepository.contarAtivos()
                if (count == 0) {
                    popularBancoComJogadoresIniciais()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro na verificação inicial: ${e.message}")
            }
        }
    }

    /**
     * Observa a lista de jogadores ativos do repositório.
     */
    private fun carregarJogadores() {
        viewModelScope.launch {
            _isLoading.value = true
            jogadorRepository.obterTodosAtivos().collect { lista ->
                _jogadores.value = lista
                _isLoading.value = false
            }
        }
    }

    /**
     * Filtra a lista de jogadores ativos pelo nome.
     */
    fun buscarPorNome(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                jogadorRepository.obterTodosAtivos().take(1).collect { _jogadores.value = it }
            } else {
                jogadorRepository.buscarPorNome(query).collect { _jogadores.value = it }
            }
        }
    }

    /**
     * Insere a lista pré-definida de jogadores iniciais no banco.
     */
    private fun popularBancoComJogadoresIniciais() {
        viewModelScope.launch {
            try {
                JogadoresIniciais.lista.forEach { jogador ->
                    jogadorRepository.inserir(jogador)
                }
                Log.d(TAG, "População inicial concluída com sucesso.")
            } catch (e: Exception) {
                Log.e(TAG, "Falha na carga inicial: ${e.message}")
            }
        }
    }

    /**
     * Adiciona um novo jogador ao sistema.
     */
    fun adicionarJogador(nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            val novoJogador = Jogador(nome = nome.trim(), numeroCamisa = numero, isPosicaoGoleiro = isGoleiro)
            jogadorRepository.inserir(novoJogador)
        }
    }

    /**
     * Atualiza os dados de um jogador existente.
     */
    fun editarJogador(id: Long, nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            val jogador = jogadorRepository.obterPorId(id) ?: return@launch
            jogadorRepository.atualizar(jogador.copy(nome = nome.trim(), numeroCamisa = numero, isPosicaoGoleiro = isGoleiro))
        }
    }

    /**
     * Realiza a inativação lógica de um jogador.
     */
    fun removerJogador(id: Long) {
        viewModelScope.launch {
            jogadorRepository.marcarComoInativo(id)
        }
    }

    companion object {
        private const val TAG = "JogadoresViewModel"
    }
}
