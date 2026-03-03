package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * DAO de acesso aos dados de jogos.
 *
 * Operações de placar são feitas por campos individuais ([atualizarPlacarBranco],
 * [atualizarPlacarVermelho]) para evitar race conditions ao atualizar o objeto inteiro via [atualizar].
 *
 * [finalizarJogo] é a única operação que altera [timeVencedor] e [status] atomicamente,
 * garantindo consistência no encerramento de partidas.
 */
@Dao
interface JogoDao {

    // region Consultas reativas (Flow)

    /** Retorna os jogos de um intervalo de datas, ordenados do mais recente para o mais antigo. */
    @Query("SELECT * FROM jogos WHERE data BETWEEN :dataInicio AND :dataFim ORDER BY data DESC, numeroJogo DESC")
    fun obterJogosDoDia(dataInicio: Long, dataFim: Long): Flow<List<Jogo>>

    /** Retorna todos os jogos de todas as sessões, do mais recente para o mais antigo. */
    @Query("SELECT * FROM jogos ORDER BY data DESC, numeroJogo DESC")
    fun obterTodos(): Flow<List<Jogo>>

    // endregion

    // region Consultas suspend

    @Query("SELECT * FROM jogos WHERE id = :id")
    suspend fun obterPorId(id: Long): Jogo?

    @Query("SELECT * FROM jogos WHERE id IN (:ids)")
    suspend fun obterPorIds(ids: List<Long>): List<Jogo>

    /** Retorna o jogo mais recente com o status informado. Útil para localizar jogo em andamento. */
    @Query("SELECT * FROM jogos WHERE status = :status ORDER BY data DESC LIMIT 1")
    suspend fun obterJogoPorStatus(status: StatusJogo): Jogo?

    /** Retorna o último jogo registrado no dia (maior [numeroJogo]). */
    @Query("SELECT * FROM jogos WHERE data >= :dataInicio AND data <= :dataFim ORDER BY numeroJogo DESC LIMIT 1")
    suspend fun obterUltimoJogoDoDia(dataInicio: Long, dataFim: Long): Jogo?

    @Query("SELECT COUNT(*) FROM jogos WHERE data >= :dataInicio AND data <= :dataFim")
    suspend fun contarJogosDoDia(dataInicio: Long, dataFim: Long): Int

    // endregion

    // region Escrita

    @Insert
    suspend fun inserir(jogo: Jogo): Long

    @Update
    suspend fun atualizar(jogo: Jogo)

    @Delete
    suspend fun deletar(jogo: Jogo)

    @Query("UPDATE jogos SET status = :status WHERE id = :id")
    suspend fun atualizarStatus(id: Long, status: StatusJogo)

    @Query("UPDATE jogos SET placarBranco = :placar WHERE id = :id")
    suspend fun atualizarPlacarBranco(id: Long, placar: Int)

    @Query("UPDATE jogos SET placarVermelho = :placar WHERE id = :id")
    suspend fun atualizarPlacarVermelho(id: Long, placar: Int)

    /**
     * Encerra o jogo: define o [vencedor] e altera o [status] para FINALIZADO atomicamente.
     * Passar null em [vencedor] representa empate.
     */
    @Query("UPDATE jogos SET timeVencedor = :vencedor, status = :status WHERE id = :id")
    suspend fun finalizarJogo(id: Long, vencedor: TimeColor?, status: StatusJogo)

    // endregion
}