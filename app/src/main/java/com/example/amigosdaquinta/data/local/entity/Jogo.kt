package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa uma partida registrada no app.
 *
 * [numeroJogo] e a sequencia da partida dentro da sessao do dia (1, 2, 3...).
 * [status] segue o ciclo: AGUARDANDO -> EM_ANDAMENTO -> FINALIZADO (ou CANCELADO).
 * [timeVencedor] e null em caso de empate ou enquanto o jogo nao foi finalizado.
 * [duracao] e definido pela regra da sessao: 30 min para o 1o jogo, 15 min para os demais.
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