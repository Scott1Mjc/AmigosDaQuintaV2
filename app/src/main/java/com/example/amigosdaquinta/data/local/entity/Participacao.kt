package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Registra a participação e estatísticas de um jogador em uma partida específica.
 *
 * @property id Identificador único da participação.
 * @property jogadorId ID do jogador (Chave Estrangeira).
 * @property jogoId ID da partida (Chave Estrangeira).
 * @property time Time que o jogador defendeu (BRANCO ou VERMELHO).
 * @property gols Quantidade de gols marcados pelo jogador na partida.
 * @property assistencias Quantidade de assistências realizadas pelo jogador.
 * @property foiSubstituido Indica se o jogador saiu de campo (substituído).
 * @property entrouComoSubstituto Indica se o jogador iniciou no banco e entrou durante a partida.
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
