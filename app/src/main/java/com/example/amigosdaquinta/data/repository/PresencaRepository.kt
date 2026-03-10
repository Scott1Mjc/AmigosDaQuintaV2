package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.PresencaDao
import com.example.amigosdaquinta.data.local.entity.PresencaDia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Repository responsável pelo registro e consulta de presenças diárias dos jogadores.
 *
 * Cada presença representa a chegada de um jogador em um dia de pelada,
 * com controle de ordem de chegada, status ativo/inativo e contagem de jogos participados.
 *
 * A ordem de chegada é calculada automaticamente com base no último registro do mesmo dia,
 * garantindo sequência incremental mesmo com inserções concorrentes.
 */
class PresencaRepository(private val presencaDao: PresencaDao) {

    // region Fluxos reativos

    fun obterPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>> =
        presencaDao.obterPresencasDoDia(dataInicio, dataFim)

    fun obterTodasPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>> =
        presencaDao.obterTodasPresencasDoDia(dataInicio, dataFim)

    // endregion

    // region Consultas suspend

    suspend fun obterPorId(id: Long): PresencaDia? =
        presencaDao.obterPorId(id)

    suspend fun obterPresencaPorJogadorEDia(
        jogadorId: Long,
        dataInicio: Long,
        dataFim: Long
    ): PresencaDia? =
        presencaDao.obterPresencaPorJogadorEDia(jogadorId, dataInicio, dataFim)

    suspend fun contarPresencasAtivas(dataInicio: Long, dataFim: Long): Int =
        presencaDao.contarPresencasAtivas(dataInicio, dataFim)

    // endregion

    // region Escrita

    /**
     * Registra a presença de um jogador no dia correspondente ao [horarioChegada].
     *
     * A ordem de chegada é definida automaticamente como (última ordem do dia + 1).
     * Se nenhum registro existir no dia, a ordem começa em 1.
     *
     * @return ID do registro de presença inserido.
     */
    suspend fun registrarPresenca(jogadorId: Long, horarioChegada: Long): Long {
        val (inicio, fim) = obterInicioEFimDoDia(horarioChegada)
        val ultimaOrdem = presencaDao.obterUltimaOrdemChegada(inicio, fim) ?: 0

        val presenca = PresencaDia(
            jogadorId = jogadorId,
            data = horarioChegada,
            horarioChegada = horarioChegada,
            ordemChegada = ultimaOrdem + 1
        )

        return presencaDao.inserir(presenca)
    }

    /**
     * Remove todos os registros de presença do banco.
     * Usado ao resetar sessão para novo dia.
     */
    suspend fun limparTodasPresencas() {
        withContext(Dispatchers.IO) {
            presencaDao.deleteAll()
        }
    }


    suspend fun marcarComoInativo(presencaId: Long) =
        presencaDao.marcarComoInativo(presencaId)

    suspend fun incrementarJogosParticipados(presencaId: Long) =
        presencaDao.incrementarJogosParticipados(presencaId)

    suspend fun resetarJogosConsecutivos(presencaId: Long) =
        presencaDao.resetarJogosConsecutivos(presencaId)

    suspend fun limparPresencasDoDia(dataInicio: Long, dataFim: Long) =
        presencaDao.limparPresencasDoDia(dataInicio, dataFim)

    // endregion

    // region Utilitários privados

    /**
     * Calcula o início (00:00:00.000) e fim (23:59:59.999) do dia
     * a partir de um timestamp arbitrário, respeitando o timezone local.
     */
    private fun obterInicioEFimDoDia(timestamp: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val inicio = calendar.timeInMillis

        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val fim = calendar.timeInMillis

        return Pair(inicio, fim)
    }

    // endregion
}