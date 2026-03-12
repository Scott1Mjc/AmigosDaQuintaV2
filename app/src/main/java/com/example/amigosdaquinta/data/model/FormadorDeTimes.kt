package com.example.amigosdaquinta.data.model

import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.data.model.JogadorNaFila
import com.example.amigosdaquinta.data.model.TimeFormado

/**
 * Classe de domínio responsável pela lógica de formação e rotação de times.
 * 
 * Implementa as regras de pelada:
 * - Primeiro jogo: 22 jogadores (2 goleiros + 20 linha).
 * - Rotação: Jogadores de linha jogam no máximo 2 partidas seguidas.
 * - Goleiros: Podem jogar até 3 partidas seguidas.
 * - Fila Curta: Jogadores que já atuaram completam os times conforme tempo de descanso.
 */
class FormadorDeTimes {

    /**
     * Forma os dois times para a partida de abertura da sessão.
     * 
     * @param jogadoresPresentes Lista de atletas na fila por ordem de chegada.
     * @return Par de times (Branco, Vermelho) ou null se houver atletas insuficientes.
     */
    fun formarPrimeiroJogo(
        jogadoresPresentes: List<JogadorNaFila>
    ): Pair<TimeFormado, TimeFormado>? {
        if (jogadoresPresentes.size < 22) return null

        val goleiros = jogadoresPresentes.filter { it.jogador.isPosicaoGoleiro }
        val linha = jogadoresPresentes.filter { !it.jogador.isPosicaoGoleiro }

        if (goleiros.size < 2) return null

        val timeBranco = TimeFormado(
            cor = TimeColor.BRANCO,
            goleiro = goleiros[0],
            jogadores = linha.take(10)
        )

        val timeVermelho = TimeFormado(
            cor = TimeColor.VERMELHO,
            goleiro = goleiros[1],
            jogadores = linha.drop(10).take(10)
        )

        return Pair(timeBranco, timeVermelho)
    }

    /**
     * Forma as equipes para o próximo jogo mantendo a base vencedora e rotacionando a fila.
     * 
     * @param timeVencedor Time que permaneceu em campo.
     * @param filaEspera Atletas aguardando para entrar.
     * @param historicoParticipacoes Dados recentes para validar jogos consecutivos.
     */
    fun formarProximoJogo(
        timeVencedor: TimeFormado?,
        filaEspera: List<JogadorNaFila>,
        historicoParticipacoes: List<Participacao>
    ): Pair<TimeFormado, TimeFormado>? {
        val timeQuePermanece = timeVencedor ?: return null

        val idsComDoisJogosConsecutivos = verificarDoisJogosConsecutivos(
            timeQuePermanece.todosJogadores.map { it.jogador.id },
            historicoParticipacoes
        )

        val jogadoresPermanentes = if (idsComDoisJogosConsecutivos.isNotEmpty()) {
            timeQuePermanece.todosJogadores.filter { jogador ->
                jogador.jogador.id !in idsComDoisJogosConsecutivos || jogador.jogador.isPosicaoGoleiro
            }
        } else {
            timeQuePermanece.todosJogadores
        }

        val novoTime = formarTimeComFila(
            filaEspera = filaEspera,
            jogadoresAEvitar = jogadoresPermanentes.map { it.jogador.id }
        ) ?: return null

        return Pair(
            TimeFormado(
                cor = TimeColor.BRANCO,
                goleiro = jogadoresPermanentes.first { it.jogador.isPosicaoGoleiro },
                jogadores = jogadoresPermanentes.filter { !it.jogador.isPosicaoGoleiro }
            ),
            novoTime
        )
    }

    private fun verificarDoisJogosConsecutivos(
        jogadoresIds: List<Long>,
        historicoParticipacoes: List<Participacao>
    ): List<Long> {
        return historicoParticipacoes
            .filter { it.jogadorId in jogadoresIds }
            .groupBy { it.jogadorId }
            .filter { (_, participacoes) ->
                val ordenadas = participacoes.sortedByDescending { it.jogoId }
                if (ordenadas.size < 2) return@filter false
                ordenadas[0].jogoId - ordenadas[1].jogoId <= 1
            }
            .keys
            .toList()
    }

    private fun formarTimeComFila(
        filaEspera: List<JogadorNaFila>,
        jogadoresAEvitar: List<Long> = emptyList()
    ): TimeFormado? {
        val disponiveis = filaEspera.filter { it.jogador.id !in jogadoresAEvitar }

        if (disponiveis.size < 11) return null

        val goleiro = disponiveis.firstOrNull { it.jogador.isPosicaoGoleiro }
        val linha = disponiveis.filter { !it.jogador.isPosicaoGoleiro }.take(10)

        if (goleiro == null || linha.size < 10) return null

        return TimeFormado(
            cor = TimeColor.VERMELHO,
            goleiro = goleiro,
            jogadores = linha
        )
    }

    /** Permite que goleiros atuem em até 3 partidas seguidas. */
    fun permitirGoleirosJogarem3Vezes(
        goleiro: JogadorNaFila,
        historicoParticipacoes: List<Participacao>
    ): Boolean {
        val participacoesRecentes = historicoParticipacoes
            .filter { it.jogadorId == goleiro.jogador.id }
            .sortedByDescending { it.jogoId }
            .take(3)

        return participacoesRecentes.size < 3
    }

    /** Preenche vagas remanescentes no time com jogadores que estão há mais tempo sem atuar. */
    fun completarComJogadoresUsados(
        timeIncompleto: TimeFormado,
        jogadoresDisponiveis: List<JogadorNaFila>,
        historicoParticipacoes: List<Participacao>
    ): TimeFormado {
        val vagasFaltando = 11 - timeIncompleto.todosJogadores.size

        if (vagasFaltando <= 0) return timeIncompleto

        val idsNoTime = timeIncompleto.todosJogadores.map { it.jogador.id }

        val novosJogadores = jogadoresDisponiveis
            .filter { it.jogador.id !in idsNoTime && !it.jogador.isPosicaoGoleiro }
            .sortedBy { jogador ->
                historicoParticipacoes
                    .filter { it.jogadorId == jogador.jogador.id }
                    .maxOfOrNull { it.jogoId } ?: 0
            }
            .take(vagasFaltando)

        return TimeFormado(
            cor = timeIncompleto.cor,
            goleiro = timeIncompleto.goleiro,
            jogadores = timeIncompleto.jogadores + novosJogadores
        )
    }
}
