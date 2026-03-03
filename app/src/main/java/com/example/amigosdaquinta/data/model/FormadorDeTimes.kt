package com.example.amigosdaquinta.domain

import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.TimeColor
import com.example.amigosdaquinta.data.model.JogadorNaFila
import com.example.amigosdaquinta.data.model.TimeFormado

/**
 * Classe de domínio responsável por toda a lógica de formação de times.
 *
 * Regras implementadas:
 * - Primeiro jogo requer mínimo de 22 jogadores e ao menos 2 goleiros.
 * - Jogadores com 2 jogos consecutivos são rotacionados (exceto goleiros).
 * - Goleiros podem jogar até 3 vezes consecutivas antes de rotacionar.
 * - Quando a fila está curta, jogadores já utilizados completam o time pelo critério
 *   de quem jogou há mais tempo (menor jogoId mais recente).
 *
 * Cada função pública retorna null quando as condições mínimas não são atendidas,
 * cabendo à camada de apresentação tratar esses casos.
 */
class FormadorDeTimes {

    /**
     * Forma os dois times para o primeiro jogo da sessão.
     *
     * Distribui os primeiros 2 goleiros e os primeiros 20 jogadores de linha
     * conforme a ordem de chegada em [jogadoresPresentes].
     *
     * @return Par (timeBranco, timeVermelho) ou null se houver menos de 22 jogadores ou menos de 2 goleiros.
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
     * Forma os times para o próximo jogo, mantendo o time vencedor e trazendo novos jogadores da fila.
     *
     * Jogadores do time vencedor com 2 jogos consecutivos são rotacionados para a fila,
     * exceto goleiros. O novo time adversário é formado com jogadores da fila de espera.
     *
     * @param timeVencedor Time que permanece em campo. Null indica empate — nesse caso retorna null.
     * @param filaEspera Jogadores aguardando para entrar.
     * @param historicoParticipacoes Participações recentes usadas para checar jogos consecutivos.
     * @return Par (timePermanente, timeNovo) ou null se não houver jogadores suficientes na fila.
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

    /**
     * Verifica quais jogadores do time jogaram os dois últimos jogos consecutivos.
     *
     * Considera consecutivo quando a diferença entre os dois últimos [jogoId] é <= 1.
     *
     * @return Lista de IDs dos jogadores que devem ser rotacionados.
     */
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

    /**
     * Monta um time adversário a partir da fila de espera, ignorando jogadores já em campo.
     *
     * @param filaEspera Jogadores disponíveis para entrar.
     * @param jogadoresAEvitar IDs dos jogadores que já estão no time permanente.
     * @return [TimeFormado] ou null se não houver ao menos 1 goleiro e 10 jogadores de linha disponíveis.
     */
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

    /**
     * Verifica se um goleiro ainda pode jogar mais uma rodada.
     * Goleiros são permitidos até 3 jogos consecutivos antes de rotacionar.
     *
     * @return true se o goleiro tiver menos de 3 participações recentes registradas.
     */
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

    /**
     * Completa um time que ficou abaixo de 11 jogadores após rotação,
     * preenchendo com jogadores que estão há mais tempo sem jogar.
     *
     * A prioridade é determinada pelo menor [jogoId] mais recente do jogador no histórico.
     * Jogadores sem histórico recebem prioridade máxima (jogoId = 0).
     *
     * @return [TimeFormado] completo com os jogadores adicionados.
     */
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