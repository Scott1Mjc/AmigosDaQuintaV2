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
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _sessaoAtual = MutableStateFlow<SessaoJogos?>(null)
    val sessaoAtual: StateFlow<SessaoJogos?> = _sessaoAtual.asStateFlow()

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

    private val _vencedorUltimoJogo = MutableStateFlow<TimeColor?>(null)
    val vencedorUltimoJogo: StateFlow<TimeColor?> = _vencedorUltimoJogo.asStateFlow()

    private val _jogadoresUltimoTimeGanhador = MutableStateFlow<List<Jogador>>(emptyList())
    val jogadoresUltimoTimeGanhador: StateFlow<List<Jogador>> = _jogadoresUltimoTimeGanhador.asStateFlow()

    private val _jogosConsecutivosTimeAtual = MutableStateFlow(0)
    val jogosConsecutivosTimeAtual: StateFlow<Int> = _jogosConsecutivosTimeAtual.asStateFlow()

    private val _duracaoJogoAtualMinutos = MutableStateFlow(30)
    val duracaoJogoAtualMinutos: StateFlow<Int> = _duracaoJogoAtualMinutos.asStateFlow()

    private val _timestampInicioJogo = MutableStateFlow<Long?>(null)

    private val _jogadoresSubstituidosIds = MutableStateFlow<Set<Long>>(emptySet())
    val jogadoresSubstituidosIds: StateFlow<Set<Long>> = _jogadoresSubstituidosIds.asStateFlow()

    init {
        restaurarSessaoSeExistir()
    }

    /**
     * Tenta restaurar o estado de um jogo em andamento do banco de dados.
     */
    private fun restaurarSessaoSeExistir() {
        viewModelScope.launch {
            try {
                val jogoEmAndamento = jogoRepository.obterJogoPorStatus(StatusJogo.EM_ANDAMENTO)
                if (jogoEmAndamento != null) {
                    Log.d(TAG, "Restaurando jogo em andamento ID: ${jogoEmAndamento.id}")
                    _jogoAtualId.value = jogoEmAndamento.id
                    _placarBranco.value = jogoEmAndamento.placarBranco
                    _placarVermelho.value = jogoEmAndamento.placarVermelho
                    _numeroDoProximoJogo.value = jogoEmAndamento.numeroJogo
                    _timestampInicioJogo.value = jogoEmAndamento.data

                    val participacoes = participacaoRepository.obterPorJogo(jogoEmAndamento.id)
                    val idsBranco = participacoes.filter { it.time == TimeColor.BRANCO }.map { it.jogadorId }
                    val idsVermelho = participacoes.filter { it.time == TimeColor.VERMELHO }.map { it.jogadorId }
                    
                    _timeBrancoAtual.value = jogadorRepository.obterPorIds(idsBranco)
                    _timeVermelhoAtual.value = jogadorRepository.obterPorIds(idsVermelho)
                    
                    _jogadoresSubstituidosIds.value = participacoes.filter { it.foiSubstituido }.map { it.jogadorId }.toSet()
                    
                    atualizarDuracaoJogo()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao restaurar sessão", e)
            }
        }
    }

    private fun atualizarDuracaoJogo() {
        _duracaoJogoAtualMinutos.value = if (_numeroDoProximoJogo.value == 1) 30 else 15
    }

    fun substituirJogador(jogadorSaindo: Jogador, jogadorEntrando: Jogador, time: TimeColor, isLesionado: Boolean = false) {
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
        
        viewModelScope.launch {
            if (isLesionado) {
                removerDaListaPresenca(jogadorSaindo.id)
            } else {
                rotacionarJogadores(listOf(jogadorSaindo))
            }

            _jogoAtualId.value?.let { jogoId ->
                participacaoRepository.marcarComoSubstituido(jogoId, jogadorSaindo.id)
                val participacoes = participacaoRepository.obterPorJogo(jogoId)
                if (!participacoes.any { it.jogadorId == jogadorEntrando.id }) {
                    participacaoRepository.inserir(
                        Participacao(jogadorId = jogadorEntrando.id, jogoId = jogoId, time = time)
                    )
                }
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
                Log.e(TAG, "Erro ao adicionar jogador", e)
            }
        }
    }

    fun rotacionarJogadores(jogadores: List<Jogador>) {
        val currentList = _listaPresenca.value.toMutableList()
        jogadores.forEach { jogador ->
            val index = currentList.indexOfFirst { it.first.id == jogador.id }
            if (index != -1) {
                val item = currentList.removeAt(index)
                // Atualiza o timestamp para o fim da fila
                currentList.add(item.first to System.currentTimeMillis())
            }
        }
        _listaPresenca.value = currentList
    }

    fun removerDaListaPresenca(jogadorId: Long) {
        viewModelScope.launch {
            _listaPresenca.value = _listaPresenca.value.filter { it.first.id != jogadorId }
        }
    }

    fun criarJogo(timeBranco: List<Jogador>, timeVermelho: List<Jogador>) {
        viewModelScope.launch {
            try {
                (timeBranco + timeVermelho).forEach { jogador ->
                    if (!_listaPresenca.value.any { it.first.id == jogador.id }) {
                        adicionarAListaPresenca(jogador)
                    }
                }

                val numeroJogo = _numeroDoProximoJogo.value
                atualizarDuracaoJogo()
                _timestampInicioJogo.value = System.currentTimeMillis()

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
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar jogo", e)
            }
        }
    }

    fun finalizarJogo(vencedor: TimeColor?) {
        viewModelScope.launch {
            try {
                val jogoId = _jogoAtualId.value ?: return@launch
                
                jogoRepository.atualizarJogo(
                    jogoRepository.obterPorId(jogoId)!!.copy(
                        placarBranco = _placarBranco.value,
                        placarVermelho = _placarVermelho.value
                    )
                )
                
                jogoRepository.finalizarJogo(id = jogoId, vencedor = vencedor)

                val jogadoresSaindo = mutableListOf<Jogador>()
                val timeBrancoAtivos = _timeBrancoAtual.value.filter { it.id !in _jogadoresSubstituidosIds.value }
                val timeVermelhoAtivos = _timeVermelhoAtual.value.filter { it.id !in _jogadoresSubstituidosIds.value }
                val todosParticipantes = (_timeBrancoAtual.value + _timeVermelhoAtual.value).distinctBy { it.id }

                when (vencedor) {
                    TimeColor.BRANCO -> {
                        _jogosConsecutivosTimeAtual.value++
                        jogadoresSaindo.addAll(todosParticipantes.filter { it.id !in timeBrancoAtivos.map { p -> p.id } })
                        
                        if (_jogosConsecutivosTimeAtual.value >= 2) {
                            jogadoresSaindo.addAll(timeBrancoAtivos)
                            _jogadoresUltimoTimeGanhador.value = emptyList()
                            _vencedorUltimoJogo.value = null
                            _jogosConsecutivosTimeAtual.value = 0
                        } else {
                            _jogadoresUltimoTimeGanhador.value = timeBrancoAtivos
                            _vencedorUltimoJogo.value = TimeColor.BRANCO
                        }
                    }
                    TimeColor.VERMELHO -> {
                        jogadoresSaindo.addAll(todosParticipantes.filter { it.id !in timeVermelhoAtivos.map { p -> p.id } })
                        _jogosConsecutivosTimeAtual.value = 1
                        _jogadoresUltimoTimeGanhador.value = timeVermelhoAtivos
                        _vencedorUltimoJogo.value = TimeColor.VERMELHO
                    }
                    null -> {
                        jogadoresSaindo.addAll(todosParticipantes)
                        _jogadoresUltimoTimeGanhador.value = emptyList()
                        _vencedorUltimoJogo.value = null
                        _jogosConsecutivosTimeAtual.value = 0
                    }
                }

                rotacionarJogadores(jogadoresSaindo.distinctBy { it.id })

                _numeroDoProximoJogo.value++
                atualizarDuracaoJogo()
                _jogoAtualId.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao finalizar jogo", e)
            }
        }
    }

    fun incrementarPlacarBranco() {
        _placarBranco.value++
        viewModelScope.launch {
            _jogoAtualId.value?.let { id ->
                jogoRepository.registrarGol(id, TimeColor.BRANCO)
            }
        }
    }

    fun incrementarPlacarVermelho() {
        _placarVermelho.value++
        viewModelScope.launch {
            _jogoAtualId.value?.let { id ->
                jogoRepository.registrarGol(id, TimeColor.VERMELHO)
            }
        }
    }

    fun iniciarNovoDia() {
        _sessaoAtual.value = SessaoJogos(data = System.currentTimeMillis())
        limparSessaoCompleta()
    }

    fun limparJogoAtual() {
        _jogoAtualId.value = null
        _timeBrancoAtual.value = emptyList()
        _timeVermelhoAtual.value = emptyList()
        _placarBranco.value = 0
        _placarVermelho.value = 0
        _jogadoresSubstituidosIds.value = emptySet()
    }

    fun limparSessaoCompleta() {
        _listaPresenca.value = emptyList()
        _numeroDoProximoJogo.value = 1
        _jogosConsecutivosTimeAtual.value = 0
        _jogadoresUltimoTimeGanhador.value = emptyList()
        _vencedorUltimoJogo.value = null
        limparJogoAtual()
    }
}
