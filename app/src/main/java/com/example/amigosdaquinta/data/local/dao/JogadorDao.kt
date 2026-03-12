package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.Jogador
import kotlinx.coroutines.flow.Flow

/**
 * Interface de acesso a dados para a entidade Jogador.
 *
 * Implementa as operações fundamentais de persistência e consulta para os atletas
 * do sistema Amigos da Quinta.
 */
@Dao
interface JogadorDao {

    /**
     * Retorna um fluxo reativo de todos os jogadores ativos.
     * Ordenados pelo número da camisa de forma crescente.
     */
    @Query("SELECT * FROM jogadores WHERE ativo = 1 ORDER BY numeroCamisa ASC")
    fun obterTodosAtivos(): Flow<List<Jogador>>

    /**
     * Retorna um fluxo reativo de todos os jogadores (ativos e inativos).
     * Ordenados alfabeticamente pelo nome.
     */
    @Query("SELECT * FROM jogadores ORDER BY nome ASC")
    fun obterTodos(): Flow<List<Jogador>>

    /**
     * Busca jogadores ativos cujo nome contenha o termo pesquisado.
     *
     * @param nome Termo para pesquisa.
     */
    @Query("SELECT * FROM jogadores WHERE nome LIKE '%' || :nome || '%' AND ativo = 1")
    fun buscarPorNome(nome: String): Flow<List<Jogador>>

    /**
     * Retorna a lista de goleiros ativos.
     */
    @Query("SELECT * FROM jogadores WHERE isPosicaoGoleiro = 1 AND ativo = 1")
    fun obterGoleiros(): Flow<List<Jogador>>

    /**
     * Busca um jogador específico pelo seu ID.
     */
    @Query("SELECT * FROM jogadores WHERE id = :id")
    suspend fun obterPorId(id: Long): Jogador?

    /**
     * Busca uma lista de jogadores pelos seus IDs.
     */
    @Query("SELECT * FROM jogadores WHERE id IN (:ids)")
    suspend fun obterPorIds(ids: List<Long>): List<Jogador>

    /**
     * Retorna o total de jogadores ativos cadastrados.
     */
    @Query("SELECT COUNT(*) FROM jogadores WHERE ativo = 1")
    suspend fun contarAtivos(): Int

    /**
     * Insere ou atualiza um jogador.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(jogador: Jogador): Long

    /**
     * Atualiza os dados de um jogador existente.
     */
    @Update
    suspend fun atualizar(jogador: Jogador)

    /**
     * Marca um jogador como inativo (exclusão lógica).
     * Mantém o registro no banco para integridade do histórico de partidas.
     */
    @Query("UPDATE jogadores SET ativo = 0 WHERE id = :id")
    suspend fun marcarComoInativo(id: Long)
}
