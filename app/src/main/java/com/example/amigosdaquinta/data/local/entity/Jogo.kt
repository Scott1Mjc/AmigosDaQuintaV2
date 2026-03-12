package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma partida de futebol realizada durante uma sessão.
 *
 * @property id Identificador único da partida.
 * @property data Timestamp da data e hora em que a partida foi iniciada.
 * @property numeroJogo Sequência da partida dentro da sessão do dia (ex: 1º jogo, 2º jogo).
 * @property duracao Tempo total da partida em minutos.
 * @property status Estado atual da partida (AGUARDANDO, EM_ANDAMENTO, FINALIZADO, CANCELADO).
 * @property timeVencedor Cor do time vencedor ou nulo em caso de empate.
 * @property placarBranco Gols marcados pelo time Branco.
 * @property placarVermelho Gols marcados pelo time Vermelho.
 */
@Entity(tableName = "jogos")
data class Jogo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val data: Long,
    val numeroJogo: Int,
    val duracao: Int,
    val status: StatusJogo,
    val timeVencedor: TimeColor? = null,
    val placarBranco: Int = 0,
    val placarVermelho: Int = 0
)
