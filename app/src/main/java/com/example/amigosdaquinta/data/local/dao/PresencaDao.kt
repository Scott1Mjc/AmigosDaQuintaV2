package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.PresencaDia
import com.example.amigosdaquinta.data.model.PresencaComJogador
import kotlinx.coroutines.flow.Flow

/**
 * Interface de acesso a dados para a tabela de presenças diárias.
 *
 * Gerencia o registro de entrada dos jogadores na sessão, controle de fila
 * e estatísticas de participação imediata para fins de rotação de times.
 */
@Dao
interface PresencaDao {

    /**
     * Retorna a lista de presenças ativas do dia, ordenada pela ordem de chegada.
     * Utilizado para formação de times e gerenciamento da fila de espera.
     */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1 
        ORDER BY ordemChegada ASC
    """)
    fun obterPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    /**
     * Retorna a lista de presenças com dados do jogador, ordenadas.
     */
    @Transaction
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1 
        ORDER BY ordemChegada ASC
    """)
    suspend fun obterPresencasComJogador(dataInicio: Long, dataFim: Long): List<PresencaComJogador>

    /**
     * Retorna todos os registros de presença de um dia específico (ativos e inativos).
     */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim 
        ORDER BY ordemChegada ASC
    """)
    fun obterTodasPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    /**
     * Busca um registro de presença pelo seu identificador único.
     */
    @Query("SELECT * FROM lista_presenca WHERE id = :id")
    suspend fun obterPorId(id: Long): PresencaDia?

    /**
     * Busca o registro de presença de um jogador em uma data específica.
     */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE jogadorId = :jogadorId AND data >= :dataInicio AND data <= :dataFim 
        LIMIT 1
    """)
    suspend fun obterPresencaPorJogadorEDia(
        jogadorId: Long,
        dataInicio: Long,
        dataFim: Long
    ): PresencaDia?

    /**
     * Retorna a maior ordem de chegada registrada no dia para cálculo do próximo da fila.
     */
    @Query("""
        SELECT MAX(ordemChegada) 
        FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim
    """)
    suspend fun obterUltimaOrdemChegada(dataInicio: Long, dataFim: Long): Int?

    /**
     * Conta a quantidade de jogadores ativos presentes na sessão.
     */
    @Query("""
        SELECT COUNT(*) 
        FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1
    """)
    suspend fun contarPresencasAtivas(dataInicio: Long, dataFim: Long): Int

    /**
     * Insere um novo registro de presença na lista do dia.
     */
    @Insert
    suspend fun inserir(presenca: PresencaDia): Long

    /**
     * Atualiza os dados de uma presença existente.
     */
    @Update
    suspend fun atualizar(presenca: PresencaDia)

    /**
     * Marca um jogador como inativo na sessão (exclusão lógica).
     */
    @Query("UPDATE lista_presenca SET ativo = 0 WHERE id = :id")
    suspend fun marcarComoInativo(id: Long)

    /**
     * Marca múltiplos jogadores como inativos na sessão atual.
     */
    @Query("""
        UPDATE lista_presenca SET ativo = 0 
        WHERE jogadorId IN (:jogadorIds) AND data >= :dataInicio AND data <= :dataFim
    """)
    suspend fun marcarJogadoresComoInativosNoDia(jogadorIds: List<Long>, dataInicio: Long, dataFim: Long)

    /**
     * Incrementa o contador de jogos participados por um atleta no dia.
     */
    @Query("UPDATE lista_presenca SET jogosParticipados = jogosParticipados + 1 WHERE id = :id")
    suspend fun incrementarJogosParticipados(id: Long)

    /**
     * Reseta o contador de jogos participados de um atleta.
     */
    @Query("UPDATE lista_presenca SET jogosParticipados = 0 WHERE id = :id")
    suspend fun resetarJogosConsecutivos(id: Long)

    /**
     * Remove permanentemente todos os registros de presença de todas as datas.
     */
    @Query("DELETE FROM lista_presenca")
    suspend fun deleteAll()

    /**
     * Remove permanentemente um registro de presença.
     */
    @Delete
    suspend fun deletar(presenca: PresencaDia)

    /**
     * Remove permanentemente todos os registros de presença de um dia específico.
     */
    @Query("DELETE FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim")
    suspend fun limparPresencasDoDia(dataInicio: Long, dataFim: Long)
}
