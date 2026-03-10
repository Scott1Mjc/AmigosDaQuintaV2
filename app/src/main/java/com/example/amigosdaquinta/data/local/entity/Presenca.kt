package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade que representa a presença de um jogador em uma sessão.
 * Armazena o timestamp de chegada para ordenação por ordem de chegada.
 */
@Entity(
    tableName = "presencas",
    foreignKeys = [
        ForeignKey(
            entity = Jogador::class,
            parentColumns = ["id"],
            childColumns = ["jogadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["jogadorId"], unique = true) // Cada jogador só pode ter 1 presença
    ]
)
data class Presenca(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * ID do jogador que está presente.
     */
    val jogadorId: Long,

    /**
     * Timestamp de chegada (System.currentTimeMillis()).
     * Usado para ordenar jogadores por ordem de chegada.
     */
    val timestamp: Long
)