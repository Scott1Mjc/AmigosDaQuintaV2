package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.ParticipacaoDao
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsável pelas operações de participação dos jogadores nos jogos.
 *
 * Centraliza o acesso ao [ParticipacaoDao], expondo tanto fluxos reativos (Flow)
 * para observação de listas quanto funções suspend para operações pontuais de leitura e escrita.
 *
 * Estatísticas agregadas (gols, assistências) retornam 0 quando nenhum registro é encontrado,
 * pois o DAO pode retornar null em queries de SUM sem resultados.
 */
class ParticipacaoRepository(private val participacaoDao: ParticipacaoDao) {

    // region Fluxos reativos

    fun obterParticipacoesPorJogo(jogoId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogo(jogoId)

    fun obterParticipacoesPorJogoETime(jogoId: Long, time: TimeColor): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogoETime(jogoId, time)

    fun obterParticipacoesPorJogador(jogadorId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogador(jogadorId)

    // endregion

    // region Consultas suspend

    /**
     * Retorna a lista de participações de um jogo de forma pontual (sem Flow).
     * Usado quando o resultado é necessário imediatamente, sem observação contínua.
     */
    suspend fun obterPorJogo(jogoId: Long): List<Participacao> =
        participacaoDao.obterPorJogo(jogoId)

    suspend fun obterPorJogador(jogadorId: Long): List<Participacao> =
        participacaoDao.obterPorJogador(jogadorId)

    /**
     * Conta quantos jogos o jogador participou em um intervalo de datas (início e fim do dia).
     */
    suspend fun contarJogosDoDia(jogadorId: Long, dataInicio: Long, dataFim: Long): Int =
        participacaoDao.contarJogosDoDia(jogadorId, dataInicio, dataFim)

    /** Retorna o total de gols do jogador em toda a sua história. Retorna 0 se sem registros. */
    suspend fun obterTotalGols(jogadorId: Long): Int =
        participacaoDao.obterTotalGols(jogadorId) ?: 0

    /** Retorna o total de assistências do jogador em toda a sua história. Retorna 0 se sem registros. */
    suspend fun obterTotalAssistencias(jogadorId: Long): Int =
        participacaoDao.obterTotalAssistencias(jogadorId) ?: 0

    // endregion

    // region Escrita

    suspend fun inserir(participacao: Participacao): Long =
        participacaoDao.inserir(participacao)

    suspend fun inserirVarias(participacoes: List<Participacao>) =
        participacaoDao.inserirVarias(participacoes)

    suspend fun registrarGol(participacaoId: Long) =
        participacaoDao.incrementarGol(participacaoId)

    suspend fun registrarAssistencia(participacaoId: Long) =
        participacaoDao.incrementarAssistencia(participacaoId)

}