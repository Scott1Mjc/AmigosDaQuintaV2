package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.PresencaDia
import com.example.amigosdaquinta.data.model.PresencaComJogador
import kotlinx.coroutines.flow.Flow

/**
 * Interface de acesso a dados para a tabela de presenças diárias.
 */
@Dao
interface PresencaDao {

    /**
     * Retorna a lista de presenças com dados do jogador, ordenadas e REATIVAS.
     * Atualizada para prover Flow, garantindo que qualquer mudança no banco reflita na UI.
     */
    @Transaction
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1 
        ORDER BY ordemChegada ASC
    """)
    fun obterPresencasComJogadorFlow(dataInicio: Long, dataFim: Long): Flow<List<PresencaComJogador>>

    @Transaction
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1 
        ORDER BY ordemChegada ASC
    """)
    suspend fun obterPresencasComJogador(dataInicio: Long, dataFim: Long): List<PresencaComJogador>

    @Query("SELECT * FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1")
    fun obterPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    @Query("SELECT * FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim")
    fun obterTodasPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    @Query("SELECT * FROM lista_presenca WHERE id = :id")
    suspend fun obterPorId(id: Long): PresencaDia?

    @Query("""
        SELECT * FROM lista_presenca 
        WHERE jogadorId = :jogadorId AND data >= :dataInicio AND data <= :dataFim 
        LIMIT 1
    """)
    suspend fun obterPresencaPorJogadorEDia(jogadorId: Long, dataInicio: Long, dataFim: Long): PresencaDia?

    @Query("""
        SELECT COUNT(*) FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1
    """)
    suspend fun contarPresencasAtivas(dataInicio: Long, dataFim: Long): Int

    @Query("""
        SELECT MAX(ordemChegada) 
        FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim
    """)
    suspend fun obterUltimaOrdemChegada(dataInicio: Long, dataFim: Long): Int?

    @Insert
    suspend fun inserir(presenca: PresencaDia): Long

    @Update
    suspend fun atualizar(presenca: PresencaDia)

    @Query("UPDATE lista_presenca SET ativo = 0 WHERE id = :id")
    suspend fun marcarComoInativo(id: Long)

    @Query("""
        UPDATE lista_presenca SET ativo = 0 
        WHERE jogadorId IN (:jogadorIds) AND data >= :dataInicio AND data <= :dataFim
    """)
    suspend fun marcarJogadoresComoInativosNoDia(jogadorIds: List<Long>, dataInicio: Long, dataFim: Long)

    @Query("UPDATE lista_presenca SET jogosParticipados = jogosParticipados + 1 WHERE id = :presencaId")
    suspend fun incrementarJogosParticipados(presencaId: Long)

    @Query("UPDATE lista_presenca SET jogosParticipados = 0 WHERE id = :presencaId")
    suspend fun resetarJogosConsecutivos(presencaId: Long)

    @Query("DELETE FROM lista_presenca")
    suspend fun deleteAll()

    @Query("DELETE FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim")
    suspend fun limparPresencasDoDia(dataInicio: Long, dataFim: Long)
}
