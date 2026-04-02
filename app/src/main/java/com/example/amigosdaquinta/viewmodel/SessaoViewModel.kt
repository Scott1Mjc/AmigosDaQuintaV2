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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessaoViewModel(
    private val jogoRepository: JogoRepository,
    private val participacaoRepository: ParticipacaoRepository,
    private val presencaRepository: PresencaRepository,
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SessaoViewModel"
    }

    private val _listaPresenca = MutableStateFlow<List<Pair<Jogador, Long>>>(emptyList())
    val listaPresenca: StateFlow<List<Pair<Jogador, Long>>> = _listaPresenca.asStateFlow()

    private val _jogoAtualId = MutableStateFlow<Long?>(null)
    val jogoAtualId: StateFlow<Long?> = _jogoAtualId.asStateFlow()

    private val _timeBrancoAtual = MutableStateFlow<List<Jogador>>(emptyList())
    val timeBrancoAtual: StateFlow<List<Jogador>> = _timeBrancoAtual.asStateFlow()

    private val _timeVermelhoAtual = MutableStateFlow<List<Jogador>>(emptyList())
    val timeVermelhoAtual: StateFlow<List<Jogador>> = _timeVermelhoAtual.asStateFlow()

    private val _placarBranco = MutableStateFlow(0)
    val placarBranco: StateFlow<Int> = _placarBranco.asStateFlow()

    private val _placarVermelho = MutableStateFlow(0)
    val placarVermelho: StateFlow<Int> = _placarVermelho.asStateFlow()

    private val _numeroDoProximoJogo = MutableStateFlow(1)
    val numeroDoProximoJogo: StateFlow<Int> = _numeroDoProximoJogo.asStateFlow()

    private val _jogosConsecutivosTimeAtual = MutableStateFlow(0)
    val jogosConsecutivosTimeAtual: StateFlow<Int> = _jogosConsecutivosTimeAtual.asStateFlow()

    private val _duracaoJogoAtualMinutos = MutableStateFlow(30)
    val duracaoJogoAtualMinutos: StateFlow<Int> = _duracaoJogoAtualMinutos.asStateFlow()

    private val _tempoRestanteSegundos = MutableStateFlow(30 * 60)
    val tempoRestanteSegundos: StateFlow<Int> = _tempoRestanteSegundos.asStateFlow()

    private val _timerPausado = MutableStateFlow(true)
    val timerPausado: StateFlow<Boolean> = _timerPausado.asStateFlow()

    private val _temJogoAtivo = MutableStateFlow(false)
    val temJogoAtivo: StateFlow<Boolean> = _temJogoAtivo.asStateFlow()

    private val _eventoTempoEsgotado = MutableSharedFlow<Unit>()
    val eventoTempoEsgotado: SharedFlow<Unit> = _eventoTempoEsgotado.asSharedFlow()

    private var timerJob: Job? = null
    
    private var timeGanhadorUltimoJogo: TimeColor? = null

    private val _jogadoresSubstituidosIds = MutableStateFlow<Set<Long>>(emptySet())
    val jogadoresSubstituidosIds: StateFlow<Set<Long>> = _jogadoresSubstituidosIds.asStateFlow()

    private val _jogadoresQueEntraramSubstitutosIds = MutableStateFlow<Set<Long>>(emptySet())
    val jogadoresQueEntraramSubstitutosIds: StateFlow<Set<Long>> = _jogadoresQueEntraramSubstitutosIds.asStateFlow()

    private val _jogadoresUltimoJogoIds = MutableStateFlow<Set<Long>>(emptySet())
    val jogadoresUltimoJogoIds: StateFlow<Set<Long>> = _jogadoresUltimoJogoIds.asStateFlow()

    init {
        restaurarSessaoSeExistir()
        iniciarTimerLoop()
    }

    private fun iniciarTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!_timerPausado.value && _tempoRestanteSegundos.value > 0) {
                    _tempoRestanteSegundos.value--
                    if (_tempoRestanteSegundos.value == 0) {
                        _eventoTempoEsgotado.emit(Unit)
                        _timerPausado.value = true
                    }
                }
            }
        }
    }

    fun alternarPausaTimer() {
        _timerPausado.value = !_timerPausado.value
    }

    private fun restaurarSessaoSeExistir() {
        viewModelScope.launch {
            try {
                val jogoEmAndamento = jogoRepository.obterJogoPorStatus(StatusJogo.EM_ANDAMENTO)
                if (jogoEmAndamento != null) {
                    _jogoAtualId.value = jogoEmAndamento.id
                    _placarBranco.value = jogoEmAndamento.placarBranco
                    _placarVermelho.value = jogoEmAndamento.placarVermelho
                    _numeroDoProximoJogo.value = jogoEmAndamento.numeroJogo
                    _duracaoJogoAtualMinutos.value = jogoEmAndamento.duracao
                    _temJogoAtivo.value = true
                    _tempoRestanteSegundos.value = jogoEmAndamento.duracao * 60

                    val participacoes = participacaoRepository.obterPorJogo(jogoEmAndamento.id)
                    val jogadoresIds = participacoes.map { it.jogadorId }
                    val todosJogadores = jogadorRepository.obterPorIds(jogadoresIds)

                    _timeBrancoAtual.value = todosJogadores.filter { j ->
                        participacoes.any { it.jogadorId == j.id && it.time == TimeColor.BRANCO }
                    }
                    _timeVermelhoAtual.value = todosJogadores.filter { j ->
                        participacoes.any { it.jogadorId == j.id && it.time == TimeColor.VERMELHO }
                    }

                    _jogadoresSubstituidosIds.value = participacoes.filter { it.foiSubstituido }.map { it.jogadorId }.toSet()
                    _jogadoresQueEntraramSubstitutosIds.value = participacoes.filter { it.entrouComoSubstituto }.map { it.jogadorId }.toSet()
                    _jogadoresUltimoJogoIds.value = participacoes.map { it.jogadorId }.toSet()

                    val presencas = presencaRepository.obterPresencasOrdenadas()
                    _listaPresenca.value = presencas.map { Pair(it.jogador, it.horarioChegada) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao restaurar sessão: ${e.message}")
            }
        }
    }

    fun criarJogo(timeBranco: List<Jogador>, timeVermelho: List<Jogador>) {
        viewModelScope.launch {
            try {
                val numeroJogo = _numeroDoProximoJogo.value
                val duracao = if (numeroJogo == 1) 30 else 15
                _duracaoJogoAtualMinutos.value = duracao
                _tempoRestanteSegundos.value = duracao * 60
                _timerPausado.value = false

                val jogoId = jogoRepository.criarJogo(Jogo(
                    data = System.currentTimeMillis(),
                    numeroJogo = numeroJogo,
                    duracao = duracao,
                    status = StatusJogo.EM_ANDAMENTO
                ))
                _jogoAtualId.value = jogoId
                _temJogoAtivo.value = true

                val participacoes = mutableListOf<Participacao>()
                timeBranco.forEach { participacoes.add(Participacao(jogadorId = it.id, jogoId = jogoId, time = TimeColor.BRANCO)) }
                timeVermelho.forEach { participacoes.add(Participacao(jogadorId = it.id, jogoId = jogoId, time = TimeColor.VERMELHO)) }
                jogoRepository.adicionarParticipacoes(participacoes)

                jogadorRepository.atualizarStatusCampoMuitos((timeBranco + timeVermelho).map { it.id }, true)

                _timeBrancoAtual.value = timeBranco
                _timeVermelhoAtual.value = timeVermelho
                _placarBranco.value = 0
                _placarVermelho.value = 0
                _jogadoresSubstituidosIds.value = emptySet()
                _jogadoresQueEntraramSubstitutosIds.value = emptySet()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar jogo: ${e.message}")
            }
        }
    }

    fun finalizarJogo(vencedor: TimeColor?) {
        viewModelScope.launch {
            try {
                val jogoId = _jogoAtualId.value ?: return@launch
                _timerPausado.value = true
                jogoRepository.finalizarJogo(jogoId, _placarBranco.value, _placarVermelho.value, vencedor)
                _temJogoAtivo.value = false
                timeGanhadorUltimoJogo = vencedor
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao finalizar jogo: ${e.message}")
            }
        }
    }

    fun prepararProximaPartida(vencedor: TimeColor?) {
        viewModelScope.launch {
            try {
                _numeroDoProximoJogo.value++
                
                // Salva quem jogou para a rotação
                val jogadoresQueJogaram = (_timeBrancoAtual.value + _timeVermelhoAtual.value).map { it.id }.toSet()
                _jogadoresUltimoJogoIds.value = jogadoresQueJogaram

                val ativosBranco = _timeBrancoAtual.value.filter { j -> !_jogadoresSubstituidosIds.value.contains(j.id) }
                val ativosVermelho = _timeVermelhoAtual.value.filter { j -> !_jogadoresSubstituidosIds.value.contains(j.id) }

                if (vencedor == null) {
                    _jogosConsecutivosTimeAtual.value = 0
                    // No empate, ambos saem (regra da pelada) ou um é escolhido.
                    _timeBrancoAtual.value = emptyList()
                    _timeVermelhoAtual.value = emptyList()
                } else {
                    if (vencedor == timeGanhadorUltimoJogo) {
                        _jogosConsecutivosTimeAtual.value++
                    } else {
                        _jogosConsecutivosTimeAtual.value = 1
                    }

                    if (vencedor == TimeColor.BRANCO) {
                        _timeBrancoAtual.value = ativosBranco
                        _timeVermelhoAtual.value = emptyList()
                    } else {
                        _timeVermelhoAtual.value = ativosVermelho
                        _timeBrancoAtual.value = emptyList()
                    }
                }
                
                jogadorRepository.atualizarStatusCampoMuitos(jogadoresQueJogaram.toList(), false)

                _jogadoresSubstituidosIds.value = emptySet()
                _jogadoresQueEntraramSubstitutosIds.value = emptySet()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao preparar próxima partida: ${e.message}")
            }
        }
    }

    fun substituirJogador(jogadorSaindo: Jogador, jogadorEntrando: Jogador, time: TimeColor, isLesionado: Boolean = false) {
        viewModelScope.launch {
            try {
                if (time == TimeColor.BRANCO) _timeBrancoAtual.update { it + jogadorEntrando }
                else _timeVermelhoAtual.update { it + jogadorEntrando }

                _jogadoresSubstituidosIds.update { it + jogadorSaindo.id }
                _jogadoresQueEntraramSubstitutosIds.update { it + jogadorEntrando.id }

                if (isLesionado) removerDaListaPresenca(jogadorSaindo.id)

                jogadorRepository.atualizarStatusCampo(jogadorEntrando.id, true)
                jogadorRepository.atualizarStatusCampo(jogadorSaindo.id, false)

                _jogoAtualId.value?.let { jogoId ->
                    participacaoRepository.marcarComoSubstituido(jogoId, jogadorSaindo.id)
                    participacaoRepository.inserir(Participacao(jogadorId = jogadorEntrando.id, jogoId = jogoId, time = time, entrouComoSubstituto = true))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro na substituição: ${e.message}")
            }
        }
    }

    fun adicionarAListaPresenca(jogador: Jogador) {
        viewModelScope.launch {
            if (!_listaPresenca.value.any { it.first.id == jogador.id }) {
                val timestamp = System.currentTimeMillis()
                _listaPresenca.update { it + (jogador to timestamp) }
                presencaRepository.registrarPresenca(jogador.id, timestamp)
            }
        }
    }

    fun removerDaListaPresenca(jogadorId: Long) {
        viewModelScope.launch {
            try {
                // 1. Marcar como inativo no banco de dados para persistência
                val presencas = presencaRepository.obterPresencasOrdenadas()
                presencas.find { it.jogador.id == jogadorId }?.let {
                    presencaRepository.marcarComoInativo(it.presenca.id)
                }

                // 2. Remover da lista em memória (fila de espera)
                _listaPresenca.update { list -> list.filter { it.first.id != jogadorId } }

                // 3. Remover dos times atuais se estiver escalado
                _timeBrancoAtual.update { list -> list.filter { it.id != jogadorId } }
                _timeVermelhoAtual.update { list -> list.filter { it.id != jogadorId } }
                
                // 4. Resetar status de campo no repo de jogadores
                jogadorRepository.atualizarStatusCampo(jogadorId, false)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover jogador da presença: ${e.message}")
            }
        }
    }

    /**
     * Remove múltiplos jogadores da presença (saída definitiva da pelada).
     */
    fun removerVariosDaListaPresenca(jogadorIds: List<Long>) {
        viewModelScope.launch {
            try {
                // 1. Banco de Dados (Inativação lógica)
                presencaRepository.marcarJogadoresComoInativosNoDia(jogadorIds)

                // 2. Memória (Fila de Espera)
                _listaPresenca.update { list ->
                    list.filter { it.first.id !in jogadorIds }
                }

                // 3. Memória (Times Atuais)
                _timeBrancoAtual.update { list ->
                    list.filter { it.id !in jogadorIds }
                }
                _timeVermelhoAtual.update { list ->
                    list.filter { it.id !in jogadorIds }
                }

                // 4. Status de Campo
                jogadorRepository.atualizarStatusCampoMuitos(jogadorIds, false)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover múltiplos jogadores: ${e.message}")
            }
        }
    }

    fun iniciarNovoDia() {
        viewModelScope.launch {
            val lista = jogadorRepository.obterTodosAtivos().first()
            jogadorRepository.atualizarStatusCampoMuitos(lista.map { it.id }, false)
            presencaRepository.limparTodasPresencas()
            _listaPresenca.value = emptyList()
            _jogoAtualId.value = null
            _timeBrancoAtual.value = emptyList()
            _timeVermelhoAtual.value = emptyList()
            _placarBranco.value = 0
            _placarVermelho.value = 0
            _numeroDoProximoJogo.value = 1
            _jogosConsecutivosTimeAtual.value = 0
            _temJogoAtivo.value = false
            _timerPausado.value = true
            _duracaoJogoAtualMinutos.value = 30
            _tempoRestanteSegundos.value = 30 * 60
            _jogadoresUltimoJogoIds.value = emptySet()
            timeGanhadorUltimoJogo = null
        }
    }

    fun incrementarPlacarBranco() {
        _placarBranco.value++
        viewModelScope.launch { _jogoAtualId.value?.let { jogoRepository.registrarGol(it, TimeColor.BRANCO) } }
    }

    fun incrementarPlacarVermelho() {
        _placarVermelho.value++
        viewModelScope.launch { _jogoAtualId.value?.let { jogoRepository.registrarGol(it, TimeColor.VERMELHO) } }
    }
}
