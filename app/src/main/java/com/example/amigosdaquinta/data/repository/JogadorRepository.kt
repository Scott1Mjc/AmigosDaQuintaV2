package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.JogadorDao
import com.example.amigosdaquinta.data.local.entity.Jogador
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gerenciar operações de jogadores.
 * Abstrai o acesso ao DAO e fornece uma API limpa para a camada de apresentação.
 *
 * @property jogadorDao DAO de jogadores
 */
class JogadorRepository(private val jogadorDao: JogadorDao) {

    /**
     * Obtém todos os jogadores ativos (não marcados como inativos).
     * Retorna um Flow que atualiza automaticamente quando há mudanças.
     */
    fun obterTodosAtivos(): Flow<List<Jogador>> = jogadorDao.obterTodosAtivos()

    /**
     * Obtém todos os jogadores (ativos e inativos).
     */
    fun obterTodos(): Flow<List<Jogador>> = jogadorDao.obterTodos()

    /**
     * Busca jogadores por nome (parcial).
     *
     * @param nome Termo de busca
     */
    fun buscarPorNome(nome: String): Flow<List<Jogador>> = jogadorDao.buscarPorNome(nome)

    /**
     * Obtém apenas os goleiros ativos.
     */
    fun obterGoleiros(): Flow<List<Jogador>> = jogadorDao.obterGoleiros()

    /**
     * Insere um novo jogador no banco.
     *
     * @param jogador Jogador a ser inserido
     * @return ID do jogador inserido
     */
    suspend fun inserir(jogador: Jogador): Long = jogadorDao.inserir(jogador)

    /**
     * Atualiza os dados de um jogador existente.
     *
     * @param jogador Jogador com dados atualizados
     */
    suspend fun atualizar(jogador: Jogador) = jogadorDao.atualizar(jogador)

    /**
     * Deleta um jogador fisicamente do banco.
     * NOTA: Prefira usar marcarComoInativo() para manter histórico.
     *
     * @param jogador Jogador a ser deletado
     */
    suspend fun deletar(jogador: Jogador) = jogadorDao.deletar(jogador)

    /**
     * Marca um jogador como inativo (soft delete).
     * O jogador continua no banco mas não aparece nas listagens ativas.
     *
     * @param id ID do jogador
     */
    suspend fun marcarComoInativo(id: Long) = jogadorDao.marcarComoInativo(id)

    /**
     * Conta quantos jogadores estão ativos.
     *
     * @return Quantidade de jogadores ativos
     */
    suspend fun contarAtivos(): Int = jogadorDao.contarAtivos()

    /**
     * Obtém múltiplos jogadores por seus IDs.
     * Útil para buscar jogadores de uma partida específica.
     *
     * @param ids Lista de IDs dos jogadores
     * @return Lista de jogadores encontrados
     */
    suspend fun obterPorIds(ids: List<Long>): List<Jogador> = jogadorDao.obterPorIds(ids)

    /**
     * Obtém um jogador específico por ID.
     *
     * @param id ID do jogador
     * @return Jogador encontrado ou null
     */
    suspend fun obterPorId(id: Long): Jogador? = jogadorDao.obterPorId(id)
}