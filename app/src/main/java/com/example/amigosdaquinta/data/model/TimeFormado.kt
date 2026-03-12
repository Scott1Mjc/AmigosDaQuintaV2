package com.example.amigosdaquinta.data.model

import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Representa um time escalado para uma partida.
 * 
 * Este modelo organiza os atletas por função (goleiro e linha) e fornece
 * propriedades utilitárias para validação da composição do time.
 *
 * @property cor Identificação do time (BRANCO ou VERMELHO).
 * @property goleiro Jogador escalado para a posição de goleiro.
 * @property jogadores Lista de jogadores escalados para a linha.
 */
data class TimeFormado(
    val cor: TimeColor,
    val goleiro: JogadorNaFila,
    val jogadores: List<JogadorNaFila>
) {
    /** 
     * Retorna a lista unificada de todos os atletas escalados no time. 
     */
    val todosJogadores: List<JogadorNaFila>
        get() = listOf(goleiro) + jogadores

    /** 
     * Indica se o time atingiu a formação completa (1 goleiro + 10 jogadores de linha). 
     */
    val completo: Boolean
        get() = jogadores.size == 10
}
