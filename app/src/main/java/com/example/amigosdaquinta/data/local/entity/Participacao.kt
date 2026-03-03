package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidade que representa a participacao de um jogador em uma partida especifica.
 *
 * Relacionamento N:N entre [Jogador] e [Jogo], com estatísticas individuais por partida.
 *
 * ForeignKeys com CASCADE garantem que ao deletar um jogador ou jogo,
 * todas as participacoes associadas sejam removidas automaticamente.
 *
 * [gols] e [assistencias] sao incrementados atomicamente via queries dedicadas
 * no [ParticipacaoDao] para evitar race conditions.
 */
@Entity(
    tableName = "participacoes",
    foreignKeys = [
        ForeignKey(
            entity = Jogador::class,
            parentColumns = ["id"],
            childColumns = ["jogadorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Jogo::class,
            parentColumns = ["id"],
            childColumns = ["jogoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Participacao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jogadorId: Long,
    val jogoId: Long,
    val time: TimeColor,
    val gols: Int = 0,
    val assistencias: Int = 0
)