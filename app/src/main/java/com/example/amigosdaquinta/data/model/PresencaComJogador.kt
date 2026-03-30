package com.example.amigosdaquinta.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.PresencaDia

/**
 * Modelo de dados que combina o registro de presença com as informações do jogador.
 * Utilizado para facilitar a exibição da lista de presença na UI.
 */
data class PresencaComJogador(
    @Embedded val presenca: PresencaDia,
    @Relation(
        parentColumn = "jogadorId",
        entityColumn = "id"
    )
    val jogador: Jogador
) {
    val horarioChegada: Long get() = presenca.horarioChegada
}
