package com.example.amigosdaquinta.data.model

import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Representa um time formado para uma partida.
 *
 * [goleiro] é mantido separado de [jogadores] para facilitar validações
 * e exibição na UI sem precisar filtrar a lista a cada acesso.
 *
 * [todosJogadores] e [completo] são propriedades computadas — não armazenam estado,
 * apenas derivam dos campos principais.
 */
data class TimeFormado(
    val cor: TimeColor,
    val goleiro: JogadorNaFila,
    val jogadores: List<JogadorNaFila>
) {
    /** Goleiro + jogadores de linha. Usado pelo FormadorDeTimes para iteração completa do time. */
    val todosJogadores: List<JogadorNaFila>
        get() = listOf(goleiro) + jogadores

    /** True quando há exatamente 10 jogadores de linha (excluindo goleiro). */
    val completo: Boolean
        get() = jogadores.size == 10
}