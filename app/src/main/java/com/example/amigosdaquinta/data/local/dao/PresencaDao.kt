package com.example.amigosdaquinta.data.local.dao

import androidx.room.*
import com.example.amigosdaquinta.data.local.entity.PresencaDia
import kotlinx.coroutines.flow.Flow

/**
 * DAO de acesso à tabela [lista_presenca].
 *
 * Cada registro representa a chegada de um jogador em um dia de sessão,
 * armazenando timestamp de chegada e ordem sequencial para formação de times.
 *
 * Funcionalidades principais:
 * - Registro de presença com timestamp e ordem de chegada
 * - Consulta de jogadores ativos e inativos
 * - Controle de jogos consecutivos (rotação automática)
 * - Inativação lógica (preserva histórico)
 * - Limpeza de dados (novo dia ou reset)
 *
 * Campo [ativo] permite inativação lógica sem perda de histórico — jogadores
 * que vão embora durante a sessão são marcados como inativos em vez de deletados.
 *
 * Campo [jogosParticipados] é usado pelo FormadorDeTimes para controlar rotação
 * de jogadores após dois jogos consecutivos (regra de negócio).
 *
 * [obterPresencasDoDia] retorna apenas ativos para formação de times.
 * [obterTodasPresencasDoDia] retorna todos (ativo + inativo) para relatórios e auditoria.
 */
@Dao
interface PresencaDao {

    // =========================================================================
    // CONSULTAS REATIVAS (FLOW) - AUTO-ATUALIZAM A UI
    // =========================================================================

    /**
     * Retorna presenças ativas do dia ordenadas por chegada.
     *
     * Usado para:
     * - Formação automática de times (ordem de chegada)
     * - Lista de jogadores disponíveis
     * - Fila de espera
     *
     * Flow atualiza automaticamente quando há mudanças na tabela.
     *
     * @param dataInicio Início do dia (00:00:00 em timestamp)
     * @param dataFim Fim do dia (23:59:59 em timestamp)
     * @return Flow com lista de presenças ativas ordenadas
     */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1 
        ORDER BY ordemChegada ASC
    """)
    fun obterPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    /**
     * Retorna TODAS as presenças do dia (ativas e inativas).
     *
     * Usado para:
     * - Relatórios completos da sessão
     * - Auditoria de jogadores que saíram
     * - Histórico total do dia
     *
     * @param dataInicio Início do dia (00:00:00 em timestamp)
     * @param dataFim Fim do dia (23:59:59 em timestamp)
     * @return Flow com lista completa de presenças
     */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim 
        ORDER BY ordemChegada ASC
    """)
    fun obterTodasPresencasDoDia(dataInicio: Long, dataFim: Long): Flow<List<PresencaDia>>

    // =========================================================================
    // CONSULTAS DIRETAS (SUSPEND) - PARA LÓGICA DE NEGÓCIO
    // =========================================================================

    /**
     * Busca uma presença por ID.
     *
     * @param id ID único da presença
     * @return Presença encontrada ou null
     */
    @Query("SELECT * FROM lista_presenca WHERE id = :id")
    suspend fun obterPorId(id: Long): PresencaDia?

    /**
     * Busca a presença de um jogador específico em um dia.
     *
     * Usado para:
     * - Verificar se jogador já registrou presença
     * - Evitar duplicação de registros
     * - Obter dados da presença para atualização
     *
     * @param jogadorId ID do jogador
     * @param dataInicio Início do dia
     * @param dataFim Fim do dia
     * @return Presença do jogador ou null se não registrada
     */
    @Query("""
        SELECT * FROM lista_presenca 
        WHERE jogadorId = :jogadorId AND data >= :dataInicio AND data <= :dataFim 
        LIMIT 1
    """)
    suspend fun obterPresencaPorJogadorEDia(
        jogadorId: Long,
        dataInicio: Long,
        dataFim: Long
    ): PresencaDia?

    /**
     * Retorna o maior valor de [ordemChegada] registrado no dia.
     *
     * Usado pelo Repository para calcular a próxima ordem de chegada
     * de forma sequencial (última ordem + 1).
     *
     * Retorna null se nenhuma presença foi registrada ainda no dia.
     *
     * Exemplo:
     * - Se última ordem é 5, próximo jogador será 6
     * - Se null (primeiro do dia), próximo será 1
     *
     * @param dataInicio Início do dia
     * @param dataFim Fim do dia
     * @return Maior ordem de chegada ou null
     */
    @Query("""
        SELECT MAX(ordemChegada) 
        FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim
    """)
    suspend fun obterUltimaOrdemChegada(dataInicio: Long, dataFim: Long): Int?

    /**
     * Conta quantos jogadores ativos estão presentes no dia.
     *
     * Usado para:
     * - Validar se há jogadores suficientes (mínimo 22)
     * - Estatísticas da sessão
     * - Decisões de formação de times
     *
     * @param dataInicio Início do dia
     * @param dataFim Fim do dia
     * @return Número de presenças ativas
     */
    @Query("""
        SELECT COUNT(*) 
        FROM lista_presenca 
        WHERE data >= :dataInicio AND data <= :dataFim AND ativo = 1
    """)
    suspend fun contarPresencasAtivas(dataInicio: Long, dataFim: Long): Int

    // =========================================================================
    // OPERAÇÕES DE ESCRITA
    // =========================================================================

    /**
     * Insere uma nova presença.
     *
     * Fluxo típico:
     * 1. Jogador marca presença na chegada
     * 2. Repository calcula próxima ordem de chegada
     * 3. Cria PresencaDia com timestamp e ordem
     * 4. Insere no banco
     *
     * @param presenca Dados da presença a inserir
     * @return ID gerado automaticamente
     */
    @Insert
    suspend fun inserir(presenca: PresencaDia): Long

    /**
     * Atualiza uma presença existente.
     *
     * Usado para:
     * - Incrementar jogos participados
     * - Marcar como inativo
     * - Resetar contadores
     *
     * @param presenca Presença com dados atualizados
     */
    @Update
    suspend fun atualizar(presenca: PresencaDia)

    /**
     * Inativação lógica: jogador sai mas registro é preservado.
     *
     * Usado quando:
     * - Jogador vai embora antes do fim da sessão
     * - Jogador não pode mais jogar (machucado, etc)
     *
     * O jogador NÃO aparecerá mais em:
     * - Fila de espera
     * - Formação de times
     * - Contagem de ativos
     *
     * MAS será visível em:
     * - Relatórios completos
     * - Histórico do dia
     * - Auditoria
     *
     * @param id ID da presença a inativar
     */
    @Query("UPDATE lista_presenca SET ativo = 0 WHERE id = :id")
    suspend fun marcarComoInativo(id: Long)

    /**
     * Incrementa contador de jogos participados.
     *
     * Chamado quando jogador entra em um jogo.
     * Usado para controlar rotação (máximo 2 jogos consecutivos).
     *
     * @param id ID da presença
     */
    @Query("UPDATE lista_presenca SET jogosParticipados = jogosParticipados + 1 WHERE id = :id")
    suspend fun incrementarJogosParticipados(id: Long)

    /**
     * Zera o contador de jogos participados.
     *
     * Chamado quando:
     * - Jogador sai por rotação (jogou 2x seguidas)
     * - Time perde (contador reseta para próximo ciclo)
     * - Início de novo ciclo de formação
     *
     * Permite que jogador volte a jogar após rotação obrigatória.
     *
     * @param id ID da presença
     */
    @Query("UPDATE lista_presenca SET jogosParticipados = 0 WHERE id = :id")
    suspend fun resetarJogosConsecutivos(id: Long)

    // =========================================================================
    // OPERAÇÕES DE LIMPEZA
    // =========================================================================

    /**
     * Remove TODOS os registros de presença do banco.
     *
     * ATENÇÃO: Operação destrutiva!
     * Remove dados de TODOS os dias, não apenas do dia atual.
     *
     * Usado para:
     * - Reset completo do app
     * - Limpeza de dados de teste
     * - Debug
     *
     * Para limpar apenas o dia atual, use [limparPresencasDoDia].
     */
    @Query("DELETE FROM lista_presenca")
    suspend fun deleteAll()

    /**
     * Deleção física de presença individual.
     *
     * Uso raro - prefira [marcarComoInativo] para preservar histórico.
     *
     * @param presenca Presença a deletar permanentemente
     */
    @Delete
    suspend fun deletar(presenca: PresencaDia)

    /**
     * Deleção física de todos os registros do dia.
     *
     * Usado ao:
     * - Encerrar sessão do dia
     * - Resetar dia para nova sessão
     * - Limpar dados antes de iniciar novo dia
     *
     * ATENÇÃO: Remove permanentemente!
     * Considere exportar dados antes se precisar de histórico.
     *
     * @param dataInicio Início do dia a limpar
     * @param dataFim Fim do dia a limpar
     */
    @Query("DELETE FROM lista_presenca WHERE data >= :dataInicio AND data <= :dataFim")
    suspend fun limparPresencasDoDia(dataInicio: Long, dataFim: Long)
}