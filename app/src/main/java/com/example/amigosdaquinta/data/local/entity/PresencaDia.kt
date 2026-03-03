package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa a presenca de um jogador em um dia de sessao.
 *
 * [ordemChegada] e calculado pelo [PresencaRepository] no momento do registro,
 * garantindo sequencia incremental por dia.
 *
 * [jogosParticipados] e incrementado pelo [PresencaRepository] a cada jogo
 * que o jogador entra em campo, e zerado pelo FormadorDeTimes apos rotacao.
 *
 * [ativo] permite inativacao logica quando o jogador vai embora durante a sessao,
 * sem remover o registro do historico do dia.
 */
@Entity(tableName = "lista_presenca")
data class PresencaDia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jogadorId: Long,
    val data: Long,
    val horarioChegada: Long,
    val ordemChegada: Int,
    val jogosParticipados: Int = 0,
    val ativo: Boolean = true
)