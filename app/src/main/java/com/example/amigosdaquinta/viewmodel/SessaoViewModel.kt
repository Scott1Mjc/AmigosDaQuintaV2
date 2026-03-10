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
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import com.example.amigosdaquinta.data.repository.PresencaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessaoViewModel(
    private val jogoRepository: JogoRepository,
    private val participacaoRepository: ParticipacaoRepository,
    private val presencaRepository: PresencaRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SessaoViewModel"
    }

    private val _sessaoAtual = MutableStateFlow<SessaoJogos?>(null)
    val sessaoAtual: StateFlow<SessaoJogos?> = _sessaoAtual.asStateFlow()

    private val _listaPresenca = MutableStateFlow<List<Pair<Jogador, Long>>>(emptyList())
    val listaPresenca: StateFlow<List<Pair<Jogador, Long>>> = _listaPresenca.asStateFlow()

    private val _filaEspera = MutableStateFlow<List<JogadorNaFila>>(emptyList())
    val filaEspera: StateFlow<List<JogadorNaFila>> = _filaEspera.asStateFlow()

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

    private fun atualizarDuracaoJogo() {
        _duracaoJogoAtualMinutos.value = if (_numeroDoProximoJogo.value == 1) 30 else 15
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
                // Ao re-adicionar no fim, atualizamos o timestamp para o final da fila
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
                // Garantir que todos os 22 estão na lista de presença
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
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar jogo", e)
            }
        }
    }

    fun finalizarJogo(vencedor: TimeColor?) {
        viewModelScope.launch {
            try {
                val jogoId = _jogoAtualId.value ?: return@launch
                
                // Persistir placar final antes de finalizar o status
                jogoRepository.atualizarJogo(
                    jogoRepository.obterPorId(jogoId)!!.copy(
                        placarBranco = _placarBranco.value,
                        placarVermelho = _placarVermelho.value
                    )
                )
                
                jogoRepository.finalizarJogo(id = jogoId, vencedor = vencedor)

                // Lógica de Rotação (quem sai vai pro fim da fila)
                val jogadoresSaindo = mutableListOf<Jogador>()
                
                when (vencedor) {
                    TimeColor.BRANCO -> {
                        _jogosConsecutivosTimeAtual.value++
                        jogadoresSaindo.addAll(_timeVermelhoAtual.value)
                        if (_jogosConsecutivosTimeAtual.value >= 2) {
                            jogadoresSaindo.addAll(_timeBrancoAtual.value)
                            _jogadoresUltimoTimeGanhador.value = emptyList()
                            _vencedorUltimoJogo.value = null
                            _jogosConsecutivosTimeAtual.value = 0
                        } else {
                            _jogadoresUltimoTimeGanhador.value = _timeBrancoAtual.value
                            _vencedorUltimoJogo.value = TimeColor.BRANCO
                        }
                    }
                    TimeColor.VERMELHO -> {
                        jogadoresSaindo.addAll(_timeBrancoAtual.value)
                        _jogosConsecutivosTimeAtual.value = 1
                        _jogadoresUltimoTimeGanhador.value = _timeVermelhoAtual.value
                        _vencedorUltimoJogo.value = TimeColor.VERMELHO
                    }
                    null -> {
                        jogadoresSaindo.addAll(_timeBrancoAtual.value)
                        jogadoresSaindo.addAll(_timeVermelhoAtual.value)
                        _jogadoresUltimoTimeGanhador.value = emptyList()
                        _vencedorUltimoJogo.value = null
                        _jogosConsecutivosTimeAtual.value = 0
                    }
                }

                // Move os perdedores (ou todos se empate/limite) para o fim da lista de presença
                rotacionarJogadores(jogadoresSaindo)

                _numeroDoProximoJogo.value++
                atualizarDuracaoJogo()
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
