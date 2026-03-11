package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * DAO de acesso às participações dos jogadores em cada jogo.
 */
@Dao
interface ParticipacaoDao {

    @Query("SELECT * FROM participacoes WHERE jogoId = :jogoId")
    fun obterParticipacoesPorJogo(jogoId: Long): Flow<List<Participacao>>

    @Query("SELECT * FROM participacoes WHERE jogoId = :jogoId AND time = :time")
    fun obterParticipacoesPorJogoETime(jogoId: Long, time: TimeColor): Flow<List<Participacao>>

    @Query("SELECT * FROM participacoes WHERE jogadorId = :jogadorId ORDER BY jogoId DESC")
    fun obterParticipacoesPorJogador(jogadorId: Long): Flow<List<Participacao>>

    @Query("SELECT * FROM participacoes WHERE jogoId = :jogoId")
    suspend fun obterPorJogo(jogoId: Long): List<Participacao>

    @Query("SELECT * FROM participacoes WHERE jogadorId = :jogadorId ORDER BY jogoId DESC")
    suspend fun obterPorJogador(jogadorId: Long): List<Participacao>

    @Query("""
        SELECT COUNT(*) FROM participacoes 
        WHERE jogadorId = :jogadorId 
        AND jogoId IN (
            SELECT id FROM jogos 
            WHERE data >= :dataInicio AND data <= :dataFim
        )
    """)
    suspend fun contarJogosDoDia(jogadorId: Long, dataInicio: Long, dataFim: Long): Int

    @Query("SELECT SUM(gols) FROM participacoes WHERE jogadorId = :jogadorId")
    suspend fun obterTotalGols(jogadorId: Long): Int?

    @Query("SELECT SUM(assistencias) FROM participacoes WHERE jogadorId = :jogadorId")
    suspend fun obterTotalAssistencias(jogadorId: Long): Int?

    @Insert
    suspend fun inserir(participacao: Participacao): Long

    @Insert
    suspend fun inserirVarias(participacoes: List<Participacao>)

    @Update
    suspend fun atualizar(participacao: Participacao)

    @Query("UPDATE participacoes SET foiSubstituido = 1 WHERE jogoId = :jogoId AND jogadorId = :jogadorId")
    suspend fun marcarComoSubstituido(jogoId: Long, jogadorId: Long)

    @Query("UPDATE participacoes SET gols = gols + 1 WHERE id = :id")
    suspend fun incrementarGol(id: Long)

    @Query("UPDATE participacoes SET assistencias = assistencias + 1 WHERE id = :id")
    suspend fun incrementarAssistencia(id: Long)

    @Delete
    suspend fun deletar(participacao: Participacao)

    @Query("DELETE FROM participacoes WHERE jogoId = :jogoId")
    suspend fun deletarPorJogo(jogoId: Long)
}
