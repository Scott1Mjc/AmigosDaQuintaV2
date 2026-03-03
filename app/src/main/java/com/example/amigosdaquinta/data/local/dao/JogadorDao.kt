package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.Jogador
import kotlinx.coroutines.flow.Flow

/**
 * DAO de acesso aos dados de jogadores.
 *
 * O campo [numeroCamisa] é armazenado como String na entidade, portanto a ordenação
 * numérica é feita via CAST para evitar ordenação lexicográfica (ex: "10" < "9").
 *
 * Deleção física está disponível via [deletar], mas o fluxo padrão do app
 * utiliza inativação lógica via [marcarComoInativo] para preservar histórico.
 */
@Dao
interface JogadorDao {

    // region Consultas reativas (Flow)

    /** Retorna apenas jogadores com [ativo] = true, ordenados pelo número de camisa numericamente. */
    @Query("SELECT * FROM jogadores WHERE ativo = 1 ORDER BY CAST(numeroCamisa AS INTEGER) ASC")
    fun obterTodosAtivos(): Flow<List<Jogador>>

    /** Retorna todos os jogadores (ativos e inativos), ordenados por nome. */
    @Query("SELECT * FROM jogadores ORDER BY nome ASC")
    fun obterTodos(): Flow<List<Jogador>>

    /** Busca jogadores ativos cujo nome contenha o trecho informado (case-insensitive via LIKE). */
    @Query("SELECT * FROM jogadores WHERE nome LIKE '%' || :nome || '%' AND ativo = 1")
    fun buscarPorNome(nome: String): Flow<List<Jogador>>

    /** Retorna apenas goleiros ativos. */
    @Query("SELECT * FROM jogadores WHERE isPosicaoGoleiro = 1 AND ativo = 1")
    fun obterGoleiros(): Flow<List<Jogador>>

    // endregion

    // region Consultas suspend

    @Query("SELECT * FROM jogadores WHERE id = :id")
    suspend fun obterPorId(id: Long): Jogador?

    @Query("SELECT * FROM jogadores WHERE id IN (:ids)")
    suspend fun obterPorIds(ids: List<Long>): List<Jogador>

    @Query("SELECT COUNT(*) FROM jogadores WHERE ativo = 1")
    suspend fun contarAtivos(): Int

    // endregion

    // region Escrita

    /** Insere ou substitui o jogador em caso de conflito de chave primária. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(jogador: Jogador): Long

    @Update
    suspend fun atualizar(jogador: Jogador)

    /** Deleção física. Usar apenas em contextos administrativos; prefira [marcarComoInativo]. */
    @Delete
    suspend fun deletar(jogador: Jogador)

    /** Inativação lógica: preserva histórico de participações e estatísticas. */
    @Query("UPDATE jogadores SET ativo = 0 WHERE id = :id")
    suspend fun marcarComoInativo(id: Long)

    // endregion
}