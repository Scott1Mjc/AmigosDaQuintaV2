package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.Jogador
import kotlinx.coroutines.flow.Flow

/**
 * Interface de acesso a dados para a entidade Jogador.
 */
@Dao
interface JogadorDao {

    @Query("SELECT * FROM jogadores WHERE ativo = 1 ORDER BY numeroCamisa ASC")
    fun obterTodosAtivos(): Flow<List<Jogador>>

    @Query("SELECT * FROM jogadores ORDER BY nome ASC")
    fun obterTodos(): Flow<List<Jogador>>

    /**
     * Busca jogadores ativos por nome ou número da camisa (Exato para número).
     */
    @Query("SELECT * FROM jogadores WHERE (nome LIKE '%' || :query || '%' OR CAST(numeroCamisa AS TEXT) = :query) AND ativo = 1")
    fun buscarJogadores(query: String): Flow<List<Jogador>>

    @Query("SELECT * FROM jogadores WHERE isPosicaoGoleiro = 1 AND ativo = 1")
    fun obterGoleiros(): Flow<List<Jogador>>

    @Query("SELECT * FROM jogadores WHERE id = :id")
    suspend fun obterPorId(id: Long): Jogador?

    @Query("SELECT * FROM jogadores WHERE id IN (:ids)")
    suspend fun obterPorIds(ids: List<Long>): List<Jogador>

    @Query("SELECT COUNT(*) FROM jogadores WHERE ativo = 1")
    suspend fun contarAtivos(): Int

    @Query("SELECT * FROM jogadores WHERE numeroCamisa = :numero LIMIT 1")
    suspend fun obterPorNumeroCamisa(numero: Int): Jogador?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(jogador: Jogador): Long

    @Update
    suspend fun atualizar(jogador: Jogador)

    @Query("UPDATE jogadores SET ativo = 0 WHERE id = :id")
    suspend fun marcarComoInativo(id: Long)

    @Query("UPDATE jogadores SET estaEmCampo = :emCampo WHERE id = :id")
    suspend fun atualizarStatusCampo(id: Long, emCampo: Boolean)

    @Query("UPDATE jogadores SET estaEmCampo = :emCampo WHERE id IN (:ids)")
    suspend fun atualizarStatusCampoMuitos(ids: List<Long>, emCampo: Boolean)
}
