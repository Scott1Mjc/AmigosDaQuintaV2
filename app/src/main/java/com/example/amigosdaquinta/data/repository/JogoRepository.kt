package com.example.amigosdaquinta.data.repository

import com.example.amigosdaquinta.data.local.dao.JogoDao
import com.example.amigosdaquinta.data.local.dao.ParticipacaoDao
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gerenciar operações de jogos e participações.
 * Centraliza a lógica de acesso a dados relacionados a jogos.
 *
 * @property jogoDao DAO de jogos
 * @property participacaoDao DAO de participações
 */
class JogoRepository(
    private val jogoDao: JogoDao,
    private val participacaoDao: ParticipacaoDao
) {

    /**
     * Obtém jogos em um intervalo de datas.
     * Retorna ordenado do mais recente para o mais antigo.
     *
     * @param dataInicio Timestamp inicial
     * @param dataFim Timestamp final
     */
    fun obterJogosDoDia(dataInicio: Long, dataFim: Long): Flow<List<Jogo>> =
        jogoDao.obterJogosDoDia(dataInicio, dataFim)

    /**
     * Obtém todos os jogos cadastrados.
     * Usado para histórico completo.
     */
    fun obterTodos(): Flow<List<Jogo>> = jogoDao.obterTodos()

    /**
     * Obtém um jogo específico por ID.
     *
     * @param id ID do jogo
     * @return Jogo encontrado ou null
     */
    suspend fun obterPorId(id: Long): Jogo? = jogoDao.obterPorId(id)

    /**
     * Alias para obterPorId (mantido por compatibilidade).
     */
    suspend fun obterJogoPorId(id: Long): Jogo? = obterPorId(id)

    /**
     * Obtém múltiplos jogos por seus IDs.
     * Útil para estatísticas de jogadores.
     *
     * @param ids Lista de IDs dos jogos
     * @return Lista de jogos encontrados
     */
    suspend fun obterPorIds(ids: List<Long>): List<Jogo> = jogoDao.obterPorIds(ids)

    /**
     * Busca um jogo por status.
     * Útil para encontrar jogo em andamento.
     *
     * @param status Status do jogo
     * @return Primeiro jogo encontrado com esse status ou null
     */
    suspend fun obterJogoPorStatus(status: StatusJogo): Jogo? =
        jogoDao.obterJogoPorStatus(status)

    /**
     * Obtém o último jogo de um dia específico.
     *
     * @param dataInicio Início do dia
     * @param dataFim Fim do dia
     * @return Último jogo do dia ou null
     */
    suspend fun obterUltimoJogoDoDia(dataInicio: Long, dataFim: Long): Jogo? =
        jogoDao.obterUltimoJogoDoDia(dataInicio, dataFim)

    /**
     * Cria um novo jogo no banco.
     *
     * @param jogo Dados do jogo
     * @return ID do jogo criado
     */
    suspend fun criarJogo(jogo: Jogo): Long = jogoDao.inserir(jogo)

    /**
     * Atualiza os dados de um jogo existente.
     *
     * @param jogo Jogo com dados atualizados
     */
    suspend fun atualizarJogo(jogo: Jogo) = jogoDao.atualizar(jogo)

    /**
     * Atualiza apenas o status de um jogo.
     *
     * @param id ID do jogo
     * @param status Novo status
     */
    suspend fun atualizarStatus(id: Long, status: StatusJogo) =
        jogoDao.atualizarStatus(id, status)

    /**
     * Registra um gol para um dos times.
     * Incrementa o placar correspondente.
     *
     * @param jogoId ID do jogo
     * @param time Time que marcou o gol
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
     * Finaliza um jogo e define o vencedor.
     *
     * @param id ID do jogo
     * @param vencedor Time vencedor (ou null para empate)
     */
    suspend fun finalizarJogo(id: Long, vencedor: TimeColor?) =
        jogoDao.finalizarJogo(id, vencedor, StatusJogo.FINALIZADO)

    /**
     * Conta quantos jogos aconteceram em um intervalo de datas.
     *
     * @param dataInicio Timestamp inicial
     * @param dataFim Timestamp final
     * @return Quantidade de jogos
     */
    suspend fun contarJogosDoDia(dataInicio: Long, dataFim: Long): Int =
        jogoDao.contarJogosDoDia(dataInicio, dataFim)

    /**
     * Obtém as participações de um jogo específico.
     *
     * @param jogoId ID do jogo
     * @return Flow com lista de participações
     */
    fun obterParticipacoesPorJogo(jogoId: Long): Flow<List<Participacao>> =
        participacaoDao.obterParticipacoesPorJogo(jogoId)

    /**
     * Adiciona múltiplas participações de uma vez.
     * Usado ao criar um jogo novo.
     *
     * @param participacoes Lista de participações a serem inseridas
     */
    suspend fun adicionarParticipacoes(participacoes: List<Participacao>) =
        participacaoDao.inserirVarias(participacoes)
}