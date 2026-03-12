package com.example.amigosdaquinta.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel principal que gerencia o fluxo de uma sessão de jogos em tempo real.
 *
 * Responsável pelo controle do placar, gerenciamento da fila de presença,
 * cronometragem (lógica de duração) e persistência do estado da partida atual.
 *
 * @property jogoRepository Repositório para operações de partidas.
 * @property participacaoRepository Repositório para estatísticas de jogadores.
 * @property presencaRepository Repositório para controle da fila de entrada.
 * @property jogadorRepository Repositório para dados cadastrais dos atletas.
 */
class SessaoViewModel(
    private val jogoRepository: JogoRepository,
    private val participacaoRepository: ParticipacaoRepository,
    private val presencaRepository: PresencaRepository,
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SessaoViewModel"
    }

    // region Estados da Sessão

    private val _listaPresenca = MutableStateFlow<List<Pair<Jogador, Long>>>(emptyList())
    /** Lista de jogadores presentes na sessão e seus respectivos horários de chegada. */
    val listaPresenca: StateFlow<List<Pair<Jogador, Long>>> = _listaPresenca.asStateFlow()

    private val _jogoAtualId = MutableStateFlow<Long?>(null)
    /** ID da partida em andamento no banco de dados. */
    val jogoAtualId: StateFlow<Long?> = _jogoAtualId.asStateFlow()

    private val _timeBrancoAtual = MutableStateFlow<List<Jogador>>(emptyList())
    /** Jogadores escalados no Time Branco para a partida atual. */
    val timeBrancoAtual: StateFlow<List<Jogador>> = _timeBrancoAtual.asStateFlow()

    private val _timeVermelhoAtual = MutableStateFlow<List<Jogador>>(emptyList())
    /** Jogadores escalados no Time Vermelho para a partida atual. */
    val timeVermelhoAtual: StateFlow<List<Jogador>> = _timeVermelhoAtual.asStateFlow()

    private val _placarBranco = MutableStateFlow(0)
    /** Gols marcados pelo Time Branco na partida atual. */
    val placarBranco: StateFlow<Int> = _placarBranco.asStateFlow()

    private val _placarVermelho = MutableStateFlow(0)
    /** Gols marcados pelo Time Vermelho na partida atual. */
    val placarVermelho: StateFlow<Int> = _placarVermelho.asStateFlow()

    private val _numeroDoProximoJogo = MutableStateFlow(1)
    /** Sequência numérica do próximo jogo a ser realizado no dia. */
    val numeroDoProximoJogo: StateFlow<Int> = _numeroDoProximoJogo.asStateFlow()

    private val _jogosConsecutivosTimeAtual = MutableStateFlow(0)
    /** Contador de vitórias seguidas do time atual (máximo de 2). */
    val jogosConsecutivosTimeAtual: StateFlow<Int> = _jogosConsecutivosTimeAtual.asStateFlow()

    private val _duracaoJogoAtualMinutos = MutableStateFlow(30)
    /** Duração definida para o jogo atual (30min para o 1º, 15min para os demais). */
    val duracaoJogoAtualMinutos: StateFlow<Int> = _duracaoJogoAtualMinutos.asStateFlow()

    private val _jogadoresSubstituidosIds = MutableStateFlow<Set<Long>>(emptySet())
    /** IDs de jogadores que saíram de campo nesta partida. */
    val jogadoresSubstituidosIds: StateFlow<Set<Long>> = _jogadoresSubstituidosIds.asStateFlow()

    private val _jogadoresQueEntraramSubstitutosIds = MutableStateFlow<Set<Long>>(emptySet())
    /** IDs de jogadores que entraram durante a partida como substitutos. */
    val jogadoresQueEntraramSubstitutosIds: StateFlow<Set<Long>> = _jogadoresQueEntraramSubstitutosIds.asStateFlow()

    // endregion

    init {
        restaurarSessaoSeExistir()
    }

    /**
     * Tenta recuperar o estado de uma partida que foi deixada em aberto no banco de dados.
     */
    private fun restaurarSessaoSeExistir() {
        viewModelScope.launch {
            try {
                val jogoEmAndamento = jogoRepository.obterJogoPorStatus(StatusJogo.EM_ANDAMENTO)
                if (jogoEmAndamento != null) {
                    _jogoAtualId.value = jogoEmAndamento.id
                    _placarBranco.value = jogoEmAndamento.placarBranco
                    _placarVermelho.value = jogoEmAndamento.placarVermelho
                    _numeroDoProximoJogo.value = jogoEmAndamento.numeroJogo

                    val participacoes = participacaoRepository.obterPorJogo(jogoEmAndamento.id)
                    val idsBranco = participacoes.filter { it.time == TimeColor.BRANCO }.map { it.jogadorId }
                    val idsVermelho = participacoes.filter { it.time == TimeColor.VERMELHO }.map { it.jogadorId }
                    
                    _timeBrancoAtual.value = jogadorRepository.obterPorIds(idsBranco)
                    _timeVermelhoAtual.value = jogadorRepository.obterPorIds(idsVermelho)
                    
                    _jogadoresSubstituidosIds.value = participacoes.filter { it.foiSubstituido }.map { it.jogadorId }.toSet()
                    _jogadoresQueEntraramSubstitutosIds.value = participacoes.filter { it.entrouComoSubstituto }.map { it.jogadorId }.toSet()
                    
                    atualizarDuracaoJogo()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao restaurar sessão: ${e.message}")
            }
        }
    }

    private fun atualizarDuracaoJogo() {
        _duracaoJogoAtualMinutos.value = if (_numeroDoProximoJogo.value == 1) 30 else 15
    }

    /**
     * Realiza a troca de jogadores durante uma partida.
     *
     * @param jogadorSaindo Atleta que deixa o campo.
     * @param jogadorEntrando Atleta que entra no jogo.
     * @param time Time onde a substituição ocorre.
     * @param isLesionado Se verdadeiro, remove o jogador da lista de presença do dia.
     */
    fun substituirJogador(jogadorSaindo: Jogador, jogadorEntrando: Jogador, time: TimeColor, isLesionado: Boolean = false) {
        viewModelScope.launch {
            try {
                when (time) {
                    TimeColor.BRANCO -> {
                        _timeBrancoAtual.value = _timeBrancoAtual.value
                            .filter { it.id != jogadorSaindo.id && it.id != jogadorEntrando.id }
                            .plus(jogadorEntrando)
                            .plus(jogadorSaindo)
                    }
                    TimeColor.VERMELHO -> {
                        _timeVermelhoAtual.value = _timeVermelhoAtual.value
                            .filter { it.id != jogadorSaindo.id && it.id != jogadorEntrando.id }
                            .plus(jogadorEntrando)
                            .plus(jogadorSaindo)
                    }
                }
                _jogadoresSubstituidosIds.value = _jogadoresSubstituidosIds.value + jogadorSaindo.id
                _jogadoresQueEntraramSubstitutosIds.value = _jogadoresQueEntraramSubstitutosIds.value + jogadorEntrando.id
                
                if (isLesionado) {
                    removerDaListaPresenca(jogadorSaindo.id)
                } else {
                    rotacionarJogadores(listOf(jogadorSaindo))
                }

                _jogoAtualId.value?.let { jogoId ->
                    participacaoRepository.marcarComoSubstituido(jogoId, jogadorSaindo.id)
                    participacaoRepository.inserir(
                        Participacao(
                            jogadorId = jogadorEntrando.id, 
                            jogoId = jogoId, 
                            time = time,
                            entrouComoSubstituto = true
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro na substituição: ${e.message}")
            }
        }
    }

    /**
     * Registra um jogador na lista de presença da sessão atual.
     */
    fun adicionarAListaPresenca(jogador: Jogador) {
        viewModelScope.launch {
            try {
                val jaExiste = _listaPresenca.value.any { it.first.id == jogador.id }
                if (!jaExiste) {
                    val horarioChegada = System.currentTimeMillis()
                    _listaPresenca.value = _listaPresenca.value + Pair(jogador, horarioChegada)
                    presencaRepository.registrarPresenca(jogador.id, horarioChegada)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao registrar presença: ${e.message}")
            }
        }
    }

    /**
     * Move os jogadores para o final da fila de espera.
     */
    fun rotacionarJogadores(jogadores: List<Jogador>) {
        val currentList = _listaPresenca.value.toMutableList()
        jogadores.forEach { jogador ->
            val index = currentList.indexOfFirst { it.first.id == jogador.id }
            if (index != -1) {
                val item = currentList.removeAt(index)
                currentList.add(item.first to System.currentTimeMillis())
            }
        }
        _listaPresenca.value = currentList
    }

    /**
     * Remove um jogador da lista de presença atual.
     */
    fun removerDaListaPresenca(jogadorId: Long) {
        _listaPresenca.value = _listaPresenca.value.filter { it.first.id != jogadorId }
    }

    /**
     * Inicia uma nova partida com os times escalados.
     */
    fun criarJogo(timeBranco: List<Jogador>, timeVermelho: List<Jogador>) {
        viewModelScope.launch {
            try {
                val numeroJogo = _numeroDoProximoJogo.value
                atualizarDuracaoJogo()

                val jogo = Jogo(
                    data = System.currentTimeMillis(),
                    numeroJogo = numeroJogo,
                    duracao = _duracaoJogoAtualMinutos.value,
                    status = StatusJogo.EM_ANDAMENTO,
                    placarBranco = 0,
                    placarVermelho = 0
                )

                val jogoId = jogoRepository.criarJogo(jogo)
                _jogoAtualId.value = jogoId

                val participacoes = mutableListOf<Participacao>()
                timeBranco.forEach { participacoes.add(Participacao(jogadorId = it.id, jogoId = jogoId, time = TimeColor.BRANCO)) }
                timeVermelho.forEach { participacoes.add(Participacao(jogadorId = it.id, jogoId = jogoId, time = TimeColor.VERMELHO)) }
                jogoRepository.adicionarParticipacoes(participacoes)

                _timeBrancoAtual.value = timeBranco
                _timeVermelhoAtual.value = timeVermelho
                _placarBranco.value = 0
                _placarVermelho.value = 0
                _jogadoresSubstituidosIds.value = emptySet()
                _jogadoresQueEntraramSubstitutosIds.value = emptySet()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar novo jogo: ${e.message}")
            }
        }
    }

    /**
     * Finaliza a partida atual e processa a rotatividade dos times.
     *
     * @param vencedor Time ganhador ou nulo para empate.
     */
    fun finalizarJogo(vencedor: TimeColor?) {
        viewModelScope.launch {
            try {
                val jogoId = _jogoAtualId.value ?: return@launch
                
                jogoRepository.finalizarJogo(id = jogoId, vencedor = vencedor)

                val jogadoresQueSairaoAgora = mutableListOf<Jogador>()
                val ativosBranco = _timeBrancoAtual.value.filter { it.id !in _jogadoresSubstituidosIds.value }
                val ativosVermelho = _timeVermelhoAtual.value.filter { it.id !in _jogadoresSubstituidosIds.value }

                when (vencedor) {
                    TimeColor.BRANCO -> {
                        _jogosConsecutivosTimeAtual.value++
                        jogadoresQueSairaoAgora.addAll(ativosVermelho)
                        if (_jogosConsecutivosTimeAtual.value >= 2) {
                            jogadoresQueSairaoAgora.addAll(ativosBranco)
                            _jogosConsecutivosTimeAtual.value = 0
                        }
                    }
                    TimeColor.VERMELHO -> {
                        jogadoresQueSairaoAgora.addAll(ativosBranco)
                        _jogosConsecutivosTimeAtual.value = 1
                    }
                    null -> {
                        jogadoresQueSairaoAgora.addAll(ativosBranco)
                        jogadoresQueSairaoAgora.addAll(ativosVermelho)
                        _jogosConsecutivosTimeAtual.value = 0
                    }
                }

                rotacionarJogadores(jogadoresQueSairaoAgora.distinctBy { it.id })
                _numeroDoProximoJogo.value++
                atualizarDuracaoJogo()
                limparJogoAtual()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao finalizar jogo: ${e.message}")
            }
        }
    }

    /** Incrementa o placar do Time Branco. */
    fun incrementarPlacarBranco() {
        _placarBranco.value++
        viewModelScope.launch { _jogoAtualId.value?.let { jogoRepository.registrarGol(it, TimeColor.BRANCO) } }
    }

    /** Incrementa o placar do Time Vermelho. */
    fun incrementarPlacarVermelho() {
        _placarVermelho.value++
        viewModelScope.launch { _jogoAtualId.value?.let { jogoRepository.registrarGol(it, TimeColor.VERMELHO) } }
    }

    /** Limpa apenas os dados da partida atual, sem resetar a lista de presença. */
    fun limparJogoAtual() {
        _jogoAtualId.value = null
        _timeBrancoAtual.value = emptyList()
        _timeVermelhoAtual.value = emptyList()
        _placarBranco.value = 0
        _placarVermelho.value = 0
        _jogadoresSubstituidosIds.value = emptySet()
        _jogadoresQueEntraramSubstitutosIds.value = emptySet()
    }

    /** Reseta toda a sessão do dia para iniciar um novo dia de pelada. */
    fun iniciarNovoDia() {
        _listaPresenca.value = emptyList()
        _numeroDoProximoJogo.value = 1
        _jogosConsecutivosTimeAtual.value = 0
        limparJogoAtual()
    }
}
