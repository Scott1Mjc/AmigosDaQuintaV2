package com.example.amigosdaquinta.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.data.model.JogadorNaFila
import com.example.amigosdaquinta.data.model.SessaoJogos
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsável pelo gerenciamento da sessão de jogos.
 *
 * Funcionalidades principais:
 * - Gerenciar lista de presença
 * - Criar e finalizar jogos
 * - Controlar placar e duração
 * - Gerenciar times e participações
 * - Controlar fluxo entre jogos (vencedor permanece, etc)
 *
 * Regras de negócio implementadas:
 * - 1º jogo: 30 minutos
 * - Demais jogos: 15 minutos
 * - Time vencedor permanece (modo normal)
 * - Rotação forçada (30+ jogadores)
 * - Rotação total (33+ jogadores)
 *
 * @property jogoRepository Repository para gerenciar jogos
 * @property presencaRepository Repository para gerenciar presenças
 */
class SessaoViewModel(
    private val jogoRepository: JogoRepository,
    private val presencaRepository: PresencaRepository
) : ViewModel() {

    // Controle de numeração de jogos
    private val _numeroProximoJogo = MutableStateFlow(2)
    val numeroProximoJogo: StateFlow<Int> = _numeroProximoJogo.asStateFlow()

    // Sessão atual
    private val _sessaoAtual = MutableStateFlow<SessaoJogos?>(null)
    val sessaoAtual: StateFlow<SessaoJogos?> = _sessaoAtual.asStateFlow()

    // Lista de presença (ordem de chegada)
    private val _listaPresenca = MutableStateFlow<List<Pair<Jogador, Long>>>(emptyList())
    val listaPresenca: StateFlow<List<Pair<Jogador, Long>>> = _listaPresenca.asStateFlow()

    // Fila de espera
    private val _filaEspera = MutableStateFlow<List<JogadorNaFila>>(emptyList())
    val filaEspera: StateFlow<List<JogadorNaFila>> = _filaEspera.asStateFlow()

    // Jogo atual
    private val _jogoAtualId = MutableStateFlow<Long?>(null)
    val jogoAtualId: StateFlow<Long?> = _jogoAtualId.asStateFlow()

    private val _timeBrancoAtual = MutableStateFlow<List<Jogador>>(emptyList())
    val timeBrancoAtual: StateFlow<List<Jogador>> = _timeBrancoAtual.asStateFlow()

    private val _timeVermelhoAtual = MutableStateFlow<List<Jogador>>(emptyList())
    val timeVermelhoAtual: StateFlow<List<Jogador>> = _timeVermelhoAtual.asStateFlow()

    private val _duracaoAtual = MutableStateFlow(30)
    val duracaoAtual: StateFlow<Int> = _duracaoAtual.asStateFlow()

    // Placar
    private val _placarBranco = MutableStateFlow(0)
    val placarBranco: StateFlow<Int> = _placarBranco.asStateFlow()

    private val _placarVermelho = MutableStateFlow(0)
    val placarVermelho: StateFlow<Int> = _placarVermelho.asStateFlow()

    // Controle de vencedor (para próximo jogo)
    private val _vencedorUltimoJogo = MutableStateFlow<TimeColor?>(null)
    val vencedorUltimoJogo: StateFlow<TimeColor?> = _vencedorUltimoJogo.asStateFlow()

    private val _jogadoresUltimoTimeGanhador = MutableStateFlow<List<Jogador>>(emptyList())
    val jogadoresUltimoTimeGanhador: StateFlow<List<Jogador>> = _jogadoresUltimoTimeGanhador.asStateFlow()

    /**
     * Inicializa uma nova sessão de jogos para o dia.
     * Limpa todos os dados da sessão anterior.
     */
    fun iniciarNovoDia() {
        viewModelScope.launch {
            val hoje = System.currentTimeMillis()
            _sessaoAtual.value = SessaoJogos(
                data = hoje,
                jogos = emptyList(),
                jogoAtual = null,
                filaEspera = emptyList(),
                totalPresentes = 0
            )
            _listaPresenca.value = emptyList()
            _vencedorUltimoJogo.value = null
            _jogadoresUltimoTimeGanhador.value = emptyList()
            Log.d(TAG, "Nova sessão iniciada")
        }
    }

    /**
     * Adiciona um jogador à lista de presença.
     * Registra o horário de chegada automaticamente.
     *
     * @param jogador Jogador a ser adicionado
     */
    fun adicionarAListaPresenca(jogador: Jogador) {
        viewModelScope.launch {
            val horarioChegada = System.currentTimeMillis()
            _listaPresenca.value = _listaPresenca.value + Pair(jogador, horarioChegada)
            presencaRepository.registrarPresenca(jogador.id, horarioChegada)
            Log.d(TAG, "Jogador ${jogador.nome} adicionado à lista de presença")
        }
    }

    /**
     * Remove um jogador da lista de presença.
     *
     * @param jogadorId ID do jogador a ser removido
     */
    fun removerDaListaPresenca(jogadorId: Long) {
        viewModelScope.launch {
            _listaPresenca.value = _listaPresenca.value.filter { it.first.id != jogadorId }
            Log.d(TAG, "Jogador ID $jogadorId removido da lista de presença")
        }
    }

    /**
     * Cria um novo jogo e registra as participações.
     *
     * Lógica de numeração:
     * - Se é o primeiro jogo do dia: numeroJogo = 1
     * - Se é jogo subsequente: usa o contador interno
     *
     * Lógica de duração:
     * - Deve ser passada como parâmetro (30 ou 15 minutos)
     *
     * @param timeBranco Lista de jogadores do time branco
     * @param timeVermelho Lista de jogadores do time vermelho
     * @param duracao Duração do jogo em minutos (30 para 1º jogo, 15 para demais)
     */
    fun criarPrimeiroJogo(
        timeBranco: List<Jogador>,
        timeVermelho: List<Jogador>,
        duracao: Int
    ) {
        viewModelScope.launch {
            try {
                val numeroJogo = if (_jogoAtualId.value == null) 1 else _numeroProximoJogo.value

                val jogo = Jogo(
                    data = System.currentTimeMillis(),
                    numeroJogo = numeroJogo,
                    duracao = duracao,
                    status = StatusJogo.EM_ANDAMENTO,
                    placarBranco = 0,
                    placarVermelho = 0
                )

                val jogoId = jogoRepository.criarJogo(jogo)
                _jogoAtualId.value = jogoId

                // Registrar participações
                val participacoes = mutableListOf<Participacao>()
                timeBranco.forEach { jogador ->
                    participacoes.add(
                        Participacao(
                            jogadorId = jogador.id,
                            jogoId = jogoId,
                            time = TimeColor.BRANCO
                        )
                    )
                }
                timeVermelho.forEach { jogador ->
                    participacoes.add(
                        Participacao(
                            jogadorId = jogador.id,
                            jogoId = jogoId,
                            time = TimeColor.VERMELHO
                        )
                    )
                }

                jogoRepository.adicionarParticipacoes(participacoes)

                // Atualizar estado
                _timeBrancoAtual.value = timeBranco
                _timeVermelhoAtual.value = timeVermelho
                _duracaoAtual.value = duracao
                _placarBranco.value = 0
                _placarVermelho.value = 0

                // Incrementar contador para próximo jogo
                _numeroProximoJogo.value = numeroJogo + 1

                Log.d(TAG, "Jogo $numeroJogo criado (ID: $jogoId, duração: $duracao min)")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar jogo", e)
            }
        }
    }

    /**
     * Finaliza o jogo atual e salva o resultado.
     * Armazena o vencedor e time ganhador para o próximo jogo.
     *
     * @param vencedor Time vencedor (BRANCO, VERMELHO ou null para empate)
     */
    fun finalizarJogo(vencedor: TimeColor?) {
        viewModelScope.launch {
            try {
                val jogoId = _jogoAtualId.value ?: return@launch

                jogoRepository.finalizarJogo(
                    id = jogoId,
                    vencedor = vencedor
                )

                jogoRepository.atualizarJogo(
                    Jogo(
                        id = jogoId,
                        data = System.currentTimeMillis(),
                        numeroJogo = 1,
                        duracao = _duracaoAtual.value,
                        status = StatusJogo.FINALIZADO,
                        timeVencedor = vencedor,
                        placarBranco = _placarBranco.value,
                        placarVermelho = _placarVermelho.value
                    )
                )

                // Armazenar vencedor para próximo jogo
                _vencedorUltimoJogo.value = vencedor
                _jogadoresUltimoTimeGanhador.value = when (vencedor) {
                    TimeColor.BRANCO -> _timeBrancoAtual.value
                    TimeColor.VERMELHO -> _timeVermelhoAtual.value
                    null -> _timeBrancoAtual.value // Empate: time branco permanece
                }

                Log.d(TAG, "Jogo finalizado. Vencedor: ${vencedor?.name ?: "EMPATE"}")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao finalizar jogo", e)
            }
        }
    }

    /**
     * Incrementa o placar do time branco em 1.
     */
    fun incrementarPlacarBranco() {
        _placarBranco.value++
    }

    /**
     * Incrementa o placar do time vermelho em 1.
     */
    fun incrementarPlacarVermelho() {
        _placarVermelho.value++
    }

    /**
     * Limpa os dados do jogo atual, mantendo informações do vencedor.
     * Usado ao passar para o próximo jogo (botão "Próximo Jogo").
     */
    fun limparJogoAtual() {
        _jogoAtualId.value = null
        _timeBrancoAtual.value = emptyList()
        _timeVermelhoAtual.value = emptyList()
        _placarBranco.value = 0
        _placarVermelho.value = 0
        Log.d(TAG, "Dados do jogo atual limpos")
    }

    /**
     * Limpa TODOS os dados da sessão, incluindo vencedor e contador.
     * Usado ao encerrar a sessão (botão "Encerrar Sessão").
     */
    fun limparSessaoCompleta() {
        _jogoAtualId.value = null
        _timeBrancoAtual.value = emptyList()
        _timeVermelhoAtual.value = emptyList()
        _duracaoAtual.value = 30
        _placarBranco.value = 0
        _placarVermelho.value = 0
        _vencedorUltimoJogo.value = null
        _jogadoresUltimoTimeGanhador.value = emptyList()
        _numeroProximoJogo.value = 2
        Log.d(TAG, "Sessão completa encerrada e resetada")
    }

    /**
     * Marca um jogador como inativo e remove da lista/fila.
     * Usado quando um jogador vai embora durante a sessão.
     *
     * @param jogadorId ID do jogador a ser marcado como inativo
     */
    fun marcarJogadorComoInativo(jogadorId: Long) {
        viewModelScope.launch {
            _listaPresenca.value = _listaPresenca.value.filter { it.first.id != jogadorId }
            _filaEspera.value = _filaEspera.value.filter { it.jogador.id != jogadorId }
            Log.d(TAG, "Jogador ID $jogadorId marcado como inativo")
        }
    }

    /**
     * Registra um gol para um dos times.
     * Atualiza o placar no banco de dados.
     *
     * @param time Time que marcou o gol
     */
    fun registrarGol(time: TimeColor) {
        viewModelScope.launch {
            val jogoAtual = _sessaoAtual.value?.jogoAtual ?: return@launch
            jogoRepository.registrarGol(jogoAtual.id, time)
        }
    }

    companion object {
        private const val TAG = "SessaoViewModel"
    }
}