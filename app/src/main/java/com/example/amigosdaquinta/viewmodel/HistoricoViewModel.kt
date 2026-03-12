package com.example.amigosdaquinta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.data.repository.JogadorRepository
import com.example.amigosdaquinta.data.repository.JogoRepository
import com.example.amigosdaquinta.data.repository.ParticipacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel responsável pelo histórico de jogos e estatísticas.
 */
class HistoricoViewModel(
    private val jogoRepository: JogoRepository,
    private val participacaoRepository: ParticipacaoRepository,
    private val jogadorRepository: JogadorRepository
) : ViewModel() {

    private val _jogos = MutableStateFlow<List<Jogo>>(emptyList())
    val jogos: StateFlow<List<Jogo>> = _jogos.asStateFlow()

    private val _jogoDetalhes = MutableStateFlow<JogoDetalhes?>(null)
    val jogoDetalhes: StateFlow<JogoDetalhes?> = _jogoDetalhes.asStateFlow()

    private val _estatisticasJogador = MutableStateFlow<EstatisticasJogador?>(null)
    val estatisticasJogador: StateFlow<EstatisticasJogador?> = _estatisticasJogador.asStateFlow()

    fun obterJogosPorData(data: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = data
            calendar.add(Calendar.DAY_OF_MONTH, -30)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicio = calendar.timeInMillis

            calendar.timeInMillis = data
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val fim = calendar.timeInMillis

            jogoRepository.obterJogosDoDia(inicio, fim).collect { lista ->
                _jogos.value = lista
            }
        }
    }

    fun obterDetalhesJogo(jogoId: Long) {
        viewModelScope.launch {
            val jogo = jogoRepository.obterJogoPorId(jogoId) ?: return@launch
            val participacoes = participacaoRepository.obterPorJogo(jogoId)

            val jogadoresIds = participacoes.map { it.jogadorId }
            val jogadores = jogadorRepository.obterPorIds(jogadoresIds)
            val jogadoresMap = jogadores.associateBy { it.id }

            val timeBranco = participacoes
                .filter { it.time == TimeColor.BRANCO }
                .mapNotNull { part -> 
                    jogadoresMap[part.jogadorId]?.let { 
                        JogadorParticipacao(it, part.foiSubstituido, part.entrouComoSubstituto) 
                    } 
                }

            val timeVermelho = participacoes
                .filter { it.time == TimeColor.VERMELHO }
                .mapNotNull { part -> 
                    jogadoresMap[part.jogadorId]?.let { 
                        JogadorParticipacao(it, part.foiSubstituido, part.entrouComoSubstituto) 
                    } 
                }

            _jogoDetalhes.value = JogoDetalhes(
                jogo = jogo,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho
            )
        }
    }

    fun obterEstatisticasJogador(jogadorId: Long) {
        viewModelScope.launch {
            val jogador = jogadorRepository.obterPorId(jogadorId) ?: return@launch
            val participacoes = participacaoRepository.obterPorJogador(jogadorId)

            val jogosIds = participacoes.map { it.jogoId }
            val jogos = jogoRepository.obterPorIds(jogosIds)
            val jogosMap = jogos.associateBy { it.id }

            var vitorias = 0
            var derrotas = 0
            var empates = 0

            participacoes.forEach { participacao ->
                val jogo = jogosMap[participacao.jogoId]
                if (jogo != null) {
                    when {
                        jogo.timeVencedor == null -> empates++
                        jogo.timeVencedor == participacao.time -> vitorias++
                        else -> derrotas++
                    }
                }
            }

            val ultimosJogos = participacoes
                .sortedByDescending { it.jogoId }
                .take(5)
                .mapNotNull { jogosMap[it.jogoId] }

            _estatisticasJogador.value = EstatisticasJogador(
                jogador = jogador,
                totalJogos = participacoes.size,
                vitorias = vitorias,
                derrotas = derrotas,
                empates = empates,
                ultimosJogos = ultimosJogos
            )
        }
    }
}

data class JogadorParticipacao(
    val jogador: Jogador,
    val foiSubstituido: Boolean,
    val entrouComoSubstituto: Boolean
)

data class JogoDetalhes(
    val jogo: Jogo,
    val timeBranco: List<JogadorParticipacao>,
    val timeVermelho: List<JogadorParticipacao>
)

data class EstatisticasJogador(
    val jogador: Jogador,
    val totalJogos: Int,
    val vitorias: Int,
    val derrotas: Int,
    val empates: Int,
    val ultimosJogos: List<Jogo>
)
