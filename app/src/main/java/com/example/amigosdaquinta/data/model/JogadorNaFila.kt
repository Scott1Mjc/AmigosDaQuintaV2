package com.example.amigosdaquinta.data.model

import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.PresencaDia

/**
 * Representa o estado de um jogador posicionado na fila de espera.
 * 
 * Vincula o cadastro do atleta ao seu registro de presença atual, permitindo
 * o controle de jogos consecutivos e disponibilidade para entrar em campo.
 *
 * @property jogador Dados cadastrais do atleta.
 * @property presenca Registro de presença na sessão atual.
 * @property jogosConsecutivos Contador de partidas disputadas em sequência.
 * @property podeJogar Define se o jogador está apto a ser escalado (ex: não lesionado).
 */
data class JogadorNaFila(
    val jogador: Jogador,
    val presenca: PresencaDia,
    val jogosConsecutivos: Int = 0,
    val podeJogar: Boolean = true
)
