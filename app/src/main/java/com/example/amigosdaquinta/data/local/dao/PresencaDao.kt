package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.PresencaDia
import kotlinx.coroutines.flow.Flow

/**
 * DAO de acesso à tabela [lista_presenca].
 *
 * Cada registro representa a chegada de um jogador em um dia de sessão.
 * O campo [ativo] permite inativação lógica sem perda de histórico — jogadores
 * que vão embora durante a sessão são marcados como inativos em vez de deletados.
 *
 * [obterPresencasDoDia] retorna apenas ativos; [obterTodasPresencasDoDia] retorna todos,
 * inclusive inativos — útil para relatórios e auditoria da sessão.
 *
 * [resetarJogosConsecutivos] zera o campo [jogosParticipados], que é usado pelo
 * FormadorDeTimes para controlar rotação de jogadores após dois jogos seguidos.
 */
@Dao
interface PresencaDao {

    // region Consultas reativas (Flow)

    /** Retorna presenças ativas do dia, ordenadas pela ordem de chegada. */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1 
        ORDER BY ordemChegada ASC
    """)
    fun obterPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    /** Retorna todas as presenças do dia (ativas e inativas), ordenadas pela ordem de chegada. */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim 
        ORDER BY ordemChegada ASC
    """)
    fun obterTodasPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    // endregion

    // region Consultas suspend

    @Query("SELECT * FROM lista_presenca WHERE id = :id")
    suspend fun obterPorId(id: Long): PresencaDia?

    @Query("""
        SELECT * FROM lista_presenca 
        WHERE jogadorId = :jogadorId AND data >= :dataInicio AND data <= :dataFim 
        LIMIT 1
    """)
    suspend fun obterPresencaPorJogadorEDia(jogadorId: Long, dataInicio: Long, dataFim: Long): PresencaDia?

    /**
     * Retorna o maior valor de [ordemChegada] registrado no dia.
     * Usado pelo Repository para calcular a próxima ordem de chegada de forma sequencial.
     * Retorna null se nenhuma presença foi registrada ainda no dia.
     */
    @Query("SELECT MAX(ordemChegada) FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim")
    suspend fun obterUltimaOrdemChegada(dataInicio: Long, dataFim: Long): Int?

    @Query("SELECT COUNT(*) FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1")
    suspend fun contarPresencasAtivas(dataInicio: Long, dataFim: Long): Int

    // endregion

    // region Escrita

    @Insert
    suspend fun inserir(presenca: PresencaDia): Long

    @Update
    suspend fun atualizar(presenca: PresencaDia)

    /** Inativação lógica: o jogador deixa de aparecer na fila, mas o registro é preservado. */
    @Query("UPDATE lista_presenca SET ativo = 0 WHERE id = :id")
    suspend fun marcarComoInativo(id: Long)

    @Query("UPDATE lista_presenca SET jogosParticipados = jogosParticipados + 1 WHERE id = :id")
    suspend fun incrementarJogosParticipados(id: Long)

    /**
     * Zera o contador de jogos participados.
     * Chamado pelo FormadorDeTimes quando o jogador sai por rotação após dois jogos consecutivos.
     */
    @Query("UPDATE lista_presenca SET jogosParticipados = 0 WHERE id = :id")
    suspend fun resetarJogosConsecutivos(id: Long)

    @Delete
    suspend fun deletar(presenca: PresencaDia)

    /** Deleção física de todos os registros do dia. Usar apenas ao encerrar/resetar a sessão. */
    @Query("DELETE FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim")
    suspend fun limparPresencasDoDia(dataInicio: Long, dataFim: Long)

    // endregion
}