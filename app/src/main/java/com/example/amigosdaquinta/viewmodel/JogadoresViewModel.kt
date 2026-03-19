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
 */
class JogadoresViewModel(
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    private val _jogadores = MutableStateFlow<List<Jogador>>(emptyList())
    val jogadores: StateFlow<List<Jogador>> = _jogadores.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _erroMensagem = MutableStateFlow<String?>(null)
    val erroMensagem: StateFlow<String?> = _erroMensagem.asStateFlow()

    init {
        carregarJogadores()
        verificarEPopularBanco()
    }

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
     * Filtra a lista de jogadores ativos pelo nome ou número.
     */
    fun buscarPorNome(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                // Ao limpar a busca, recarrega todos os ativos (limitamos a 1 emissão para evitar loops)
                jogadorRepository.obterTodosAtivos().take(1).collect { _jogadores.value = it }
            } else {
                // Usa a nova busca híbrida (Nome + Número)
                jogadorRepository.buscarPorNome(query).collect { _jogadores.value = it }
            }
        }
    }

    private fun popularBancoComJogadoresIniciais() {
        viewModelScope.launch {
            try {
                JogadoresIniciais.lista.forEach { jogador ->
                    jogadorRepository.inserir(jogador)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha na carga inicial: ${e.message}")
            }
        }
    }

    fun adicionarJogador(nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            val existente = jogadorRepository.obterPorNumeroCamisa(numero)
            if (existente != null) {
                _erroMensagem.value = "Já existe um jogador com o número #$numero (${existente.nome})"
                return@launch
            }

            val novoJogador = Jogador(nome = nome.trim(), numeroCamisa = numero, isPosicaoGoleiro = isGoleiro)
            jogadorRepository.inserir(novoJogador)
            _erroMensagem.value = null
        }
    }

    fun editarJogador(id: Long, nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            val existenteComNumero = jogadorRepository.obterPorNumeroCamisa(numero)
            if (existenteComNumero != null && existenteComNumero.id != id) {
                _erroMensagem.value = "O número #$numero já está em uso por ${existenteComNumero.nome}"
                return@launch
            }

            val jogador = jogadorRepository.obterPorId(id) ?: return@launch
            jogadorRepository.atualizar(jogador.copy(nome = nome.trim(), numeroCamisa = numero, isPosicaoGoleiro = isGoleiro))
            _erroMensagem.value = null
        }
    }

    fun limparErro() {
        _erroMensagem.value = null
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
