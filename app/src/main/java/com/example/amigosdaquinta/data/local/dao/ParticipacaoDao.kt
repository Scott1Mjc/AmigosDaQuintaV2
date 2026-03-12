package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * Interface de acesso a dados para a entidade Participacao.
 * 
 * Gerencia os registros de atuação dos jogadores, incluindo contagem de gols,
 * assistências e controle de substituições durante as partidas.
 */
@Dao
interface ParticipacaoDao {

    /** Retorna as participações de um jogo via Flow. */
    @Query("SELECT * FROM participacoes WHERE jogoId = :jogoId")
    fun obterParticipacoesPorJogo(jogoId: Long): Flow<List<Participacao>>

    /** Retorna as participações filtradas por time em um jogo. */
    @Query("SELECT * FROM participacoes WHERE jogoId = :jogoId AND time = :time")
    fun obterParticipacoesPorJogoETime(jogoId: Long, time: TimeColor): Flow<List<Participacao>>

    /** Retorna o histórico de participações de um jogador via Flow. */
    @Query("SELECT * FROM participacoes WHERE jogadorId = :jogadorId ORDER BY jogoId DESC")
    fun obterParticipacoesPorJogador(jogadorId: Long): Flow<List<Participacao>>

    /** Busca participações de um jogo (suspend). */
    @Query("SELECT * FROM participacoes WHERE jogoId = :jogoId")
    suspend fun obterPorJogo(jogoId: Long): List<Participacao>

    /** Busca participações de um jogador (suspend). */
    @Query("SELECT * FROM participacoes WHERE jogadorId = :jogadorId ORDER BY jogoId DESC")
    suspend fun obterPorJogador(jogadorId: Long): List<Participacao>

    /** Conta participações de um jogador em um intervalo de tempo. */
    @Query("""
        SELECT COUNT(*) FROM participacoes 
        WHERE jogadorId = :jogadorId 
        AND jogoId IN (SELECT id FROM jogos WHERE data >= :dataInicio AND data <= :dataFim)
    """)
    suspend fun contarJogosDoDia(jogadorId: Long, dataInicio: Long, dataFim: Long): Int

    /** Retorna a soma total de gols de um atleta. */
    @Query("SELECT SUM(gols) FROM participacoes WHERE jogadorId = :jogadorId")
    suspend fun obterTotalGols(jogadorId: Long): Int?

    /** Retorna a soma total de assistências de um atleta. */
    @Query("SELECT SUM(assistencias) FROM participacoes WHERE jogadorId = :jogadorId")
    suspend fun obterTotalAssistencias(jogadorId: Long): Int?

    /** Insere uma nova participação. */
    @Insert
    suspend fun inserir(participacao: Participacao): Long

    /** Insere múltiplas participações de uma vez. */
    @Insert
    suspend fun inserirVarias(participacoes: List<Participacao>)

    /** Atualiza dados de uma participação. */
    @Update
    suspend fun atualizar(participacao: Participacao)

    /** Marca a saída de um jogador por substituição. */
    @Query("UPDATE participacoes SET foiSubstituido = 1 WHERE jogoId = :jogoId AND jogadorId = :jogadorId")
    suspend fun marcarComoSubstituido(jogoId: Long, jogadorId: Long)

    /** Incrementa o contador de gols de um atleta em uma partida. */
    @Query("UPDATE participacoes SET gols = gols + 1 WHERE id = :id")
    suspend fun incrementarGol(id: Long)

    /** Incrementa o contador de assistências de um atleta em uma partida. */
    @Query("UPDATE participacoes SET assistencias = assistencias + 1 WHERE id = :id")
    suspend fun incrementarAssistencia(id: Long)

    /** Remove uma participação do banco. */
    @Delete
    suspend fun deletar(participacao: Participacao)

    /** Remove todas as participações vinculadas a um jogo. */
    @Query("DELETE FROM participacoes WHERE jogoId = :jogoId")
    suspend fun deletarPorJogo(jogoId: Long)
}
