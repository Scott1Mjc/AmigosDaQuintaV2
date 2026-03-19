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

    private var timeIncumbente: TimeColor? = null

    private val _jogadoresSubstituidosIds = MutableStateFlow<Set<Long>>(emptySet())
    val jogadoresSubstituidosIds: StateFlow<Set<Long>> = _jogadoresSubstituidosIds.asStateFlow()

    private val _jogadoresQueEntraramSubstitutosIds = MutableStateFlow<Set<Long>>(emptySet())
    val jogadoresQueEntraramSubstitutosIds: StateFlow<Set<Long>> = _jogadoresQueEntraramSubstitutosIds.asStateFlow()

    init {
        restaurarSessaoSeExistir()
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

                    val participacoes = participacaoRepository.obterPorJogo(jogoEmAndamento.id)
                    val idsBranco = participacoes.filter { it.time == TimeColor.BRANCO }.map { it.jogadorId }
                    val idsVermelho = participacoes.filter { it.time == TimeColor.VERMELHO }.map { it.jogadorId }
                    
                    _timeBrancoAtual.value = jogadorRepository.obterPorIds(idsBranco)
                    _timeVermelhoAtual.value = jogadorRepository.obterPorIds(idsVermelho)
                    
                    _jogadoresSubstituidosIds.value = participacoes.filter { it.foiSubstituido }.map { it.jogadorId }.toSet()
                    _jogadoresQueEntraramSubstitutosIds.value = participacoes.filter { it.entrouComoSubstituto }.map { it.jogadorId }.toSet()
                    
                    if (jogoEmAndamento.duracao == 15) {
                        _jogosConsecutivosTimeAtual.value = 1
                    } else {
                        _jogosConsecutivosTimeAtual.value = 0
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao restaurar sessão: ${e.message}")
            }
        }
    }

    private fun atualizarDuracaoJogo() {
        _duracaoJogoAtualMinutos.value = if (_jogosConsecutivosTimeAtual.value == 1) 15 else 30
    }

    fun substituirJogador(jogadorSaindo: Jogador, jogadorEntrando: Jogador, time: TimeColor, isLesionado: Boolean = false) {
        viewModelScope.launch {
            try {
                when (time) {
                    TimeColor.BRANCO -> {
                        if (!_timeBrancoAtual.value.any { it.id == jogadorEntrando.id }) {
                            _timeBrancoAtual.value = _timeBrancoAtual.value + jogadorEntrando
                        }
                    }
                    TimeColor.VERMELHO -> {
                        if (!_timeVermelhoAtual.value.any { it.id == jogadorEntrando.id }) {
                            _timeVermelhoAtual.value = _timeVermelhoAtual.value + jogadorEntrando
                        }
                    }
                }
                
                _jogadoresSubstituidosIds.value = _jogadoresSubstituidosIds.value + jogadorSaindo.id
                _jogadoresQueEntraramSubstitutosIds.value = _jogadoresQueEntraramSubstitutosIds.value + jogadorEntrando.id
                
                if (isLesionado) {
                    removerDaListaPresenca(jogadorSaindo.id)
                } else {
                    rotacionarJogadores(listOf(jogadorSaindo))
                }

                moverParaTopoDaFila(jogadorEntrando)
                
                jogadorRepository.atualizarStatusCampo(jogadorEntrando.id, true)
                jogadorRepository.atualizarStatusCampo(jogadorSaindo.id, false)

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

    private fun moverParaTopoDaFila(jogador: Jogador) {
        val currentList = _listaPresenca.value.toMutableList()
        val index = currentList.indexOfFirst { it.first.id == jogador.id }
        if (index != -1) {
            val item = currentList.removeAt(index)
            currentList.add(0, item.first to 0L)
        } else {
            currentList.add(0, jogador to 0L)
        }
        _listaPresenca.value = currentList
    }

    fun removerDaListaPresenca(jogadorId: Long) {
        _listaPresenca.value = _listaPresenca.value.filter { it.first.id != jogadorId }
    }

    fun criarJogo(timeBranco: List<Jogador>, timeVermelho: List<Jogador>) {
        viewModelScope.launch {
            try {
                timeIncumbente = when {
                    _timeBrancoAtual.value.isNotEmpty() && _timeVermelhoAtual.value.isEmpty() -> TimeColor.BRANCO
                    _timeVermelhoAtual.value.isNotEmpty() && _timeBrancoAtual.value.isEmpty() -> TimeColor.VERMELHO
                    else -> null
                }

                timeBranco.forEach { adicionarAListaPresenca(it) }
                timeVermelho.forEach { adicionarAListaPresenca(it) }

                // REORDENAÇÃO DA FILA: Skipped players movem para depois dos jogadores em campo
                val todosNoJogoIds = (timeBranco + timeVermelho).map { it.id }.toSet()
                val currentPresenca = _listaPresenca.value.sortedBy { it.second }
                val updatedPresenca = mutableListOf<Pair<Jogador, Long>>()
                
                val jogadoresNoJogo = currentPresenca.filter { it.first.id in todosNoJogoIds }
                val jogadoresEsperando = currentPresenca.filter { it.first.id !in todosNoJogoIds }

                // Jogadores em campo ficam com timestamps antigos (0, 1, 2...) para manter 1-22
                jogadoresNoJogo.forEachIndexed { index, pair ->
                    updatedPresenca.add(pair.first to index.toLong())
                }

                // Jogadores esperando ficam com timestamps a partir de AGORA, preservando ordem relativa
                val baseTime = System.currentTimeMillis()
                jogadoresEsperando.forEachIndexed { index, pair ->
                    updatedPresenca.add(pair.first to (baseTime + index))
                }

                _listaPresenca.value = updatedPresenca

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
                
                val todosIds = (timeBranco + timeVermelho).map { it.id }
                jogadorRepository.atualizarStatusCampoMuitos(todosIds, true)

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
                        if (timeIncumbente == TimeColor.BRANCO) {
                            _jogosConsecutivosTimeAtual.value++
                        } else {
                            _jogosConsecutivosTimeAtual.value = 1
                        }

                        jogadoresQueSairaoAgora.addAll(ativosVermelho)
                        if (_jogosConsecutivosTimeAtual.value >= 2) {
                            jogadoresQueSairaoAgora.addAll(ativosBranco)
                            _jogosConsecutivosTimeAtual.value = 0
                        }
                    }
                    TimeColor.VERMELHO -> {
                        if (timeIncumbente == TimeColor.VERMELHO) {
                            _jogosConsecutivosTimeAtual.value++
                        } else {
                            _jogosConsecutivosTimeAtual.value = 1
                        }

                        jogadoresQueSairaoAgora.addAll(ativosBranco)
                        if (_jogosConsecutivosTimeAtual.value >= 2) {
                            jogadoresQueSairaoAgora.addAll(ativosVermelho)
                            _jogosConsecutivosTimeAtual.value = 0
                        }
                    }
                    null -> {
                        jogadoresQueSairaoAgora.addAll(ativosBranco)
                        jogadoresQueSairaoAgora.addAll(ativosVermelho)
                        _jogosConsecutivosTimeAtual.value = 0
                    }
                }
                
                jogadorRepository.atualizarStatusCampoMuitos(jogadoresQueSairaoAgora.map { it.id }, false)

                rotacionarJogadores(jogadoresQueSairaoAgora.distinctBy { it.id })
                _numeroDoProximoJogo.value++
                
                atualizarDuracaoJogo()
                _jogoAtualId.value = null
                
                val quemFica = when (vencedor) {
                    TimeColor.BRANCO -> if (_jogosConsecutivosTimeAtual.value == 1) ativosBranco else emptyList()
                    TimeColor.VERMELHO -> if (_jogosConsecutivosTimeAtual.value == 1) ativosVermelho else emptyList()
                    null -> emptyList()
                }
                
                if (vencedor == TimeColor.BRANCO && _jogosConsecutivosTimeAtual.value == 1) {
                    _timeBrancoAtual.value = quemFica
                    _timeVermelhoAtual.value = emptyList()
                } else if (vencedor == TimeColor.VERMELHO && _jogosConsecutivosTimeAtual.value == 1) {
                    _timeVermelhoAtual.value = quemFica
                    _timeBrancoAtual.value = emptyList()
                } else {
                    _timeBrancoAtual.value = emptyList()
                    _timeVermelhoAtual.value = emptyList()
                }

                _jogadoresSubstituidosIds.value = emptySet()
                _jogadoresQueEntraramSubstitutosIds.value = emptySet()
                timeIncumbente = null
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao finalizar jogo: ${e.message}")
            }
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

    fun limparJogoAtual() {
        viewModelScope.launch {
            val todosNoCampo = _timeBrancoAtual.value + _timeVermelhoAtual.value
            jogadorRepository.atualizarStatusCampoMuitos(todosNoCampo.map { it.id }, false)
            
            _jogoAtualId.value = null
            _timeBrancoAtual.value = emptyList()
            _timeVermelhoAtual.value = emptyList()
            _placarBranco.value = 0
            _placarVermelho.value = 0
            _jogadoresSubstituidosIds.value = emptySet()
            _jogadoresQueEntraramSubstitutosIds.value = emptySet()
            timeIncumbente = null
        }
    }

    fun iniciarNovoDia() {
        viewModelScope.launch {
            jogadorRepository.obterTodosAtivos().collect { lista ->
                jogadorRepository.atualizarStatusCampoMuitos(lista.map { it.id }, false)
            }
            _listaPresenca.value = emptyList()
            _numeroDoProximoJogo.value = 1
            _jogosConsecutivosTimeAtual.value = 0
            limparJogoAtual()
        }
    }
}
