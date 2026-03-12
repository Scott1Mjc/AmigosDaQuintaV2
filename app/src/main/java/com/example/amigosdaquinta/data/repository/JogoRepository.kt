package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.JogoDao
import com.example.amigosdaquinta.data.local.dao.ParticipacaoDao
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * Repositório responsável pela gestão das partidas (Jogos).
 *
 * Centraliza as operações de criação, atualização, registro de placar e finalização de partidas,
 * interagindo com os DAOs de Jogo e Participação.
 *
 * @property jogoDao Objeto de acesso a dados (DAO) da entidade Jogo.
 * @property participacaoDao Objeto de acesso a dados (DAO) da entidade Participacao.
 */
class JogoRepository(
    private val jogoDao: JogoDao,
    private val participacaoDao: ParticipacaoDao
) {

    /**
     * Retorna a lista de jogos realizados em um intervalo específico de datas.
     * Ordenado cronologicamente de forma decrescente.
     *
     * @param dataInicio Timestamp inicial do intervalo.
     * @param dataFim Timestamp final do intervalo.
     */
    fun obterJogosDoDia(dataInicio: Long, dataFim: Long): Flow<List<Jogo>> =
        jogoDao.obterJogosDoDia(dataInicio, dataFim)

    /**
     * Retorna um fluxo com todos os jogos registrados no sistema.
     */
    fun obterTodos(): Flow<List<Jogo>> = jogoDao.obterTodos()

    /**
     * Busca um jogo específico pelo seu ID.
     */
    suspend fun obterPorId(id: Long): Jogo? = jogoDao.obterPorId(id)

    /**
     * Busca múltiplos jogos por uma lista de IDs.
     */
    suspend fun obterPorIds(ids: List<Long>): List<Jogo> = jogoDao.obterPorIds(ids)

    /**
     * Busca um jogo filtrando pelo seu status (ex: EM_ANDAMENTO).
     */
    suspend fun obterJogoPorStatus(status: StatusJogo): Jogo? =
        jogoDao.obterJogoPorStatus(status)

    /**
     * Busca o registro do último jogo ocorrido em um intervalo de datas.
     */
    suspend fun obterUltimoJogoDoDia(dataInicio: Long, dataFim: Long): Jogo? =
        jogoDao.obterUltimoJogoDoDia(dataInicio, dataFim)

    /**
     * Cria e persiste um novo jogo.
     *
     * @param jogo Dados do jogo a ser criado.
     * @return O ID gerado para a nova partida.
     */
    suspend fun criarJogo(jogo: Jogo): Long = jogoDao.inserir(jogo)

    /**
     * Atualiza as informações completas de um jogo.
     */
    suspend fun atualizarJogo(jogo: Jogo) = jogoDao.atualizar(jogo)

    /**
     * Atualiza apenas o status de uma partida específica.
     */
    suspend fun atualizarStatus(id: Long, status: StatusJogo) =
        jogoDao.atualizarStatus(id, status)

    /**
     * Incrementa o placar do time especificado para uma partida em andamento.
     *
     * @param jogoId ID do jogo.
     * @param time Time que marcou o gol (BRANCO ou VERMELHO).
     */
    suspend fun registrarGol(jogoId: Long, time: TimeColor) {
        val jogo = jogoDao.obterPorId(jogoId) ?: return

        if (time == TimeColor.BRANCO) {
            jogoDao.atualizarPlacarBranco(jogoId, jogo.placarBranco + 1)
        } else {
            jogoDao.atualizarPlacarVermelho(jogoId, jogo.placarVermelho + 1)
        }
    }

    /**
     * Conclui uma partida, definindo o status como FINALIZADO e o time vencedor.
     *
     * @param id ID do jogo.
     * @param vencedor Time vencedor (nulo para empate).
     */
    suspend fun finalizarJogo(id: Long, vencedor: TimeColor?) =
        jogoDao.finalizarJogo(id, vencedor, StatusJogo.FINALIZADO)

    /**
     * Conta a quantidade total de jogos realizados no intervalo especificado.
     */
    suspend fun contarJogosDoDia(dataInicio: Long, dataFim: Long): Int =
        jogoDao.contarJogosDoDia(dataInicio, dataFim)

    /**
     * Retorna a lista de participações individuais dos jogadores de uma partida específica.
     */
    fun obterParticipacoesPorJogo(jogoId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogo(jogoId)

    /**
     * Registra as participações de vários jogadores em uma única operação.
     *
     * @param participacoes Lista de objetos Participacao a serem inseridos.
     */
    suspend fun adicionarParticipacoes(participacoes: List<Participacao>) =
        participacaoDao.inserirVarias(participacoes)
}
