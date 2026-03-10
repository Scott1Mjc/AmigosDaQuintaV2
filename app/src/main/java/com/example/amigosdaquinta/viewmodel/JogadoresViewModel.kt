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

                // Goleiros (10)
                Jogador(nome = "Adriano", numeroCamisa = 1, isPosicaoGoleiro = true),
                Jogador(nome = "Fabrício", numeroCamisa = 12, isPosicaoGoleiro = true),
                Jogador(nome = "Leandro", numeroCamisa = 22, isPosicaoGoleiro = true),
                Jogador(nome = "Sérgio", numeroCamisa = 33, isPosicaoGoleiro = true),
                Jogador(nome = "Cláudio", numeroCamisa = 40, isPosicaoGoleiro = true),
                Jogador(nome = "Robson", numeroCamisa = 45, isPosicaoGoleiro = true),
                Jogador(nome = "Maurício", numeroCamisa = 50, isPosicaoGoleiro = true),
                Jogador(nome = "Rogério", numeroCamisa = 56, isPosicaoGoleiro = true),
                Jogador(nome = "Edson", numeroCamisa = 61, isPosicaoGoleiro = true),
                Jogador(nome = "Valter", numeroCamisa = 70, isPosicaoGoleiro = true),

                // Jogadores de linha (130)
                Jogador(nome = "Bruno", numeroCamisa = 2, isPosicaoGoleiro = false),
                Jogador(nome = "Caio", numeroCamisa = 3, isPosicaoGoleiro = false),
                Jogador(nome = "Diego", numeroCamisa = 4, isPosicaoGoleiro = false),
                Jogador(nome = "Eduardo", numeroCamisa = 5, isPosicaoGoleiro = false),
                Jogador(nome = "Felipe", numeroCamisa = 6, isPosicaoGoleiro = false),
                Jogador(nome = "Gabriel", numeroCamisa = 7, isPosicaoGoleiro = false),
                Jogador(nome = "Henrique", numeroCamisa = 8, isPosicaoGoleiro = false),
                Jogador(nome = "Igor", numeroCamisa = 9, isPosicaoGoleiro = false),
                Jogador(nome = "João", numeroCamisa = 10, isPosicaoGoleiro = false),
                Jogador(nome = "Kevin", numeroCamisa = 11, isPosicaoGoleiro = false),
                Jogador(nome = "Lucas", numeroCamisa = 13, isPosicaoGoleiro = false),
                Jogador(nome = "Matheus", numeroCamisa = 14, isPosicaoGoleiro = false),
                Jogador(nome = "Nathan", numeroCamisa = 15, isPosicaoGoleiro = false),
                Jogador(nome = "Otávio", numeroCamisa = 16, isPosicaoGoleiro = false),
                Jogador(nome = "Paulo", numeroCamisa = 17, isPosicaoGoleiro = false),
                Jogador(nome = "Rafael", numeroCamisa = 18, isPosicaoGoleiro = false),
                Jogador(nome = "Samuel", numeroCamisa = 19, isPosicaoGoleiro = false),
                Jogador(nome = "Thiago", numeroCamisa = 20, isPosicaoGoleiro = false),
                Jogador(nome = "Vinicius", numeroCamisa = 21, isPosicaoGoleiro = false),
                Jogador(nome = "Wesley", numeroCamisa = 23, isPosicaoGoleiro = false),
                Jogador(nome = "Yuri", numeroCamisa = 24, isPosicaoGoleiro = false),
                Jogador(nome = "André", numeroCamisa = 25, isPosicaoGoleiro = false),
                Jogador(nome = "Carlos", numeroCamisa = 26, isPosicaoGoleiro = false),
                Jogador(nome = "Douglas", numeroCamisa = 27, isPosicaoGoleiro = false),
                Jogador(nome = "Elton", numeroCamisa = 28, isPosicaoGoleiro = false),
                Jogador(nome = "Fábio", numeroCamisa = 29, isPosicaoGoleiro = false),
                Jogador(nome = "Gustavo", numeroCamisa = 30, isPosicaoGoleiro = false),
                Jogador(nome = "Hugo", numeroCamisa = 31, isPosicaoGoleiro = false),
                Jogador(nome = "Ivan", numeroCamisa = 32, isPosicaoGoleiro = false),
                Jogador(nome = "Jeferson", numeroCamisa = 34, isPosicaoGoleiro = false),
                Jogador(nome = "Kleber", numeroCamisa = 35, isPosicaoGoleiro = false),
                Jogador(nome = "Luan", numeroCamisa = 36, isPosicaoGoleiro = false),
                Jogador(nome = "Marcos", numeroCamisa = 37, isPosicaoGoleiro = false),
                Jogador(nome = "Nicolas", numeroCamisa = 38, isPosicaoGoleiro = false),
                Jogador(nome = "Orlando", numeroCamisa = 39, isPosicaoGoleiro = false),
                Jogador(nome = "Patrick", numeroCamisa = 41, isPosicaoGoleiro = false),
                Jogador(nome = "Renato", numeroCamisa = 42, isPosicaoGoleiro = false),
                Jogador(nome = "Rodrigo", numeroCamisa = 43, isPosicaoGoleiro = false),
                Jogador(nome = "Sandro", numeroCamisa = 44, isPosicaoGoleiro = false),
                Jogador(nome = "Tales", numeroCamisa = 46, isPosicaoGoleiro = false),
                Jogador(nome = "Ubiratan", numeroCamisa = 47, isPosicaoGoleiro = false),
                Jogador(nome = "Vitor", numeroCamisa = 48, isPosicaoGoleiro = false),
                Jogador(nome = "Wallace", numeroCamisa = 49, isPosicaoGoleiro = false),

                // continuação até ~140
                Jogador(nome = "Alex", numeroCamisa = 51, isPosicaoGoleiro = false),
                Jogador(nome = "Breno", numeroCamisa = 52, isPosicaoGoleiro = false),
                Jogador(nome = "Cristian", numeroCamisa = 53, isPosicaoGoleiro = false),
                Jogador(nome = "Davi", numeroCamisa = 54, isPosicaoGoleiro = false),
                Jogador(nome = "Erick", numeroCamisa = 55, isPosicaoGoleiro = false),
                Jogador(nome = "Flávio", numeroCamisa = 57, isPosicaoGoleiro = false),
                Jogador(nome = "Gilberto", numeroCamisa = 58, isPosicaoGoleiro = false),
                Jogador(nome = "Heitor", numeroCamisa = 59, isPosicaoGoleiro = false),
                Jogador(nome = "Isaque", numeroCamisa = 60, isPosicaoGoleiro = false),
                Jogador(nome = "Jonas", numeroCamisa = 62, isPosicaoGoleiro = false),
                Jogador(nome = "Kaio", numeroCamisa = 63, isPosicaoGoleiro = false),
                Jogador(nome = "Luiz", numeroCamisa = 64, isPosicaoGoleiro = false),
                Jogador(nome = "Miguel", numeroCamisa = 65, isPosicaoGoleiro = false),
                Jogador(nome = "Noel", numeroCamisa = 66, isPosicaoGoleiro = false),
                Jogador(nome = "Osvaldo", numeroCamisa = 67, isPosicaoGoleiro = false),
                Jogador(nome = "Pablo", numeroCamisa = 68, isPosicaoGoleiro = false),
                Jogador(nome = "Quirino", numeroCamisa = 69, isPosicaoGoleiro = false),
                Jogador(nome = "Raul", numeroCamisa = 71, isPosicaoGoleiro = false),
                Jogador(nome = "Saulo", numeroCamisa = 72, isPosicaoGoleiro = false),
                Jogador(nome = "Túlio", numeroCamisa = 73, isPosicaoGoleiro = false),
                Jogador(nome = "Ulisses", numeroCamisa = 74, isPosicaoGoleiro = false),
                Jogador(nome = "Valmir", numeroCamisa = 75, isPosicaoGoleiro = false),
                Jogador(nome = "Willian", numeroCamisa = 76, isPosicaoGoleiro = false),
                Jogador(nome = "Xavier", numeroCamisa = 77, isPosicaoGoleiro = false),
                Jogador(nome = "Yago", numeroCamisa = 78, isPosicaoGoleiro = false),
                Jogador(nome = "Zeca", numeroCamisa = 79, isPosicaoGoleiro = false),

                Jogador(nome = "Alisson", numeroCamisa = 80, isPosicaoGoleiro = false),
                Jogador(nome = "Bernardo", numeroCamisa = 81, isPosicaoGoleiro = false),
                Jogador(nome = "César", numeroCamisa = 82, isPosicaoGoleiro = false),
                Jogador(nome = "Danilo", numeroCamisa = 83, isPosicaoGoleiro = false),
                Jogador(nome = "Elias", numeroCamisa = 84, isPosicaoGoleiro = false),
                Jogador(nome = "Francisco", numeroCamisa = 85, isPosicaoGoleiro = false),
                Jogador(nome = "Geovane", numeroCamisa = 86, isPosicaoGoleiro = false),
                Jogador(nome = "Hélio", numeroCamisa = 87, isPosicaoGoleiro = false),
                Jogador(nome = "Italo", numeroCamisa = 88, isPosicaoGoleiro = false),
                Jogador(nome = "Jean", numeroCamisa = 89, isPosicaoGoleiro = false),
                Jogador(nome = "Kelvin", numeroCamisa = 90, isPosicaoGoleiro = false),
                Jogador(nome = "Léo", numeroCamisa = 91, isPosicaoGoleiro = false),
                Jogador(nome = "Moises", numeroCamisa = 92, isPosicaoGoleiro = false),
                Jogador(nome = "Natan", numeroCamisa = 93, isPosicaoGoleiro = false),
                Jogador(nome = "Orion", numeroCamisa = 94, isPosicaoGoleiro = false),
                Jogador(nome = "Pietro", numeroCamisa = 95, isPosicaoGoleiro = false),
                Jogador(nome = "Ramon", numeroCamisa = 96, isPosicaoGoleiro = false),
                Jogador(nome = "Silvio", numeroCamisa = 97, isPosicaoGoleiro = false),
                Jogador(nome = "Tomás", numeroCamisa = 98, isPosicaoGoleiro = false),
                Jogador(nome = "Victor Hugo", numeroCamisa = 99, isPosicaoGoleiro = false)

            )

            jogadoresTeste.forEach { jogador ->
                jogadorRepository.inserir(jogador)
            }

            Log.d(TAG, "140 jogadores de teste adicionados ao banco de dados")
        }
    }

    companion object {
        private const val TAG = "JogadoresViewModel"
    }
}