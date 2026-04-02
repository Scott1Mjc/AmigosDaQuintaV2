package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.PresencaDao
import com.example.amigosdaquinta.data.local.entity.PresencaDia
import com.example.amigosdaquinta.data.model.PresencaComJogador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Repositório responsável pela gestão das presenças diárias dos jogadores.
 *
 * Controla a fila de chegada, a contagem de partidas disputadas por cada atleta no dia
 * e a persistência do histórico de presença por sessão.
 *
 * @property presencaDao Objeto de acesso a dados (DAO) da entidade PresencaDia.
 */
class PresencaRepository(private val presencaDao: PresencaDao) {

    /**
     * Retorna a lista de jogadores ativos na sessão de um dia específico.
     */
    fun obterPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>> =
        presencaDao.obterPresencasDoDia(dataInicio, dataFim)

    /**
     * Retorna a lista de presenças ordenadas com dados dos jogadores para o dia atual.
     */
    suspend fun obterPresencasOrdenadas(): List<PresencaComJogador> = withContext(Dispatchers.IO) {
        val (inicio, fim) = obterInicioEFimDoDia(System.currentTimeMillis())
        presencaDao.obterPresencasComJogador(inicio, fim)
    }

    /**
     * Retorna todos os registros de presença (ativos e inativos) de um dia.
     */
    fun obterTodasPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>> =
        presencaDao.obterTodasPresencasDoDia(dataInicio, dataFim)

    /**
     * Busca um registro de presença específico pelo ID.
     */
    suspend fun obterPorId(id: Long): PresencaDia? =
        presencaDao.obterPorId(id)

    /**
     * Busca a presença de um jogador em uma data específica.
     */
    suspend fun obterPresencaPorJogadorEDia(
        jogadorId: Long,
        dataInicio: Long,
        dataFim: Long
    ): PresencaDia? =
        presencaDao.obterPresencaPorJogadorEDia(jogadorId, dataInicio, dataFim)

    /**
     * Retorna a quantidade de jogadores que ainda estão ativos na sessão atual.
     */
    suspend fun contarPresencasAtivas(dataInicio: Long, dataFim: Long): Int =
        presencaDao.contarPresencasAtivas(dataInicio, dataFim)

    /**
     * Registra a chegada de um jogador na sessão.
     * Calcula automaticamente a ordem de chegada com base nos registros do dia.
     *
     * @param jogadorId ID do jogador que está chegando.
     * @param horarioChegada Timestamp do momento da chegada.
     * @return ID do novo registro de presença.
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
     * Remove todos os registros de presença de todas as datas.
     * Operação administrativa drástica.
     */
    suspend fun limparTodasPresencas() {
        withContext(Dispatchers.IO) {
            presencaDao.deleteAll()
        }
    }

    /**
     * Marca a saída de um jogador da sessão atual (inativação lógica).
     */
    suspend fun marcarComoInativo(presencaId: Long) =
        presencaDao.marcarComoInativo(presencaId)

    /**
     * Marca múltiplos jogadores como inativos na sessão atual.
     */
    suspend fun marcarJogadoresComoInativosNoDia(jogadorIds: List<Long>) = withContext(Dispatchers.IO) {
        val (inicio, fim) = obterInicioEFimDoDia(System.currentTimeMillis())
        presencaDao.marcarJogadoresComoInativosNoDia(jogadorIds, inicio, fim)
    }

    /**
     * Incrementa o contador de jogos que o atleta participou no dia.
     */
    suspend fun incrementarJogosParticipados(presencaId: Long) =
        presencaDao.incrementarJogosParticipados(presencaId)

    /**
     * Zera o contador de jogos participados de um jogador.
     */
    suspend fun resetarJogosConsecutivos(presencaId: Long) =
        presencaDao.resetarJogosConsecutivos(presencaId)

    /**
     * Remove todos os registros de presença de um dia específico.
     */
    suspend fun limparPresencasDoDia(dataInicio: Long, dataFim: Long) =
        presencaDao.limparPresencasDoDia(dataInicio, dataFim)

    /**
     * Utilitário para calcular os limites temporais (00:00:00 a 23:59:59) de um timestamp.
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
}
