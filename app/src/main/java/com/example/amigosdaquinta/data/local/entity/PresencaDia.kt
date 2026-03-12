package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa o registro histórico de presença de um jogador em uma data específica.
 *
 * @property id Identificador único do registro.
 * @property jogadorId ID do jogador.
 * @property data Data da sessão (timestamp).
 * @property horarioChegada Horário exato da entrada na fila (timestamp).
 * @property ordemChegada Posição numérica de chegada no dia.
 * @property jogosParticipados Contador de partidas disputadas pelo jogador no dia.
 * @property ativo Define se o jogador ainda está presente na sessão.
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
