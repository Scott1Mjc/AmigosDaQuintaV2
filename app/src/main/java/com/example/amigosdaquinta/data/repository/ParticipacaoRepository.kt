package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.ParticipacaoDao
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsável pelas operações de participação dos jogadores nos jogos.
 */
class ParticipacaoRepository(private val participacaoDao: ParticipacaoDao) {

    fun obterParticipacoesPorJogo(jogoId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogo(jogoId)

    fun obterParticipacoesPorJogoETime(jogoId: Long, time: TimeColor): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogoETime(jogoId, time)

    fun obterParticipacoesPorJogador(jogadorId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogador(jogadorId)

    suspend fun obterPorJogo(jogoId: Long): List<Participacao> =
        participacaoDao.obterPorJogo(jogoId)

    suspend fun obterPorJogador(jogadorId: Long): List<Participacao> =
        participacaoDao.obterPorJogador(jogadorId)

    suspend fun contarJogosDoDia(jogadorId: Long, dataInicio: Long, dataFim: Long): Int =
        participacaoDao.contarJogosDoDia(jogadorId, dataInicio, dataFim)

    suspend fun obterTotalGols(jogadorId: Long): Int =
        participacaoDao.obterTotalGols(jogadorId) ?: 0

    suspend fun obterTotalAssistencias(jogadorId: Long): Int =
        participacaoDao.obterTotalAssistencias(jogadorId) ?: 0

    suspend fun inserir(participacao: Participacao): Long =
        participacaoDao.inserir(participacao)

    suspend fun inserirVarias(participacoes: List<Participacao>) =
        participacaoDao.inserirVarias(participacoes)

    suspend fun marcarComoSubstituido(jogoId: Long, jogadorId: Long) =
        participacaoDao.marcarComoSubstituido(jogoId, jogadorId)

    suspend fun registrarGol(participacaoId: Long) =
        participacaoDao.incrementarGol(participacaoId)

    suspend fun registrarAssistencia(participacaoId: Long) =
        participacaoDao.incrementarAssistencia(participacaoId)
}
