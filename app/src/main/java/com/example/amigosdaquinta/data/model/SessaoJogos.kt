package com.example.amigosdaquinta.data.model

import com.example.amigosdaquinta.data.local.entity.Jogo

/**
 * Representa o estado consolidado de uma sessão de jogos.
 * 
 * Este modelo agrupa as informações de uma data de pelada, incluindo a lista de 
 * jogos realizados, a partida atual e o estado da fila de espera.
 *
 * @property data Timestamp da data da sessão.
 * @property jogos Lista de todas as partidas registradas no dia.
 * @property jogoAtual Partida que está em andamento (se houver).
 * @property filaEspera Lista de jogadores aguardando para entrar em campo.
 * @property timeBrancoProximo Sugestão de escalação para o Time Branco.
 * @property timeVermelhoProximo Sugestão de escalação para o Time Vermelho.
 * @property totalPresentes Contagem total de jogadores na sessão.
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
