package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidade que representa a participacao de um jogador em uma partida especifica.
 * Adicionado campo entrouComoSubstituto para persistência.
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
    val assistencias: Int = 0,
    val foiSubstituido: Boolean = false,
    val entrouComoSubstituto: Boolean = false
)
