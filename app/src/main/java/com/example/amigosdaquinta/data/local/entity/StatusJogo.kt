package com.example.amigosdaquinta.data.local.entity

/**
 * Representa os possíveis estados de uma partida no sistema.
 */
enum class StatusJogo {
    /** A partida está configurada, mas o cronômetro ainda não foi iniciado. */
    AGUARDANDO,
    /** A partida está em andamento com o cronômetro ativo. */
    EM_ANDAMENTO,
    /** A partida foi concluída e o resultado foi registrado. */
    FINALIZADO,
    /** A partida foi interrompida ou cancelada sem registro de resultado. */
    CANCELADO
}
