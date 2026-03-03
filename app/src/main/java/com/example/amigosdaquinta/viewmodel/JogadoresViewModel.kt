package com.example.amigosdaquinta.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.repository.JogadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsável pelo gerenciamento de jogadores.
 *
 * Funcionalidades:
 * - Listagem de jogadores ativos
 * - Adição, edição e remoção de jogadores
 * - Busca por nome
 * - Geração de dados de teste (40 jogadores)
 *
 * @property jogadorRepository Repository para acesso aos dados de jogadores
 */
class JogadoresViewModel(
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    private val _jogadores = MutableStateFlow<List<Jogador>>(emptyList())
    val jogadores: StateFlow<List<Jogador>> = _jogadores.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        carregarJogadores()
    }

    /**
     * Carrega a lista de jogadores ativos do banco de dados.
     * Atualiza automaticamente quando há mudanças.
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
     * Adiciona um novo jogador ao banco de dados.
     * Valida se já existe jogador com o mesmo número de camisa.
     *
     * @param nome Nome do jogador
     * @param numero Número da camisa (1-99)
     * @param isGoleiro True se for goleiro, false se for jogador de linha
     */
    fun adicionarJogador(nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            // Validação: verifica número duplicado
            val jaExiste = _jogadores.value.any { it.numeroCamisa == numero }
            if (jaExiste) {
                Log.w(TAG, "Tentativa de adicionar jogador com número duplicado: $numero")
                return@launch
            }

            val novoJogador = Jogador(
                nome = nome.trim(),
                numeroCamisa = numero,
                isPosicaoGoleiro = isGoleiro
            )
            jogadorRepository.inserir(novoJogador)
            Log.d(TAG, "Jogador adicionado: ${novoJogador.nome} (#${novoJogador.numeroCamisa})")
        }
    }

    /**
     * Edita os dados de um jogador existente.
     *
     * @param id ID do jogador a ser editado
     * @param nome Novo nome
     * @param numero Novo número da camisa
     * @param isGoleiro Nova posição
     */
    fun editarJogador(id: Long, nome: String, numero: Int, isGoleiro: Boolean) {
        viewModelScope.launch {
            val jogador = jogadorRepository.obterPorId(id) ?: run {
                Log.w(TAG, "Jogador não encontrado para edição: ID $id")
                return@launch
            }

            val jogadorAtualizado = jogador.copy(
                nome = nome.trim(),
                numeroCamisa = numero,
                isPosicaoGoleiro = isGoleiro
            )
            jogadorRepository.atualizar(jogadorAtualizado)
            Log.d(TAG, "Jogador atualizado: ${jogadorAtualizado.nome}")
        }
    }

    /**
     * Remove um jogador (marca como inativo).
     * O jogador não é deletado fisicamente, apenas marcado como inativo.
     *
     * @param id ID do jogador a ser removido
     */
    fun removerJogador(id: Long) {
        viewModelScope.launch {
            jogadorRepository.marcarComoInativo(id)
            Log.d(TAG, "Jogador marcado como inativo: ID $id")
        }
    }

    /**
     * Busca jogadores por nome.
     * Atualiza a lista de jogadores com os resultados da busca.
     *
     * @param query Termo de busca
     */
    fun buscarPorNome(query: String) {
        viewModelScope.launch {
            jogadorRepository.buscarPorNome(query).collect { lista ->
                _jogadores.value = lista
            }
        }
    }

    /**
     * Popula o banco com 40 jogadores de teste para debugging.
     * 5 goleiros + 35 jogadores de linha.
     *
     * NOTA: Função destinada apenas para testes e desenvolvimento.
     */
    fun popularBancoComJogadoresDeTeste() {
        viewModelScope.launch {
            val jogadoresTeste = listOf(
                // Goleiros
                Jogador(nome = "Goleiro A", numeroCamisa = 1, isPosicaoGoleiro = true),
                Jogador(nome = "Goleiro B", numeroCamisa = 12, isPosicaoGoleiro = true),
                Jogador(nome = "Goleiro C", numeroCamisa = 23, isPosicaoGoleiro = true),
                Jogador(nome = "Goleiro D", numeroCamisa = 34, isPosicaoGoleiro = true),
                Jogador(nome = "Goleiro E", numeroCamisa = 45, isPosicaoGoleiro = true),

                // Atacantes
                Jogador(nome = "Atacante 1", numeroCamisa = 7, isPosicaoGoleiro = false),
                Jogador(nome = "Atacante 2", numeroCamisa = 9, isPosicaoGoleiro = false),
                Jogador(nome = "Atacante 3", numeroCamisa = 10, isPosicaoGoleiro = false),
                Jogador(nome = "Atacante 4", numeroCamisa = 11, isPosicaoGoleiro = false),
                Jogador(nome = "Atacante 5", numeroCamisa = 17, isPosicaoGoleiro = false),

                // Meias
                Jogador(nome = "Meia 1", numeroCamisa = 8, isPosicaoGoleiro = false),
                Jogador(nome = "Meia 2", numeroCamisa = 14, isPosicaoGoleiro = false),
                Jogador(nome = "Meia 3", numeroCamisa = 16, isPosicaoGoleiro = false),
                Jogador(nome = "Meia 4", numeroCamisa = 18, isPosicaoGoleiro = false),
                Jogador(nome = "Meia 5", numeroCamisa = 20, isPosicaoGoleiro = false),

                // Zagueiros
                Jogador(nome = "Zagueiro 1", numeroCamisa = 2, isPosicaoGoleiro = false),
                Jogador(nome = "Zagueiro 2", numeroCamisa = 3, isPosicaoGoleiro = false),
                Jogador(nome = "Zagueiro 3", numeroCamisa = 4, isPosicaoGoleiro = false),
                Jogador(nome = "Zagueiro 4", numeroCamisa = 5, isPosicaoGoleiro = false),
                Jogador(nome = "Zagueiro 5", numeroCamisa = 15, isPosicaoGoleiro = false),

                // Laterais
                Jogador(nome = "Lateral 1", numeroCamisa = 6, isPosicaoGoleiro = false),
                Jogador(nome = "Lateral 2", numeroCamisa = 13, isPosicaoGoleiro = false),
                Jogador(nome = "Lateral 3", numeroCamisa = 19, isPosicaoGoleiro = false),
                Jogador(nome = "Lateral 4", numeroCamisa = 21, isPosicaoGoleiro = false),

                // Volantes
                Jogador(nome = "Volante 1", numeroCamisa = 22, isPosicaoGoleiro = false),
                Jogador(nome = "Volante 2", numeroCamisa = 24, isPosicaoGoleiro = false),
                Jogador(nome = "Volante 3", numeroCamisa = 25, isPosicaoGoleiro = false),

                // Reservas
                Jogador(nome = "Reserva 1", numeroCamisa = 26, isPosicaoGoleiro = false),
                Jogador(nome = "Reserva 2", numeroCamisa = 27, isPosicaoGoleiro = false),
                Jogador(nome = "Reserva 3", numeroCamisa = 28, isPosicaoGoleiro = false),
                Jogador(nome = "Reserva 4", numeroCamisa = 29, isPosicaoGoleiro = false),
                Jogador(nome = "Reserva 5", numeroCamisa = 30, isPosicaoGoleiro = false),
                Jogador(nome = "Reserva 6", numeroCamisa = 31, isPosicaoGoleiro = false),
                Jogador(nome = "Reserva 7", numeroCamisa = 32, isPosicaoGoleiro = false),
                Jogador(nome = "Reserva 8", numeroCamisa = 33, isPosicaoGoleiro = false)
            )

            jogadoresTeste.forEach { jogador ->
                jogadorRepository.inserir(jogador)
            }

            Log.d(TAG, "40 jogadores de teste adicionados ao banco de dados")
        }
    }

    companion object {
        private const val TAG = "JogadoresViewModel"
    }
}