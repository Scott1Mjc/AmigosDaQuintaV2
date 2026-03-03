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
 *
 * Funcionalidades:
 * - Listar jogos dos últimos 30 dias
 * - Exibir detalhes de um jogo específico
 * - Calcular estatísticas de um jogador
 *
 * @property jogoRepository Repository de jogos
 * @property participacaoRepository Repository de participações
 * @property jogadorRepository Repository de jogadores
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

    /**
     * Obtém jogos dos últimos 30 dias a partir da data fornecida.
     * Retorna jogos ordenados do mais recente para o mais antigo.
     *
     * @param data Data de referência (geralmente hoje)
     */
    fun obterJogosPorData(data: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()

            // Data inicial: 30 dias atrás
            calendar.timeInMillis = data
            calendar.add(Calendar.DAY_OF_MONTH, -30)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicio = calendar.timeInMillis

            // Data final: hoje às 23:59
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

    /**
     * Obtém os detalhes completos de um jogo específico.
     * Inclui informações do jogo e listas de jogadores de ambos os times.
     *
     * @param jogoId ID do jogo
     */
    fun obterDetalhesJogo(jogoId: Long) {
        viewModelScope.launch {
            val jogo = jogoRepository.obterJogoPorId(jogoId) ?: return@launch
            val participacoes = participacaoRepository.obterPorJogo(jogoId)

            // Buscar todos os jogadores que participaram
            val jogadoresIds = participacoes.map { it.jogadorId }
            val jogadores = jogadorRepository.obterPorIds(jogadoresIds)
            val jogadoresMap = jogadores.associateBy { it.id }

            // Separar por time
            val timeBranco = participacoes
                .filter { it.time == TimeColor.BRANCO }
                .mapNotNull { jogadoresMap[it.jogadorId] }

            val timeVermelho = participacoes
                .filter { it.time == TimeColor.VERMELHO }
                .mapNotNull { jogadoresMap[it.jogadorId] }

            _jogoDetalhes.value = JogoDetalhes(
                jogo = jogo,
                timeBranco = timeBranco,
                timeVermelho = timeVermelho
            )
        }
    }

    /**
     * Calcula as estatísticas de um jogador.
     * Inclui: total de jogos, vitórias, derrotas, empates e últimos 5 jogos.
     *
     * @param jogadorId ID do jogador
     */
    fun obterEstatisticasJogador(jogadorId: Long) {
        viewModelScope.launch {
            val jogador = jogadorRepository.obterPorId(jogadorId) ?: return@launch
            val participacoes = participacaoRepository.obterPorJogador(jogadorId)

            // Buscar todos os jogos que o jogador participou
            val jogosIds = participacoes.map { it.jogoId }
            val jogos = jogoRepository.obterPorIds(jogosIds)
            val jogosMap = jogos.associateBy { it.id }

            // Calcular estatísticas
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

            // Últimos 5 jogos (mais recentes primeiro)
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

/**
 * Data class para detalhes de um jogo.
 * Contém o jogo e as listas de jogadores de cada time.
 */
data class JogoDetalhes(
    val jogo: Jogo,
    val timeBranco: List<Jogador>,
    val timeVermelho: List<Jogador>
)

/**
 * Data class para estatísticas de um jogador.
 * Contém métricas de desempenho e histórico recente.
 */
data class EstatisticasJogador(
    val jogador: Jogador,
    val totalJogos: Int,
    val vitorias: Int,
    val derrotas: Int,
    val empates: Int,
    val ultimosJogos: List<Jogo>
)