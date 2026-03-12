package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * Interface de acesso a dados para a entidade Jogo.
 *
 * Gerencia a persistência das partidas, permitindo atualizações parciais de placar
 * para evitar conflitos de concorrência e garantindo a integridade do status do jogo.
 */
@Dao
interface JogoDao {

    /**
     * Retorna um fluxo reativo de jogos realizados em um intervalo de tempo.
     * Ordenado cronologicamente de forma decrescente.
     */
    @Query("SELECT * FROM jogos WHERE data BETWEEN :dataInicio AND :dataFim ORDER BY data DESC, numeroJogo DESC")
    fun obterJogosDoDia(dataInicio: Long, dataFim: Long): Flow<List<Jogo>>

    /**
     * Retorna um fluxo reativo de todos os jogos registrados.
     */
    @Query("SELECT * FROM jogos ORDER BY data DESC, numeroJogo DESC")
    fun obterTodos(): Flow<List<Jogo>>

    /**
     * Busca uma partida específica pelo seu ID.
     */
    @Query("SELECT * FROM jogos WHERE id = :id")
    suspend fun obterPorId(id: Long): Jogo?

    /**
     * Busca uma lista de partidas pelos seus IDs.
     */
    @Query("SELECT * FROM jogos WHERE id IN (:ids)")
    suspend fun obterPorIds(ids: List<Long>): List<Jogo>

    /**
     * Retorna a partida mais recente com o status informado (ex: EM_ANDAMENTO).
     */
    @Query("SELECT * FROM jogos WHERE status = :status ORDER BY data DESC LIMIT 1")
    suspend fun obterJogoPorStatus(status: StatusJogo): Jogo?

    /**
     * Retorna a última partida registrada em um intervalo de tempo.
     */
    @Query("SELECT * FROM jogos WHERE data >= :dataInicio AND data <= :dataFim ORDER BY numeroJogo DESC LIMIT 1")
    suspend fun obterUltimoJogoDoDia(dataInicio: Long, dataFim: Long): Jogo?

    /**
     * Conta o total de partidas realizadas no intervalo de tempo.
     */
    @Query("SELECT COUNT(*) FROM jogos WHERE data >= :dataInicio AND data <= :dataFim")
    suspend fun contarJogosDoDia(dataInicio: Long, dataFim: Long): Int

    /**
     * Insere uma nova partida no banco de dados.
     */
    @Insert
    suspend fun inserir(jogo: Jogo): Long

    /**
     * Atualiza os dados de uma partida existente.
     */
    @Update
    suspend fun atualizar(jogo: Jogo)

    /**
     * Remove uma partida do banco de dados.
     */
    @Delete
    suspend fun deletar(jogo: Jogo)

    /**
     * Atualiza apenas o status de uma partida.
     */
    @Query("UPDATE jogos SET status = :status WHERE id = :id")
    suspend fun atualizarStatus(id: Long, status: StatusJogo)

    /**
     * Atualiza o placar do Time Branco.
     */
    @Query("UPDATE jogos SET placarBranco = :placar WHERE id = :id")
    suspend fun atualizarPlacarBranco(id: Long, placar: Int)

    /**
     * Atualiza o placar do Time Vermelho.
     */
    @Query("UPDATE jogos SET placarVermelho = :placar WHERE id = :id")
    suspend fun atualizarPlacarVermelho(id: Long, placar: Int)

    /**
     * Finaliza a partida definindo o vencedor e alterando o status para FINALIZADO de forma atômica.
     */
    @Query("UPDATE jogos SET timeVencedor = :vencedor, status = :status WHERE id = :id")
    suspend fun finalizarJogo(id: Long, vencedor: TimeColor?, status: StatusJogo)
}
