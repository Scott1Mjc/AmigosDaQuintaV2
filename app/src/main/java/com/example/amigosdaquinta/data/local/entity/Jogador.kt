package com.example.amigosdaquinta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa um jogador cadastrado no app.
 *
 * [numeroCamisa] é armazenado como Int, mas o JogadorDao usa CAST para ordenacao
 * correta em queries SQL (evita ordenacao lexicografica).
 *
 * [ativo] controla inativacao logica — jogadores removidos nao sao deletados
 * do banco para preservar historico de participacoes e estatisticas.
 *
 * [foto] reservado para uso futuro (URI ou caminho local da imagem).
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
        require(nome.isNotBlank()) { "Nome não pode ser vazio" }
        require(numeroCamisa in 0..999) { "Número da camisa deve estar entre 0 e 999" }
    }
}