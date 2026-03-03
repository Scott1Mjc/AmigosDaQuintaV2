package com.example.amigosdaquinta.data.model

import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.PresencaDia

/**
 * Representa um jogador posicionado na fila de espera durante a sessão.
 *
 * [jogosConsecutivos] é incrementado a cada jogo que o jogador participa sem sair
 * e é usado pelo FormadorDeTimes para controle de rotação.
 *
 * [podeJogar] pode ser false quando o jogador está temporariamente indisponível
 * (ex: lesão durante a sessão) sem precisar ser removido da fila.
 */
data class JogadorNaFila(
    val jogador: Jogador,
    val presenca: PresencaDia,
    val jogosConsecutivos: Int = 0,
    val podeJogar: Boolean = true
)