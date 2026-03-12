package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa um jogador cadastrado no sistema Amigos da Quinta.
 *
 * @property id Identificador único gerado automaticamente pelo banco de dados.
 * @property nome Nome completo ou apelido do jogador.
 * @property numeroCamisa Número identificador oficial do jogador (0-999).
 * @property isPosicaoGoleiro Indica se o jogador atua preferencialmente como goleiro.
 * @property ativo Define se o jogador está ativo. Inativação lógica é usada para preservar o histórico.
 */
@Entity(tableName = "jogadores")
data class Jogador(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val numeroCamisa: Int,
    val isPosicaoGoleiro: Boolean = false,
    val ativo: Boolean = true,
) {
    init {
        require(nome.isNotBlank()) { "O nome do jogador não pode estar em branco" }
        require(numeroCamisa in 0..999) { "O número da camisa deve estar entre 0 e 999" }
    }
}
