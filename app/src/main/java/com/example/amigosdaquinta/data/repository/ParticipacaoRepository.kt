package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.ParticipacaoDao
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * Repositório responsável pela gestão das participações e desempenho dos jogadores.
 *
 * Registra dados como gols, assistências e substituições realizadas durante
 * uma partida específica do sistema Amigos da Quinta.
 *
 * @property participacaoDao Objeto de acesso a dados (DAO) da entidade Participacao.
 */
class ParticipacaoRepository(private val participacaoDao: ParticipacaoDao) {

    /**
     * Retorna a lista de participações individuais de uma partida específica.
     */
    fun obterParticipacoesPorJogo(jogoId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogo(jogoId)

    /**
     * Retorna a lista de participações de um time (BRANCO ou VERMELHO) em uma partida.
     */
    fun obterParticipacoesPorJogoETime(jogoId: Long, time: TimeColor): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogoETime(jogoId, time)

    /**
     * Retorna o histórico de todas as participações registradas de um jogador.
     */
    fun obterParticipacoesPorJogador(jogadorId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogador(jogadorId)

    /**
     * Retorna a lista de participações em uma partida de forma imediata (suspend).
     */
    suspend fun obterPorJogo(jogoId: Long): List<Participacao> =
        participacaoDao.obterPorJogo(jogoId)

    /**
     * Retorna a lista de participações de um jogador em todas as suas partidas.
     */
    suspend fun obterPorJogador(jogadorId: Long): List<Participacao> =
        participacaoDao.obterPorJogador(jogadorId)

    /**
     * Retorna o total de gols marcados acumulado de um jogador em toda a sua jornada.
     */
    suspend fun obterTotalGols(jogadorId: Long): Int =
        participacaoDao.obterTotalGols(jogadorId) ?: 0

    /**
     * Retorna o total de assistências acumulado de um jogador em toda a sua jornada.
     */
    suspend fun obterTotalAssistencias(jogadorId: Long): Int =
        participacaoDao.obterTotalAssistencias(jogadorId) ?: 0

    /**
     * Cadastra uma participação individual.
     */
    suspend fun inserir(participacao: Participacao): Long =
        participacaoDao.inserir(participacao)

    /**
     * Cadastra múltiplas participações em uma única operação.
     */
    suspend fun inserirVarias(participacoes: List<Participacao>) =
        participacaoDao.inserirVarias(participacoes)

    /**
     * Registra que um jogador foi substituído durante a partida.
     */
    suspend fun marcarComoSubstituido(jogoId: Long, jogadorId: Long) =
        participacaoDao.marcarComoSubstituido(jogoId, jogadorId)

    /**
     * Incrementa o contador de gols do jogador em uma partida específica.
     */
    suspend fun registrarGol(participacaoId: Long) =
        participacaoDao.incrementarGol(participacaoId)

    /**
     * Incrementa o contador de assistências do jogador em uma partida específica.
     */
    suspend fun registrarAssistencia(participacaoId: Long) =
        participacaoDao.incrementarAssistencia(participacaoId)
}
