package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a presença de um jogador na fila de espera da sessão atual.
 *
 * @property id Identificador único da presença.
 * @property jogadorId ID do jogador (Chave Estrangeira). Único por sessão.
 * @property timestamp Horário (em milissegundos) da chegada do jogador para ordenação.
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
        Index(value = ["jogadorId"], unique = true)
    ]
)
data class Presenca(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jogadorId: Long,
    val timestamp: Long
)
