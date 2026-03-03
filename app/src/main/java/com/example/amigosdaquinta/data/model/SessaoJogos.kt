package com.example.amigosdaquinta.data.model

import com.example.amigosdaquinta.data.local.entity.Jogo

/**
 * Representa o estado completo de uma sessão de jogos (dia de pelada).
 *
 * É um snapshot imutável do estado atual, mantido pelo SessaoViewModel via StateFlow.
 *
 * [timeBrancoProximo] e [timeVermelhoProximo] são pré-calculados pelo FormadorDeTimes
 * após cada jogo, permitindo que a UI exiba a prévia do próximo confronto antes de confirmá-lo.
 */
data class SessaoJogos(
    val data: Long,
    val jogos: List<Jogo> = emptyList(),
    val jogoAtual: Jogo? = null,
    val filaEspera: List<JogadorNaFila> = emptyList(),
    val timeBrancoProximo: TimeFormado? = null,
    val timeVermelhoProximo: TimeFormado? = null,
    val totalPresentes: Int = 0
)