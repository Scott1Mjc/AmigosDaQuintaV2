package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.JogadorDao
import com.example.amigosdaquinta.data.local.entity.Jogador
import kotlinx.coroutines.flow.Flow

/**
 * Repositório responsável pela gestão de dados dos jogadores.
 *
 * Centraliza as operações de consulta, cadastro e inativação lógica de atletas.
 *
 * @property jogadorDao Objeto de acesso a dados (DAO) da entidade Jogador.
 */
class JogadorRepository(private val jogadorDao: JogadorDao) {

    /**
     * Retorna um fluxo com todos os jogadores ativos no sistema.
     */
    fun obterTodosAtivos(): Flow<List<Jogador>> = jogadorDao.obterTodosAtivos()

    /**
     * Retorna um fluxo com todos os registros de jogadores, incluindo inativos.
     */
    fun obterTodos(): Flow<List<Jogador>> = jogadorDao.obterTodos()

    /**
     * Busca jogadores ativos pelo nome (ou parte dele).
     *
     * @param nome Termo para pesquisa.
     */
    fun buscarPorNome(nome: String): Flow<List<Jogador>> = jogadorDao.buscarPorNome(nome)

    /**
     * Retorna a lista de goleiros ativos.
     */
    fun obterGoleiros(): Flow<List<Jogador>> = jogadorDao.obterGoleiros()

    /**
     * Cadastra um novo jogador.
     *
     * @param jogador Objeto jogador a ser persistido.
     * @return O ID gerado para o novo registro.
     */
    suspend fun inserir(jogador: Jogador): Long = jogadorDao.inserir(jogador)

    /**
     * Atualiza as informações de um jogador existente.
     */
    suspend fun atualizar(jogador: Jogador) = jogadorDao.atualizar(jogador)

    /**
     * Inativa um jogador logicamente (recomendado para manter histórico).
     */
    suspend fun marcarComoInativo(id: Long) = jogadorDao.marcarComoInativo(id)

    /**
     * Retorna a contagem total de jogadores ativos.
     */
    suspend fun contarAtivos(): Int = jogadorDao.contarAtivos()

    /**
     * Busca múltiplos jogadores através de uma lista de identificadores.
     */
    suspend fun obterPorIds(ids: List<Long>): List<Jogador> = jogadorDao.obterPorIds(ids)

    /**
     * Busca um jogador específico pelo seu ID.
     */
    suspend fun obterPorId(id: Long): Jogador? = jogadorDao.obterPorId(id)
}
