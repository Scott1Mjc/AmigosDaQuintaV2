# Amigos da Quinta

Aplicativo Android para gerenciar peladas semanais — controle de presença, formação automática de times, placar em tempo real e histórico de partidas.

---

## Funcionalidades

**Lista de presença**
- Registro de jogadores por ordem de chegada com horário
- Remoção durante a sessão (inativação lógica)
- Indicadores visuais de prontidão (22+, 30+, 33+ jogadores)

**Formação de times**
- Formação manual: o usuário monta cada time individualmente
- Formação automática com três modos:
- //Provavelmente Sairá do Projeto
    - Normal (menos de 30): time vencedor permanece, novo adversário vem da fila
    - Rotação forçada (30+): time anterior sai, dois times inteiramente novos
    - Rotação total (33+ no 1º jogo): todos entram por ordem de chegada

**Jogo em andamento**
- Cronômetro regressivo (30 min no 1º jogo, 15 min nos demais)
- Placar interativo com registro de gols por time
- Dialog de confirmação ao tentar abandonar o jogo

**Resultado e sessão**
- Tela de resultado com vencedor e placar final
- Fluxo contínuo: próximo jogo sem sair da sessão
- Encerramento com limpeza completa do estado

**Histórico**
- Jogos dos últimos 30 dias
- Detalhes por partida: escalações, placar, duração
- Estatísticas individuais: vitórias, derrotas, empates e aproveitamento

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose |
| Navegação | Navigation Compose |
| Banco de dados | Room |
| Assincronicidade | Coroutines + Flow |
| Arquitetura | MVVM + UseCase (domain layer) |

---

## Arquitetura

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          # JogadorDao, JogoDao, ParticipacaoDao, PresencaDao
│   │   ├── entity/       # Jogador, Jogo, Participacao, PresencaDia
│   │   └── AppDatabase   # Room singleton
│   ├── model/            # JogadorNaFila, SessaoJogos, TimeFormado, TimesFormados
│   └── repository/       # JogadorRepository, JogoRepository, ParticipacaoRepository, PresencaRepository
├── domain/
│   ├── FormadorDeTimes         # Lógica de formação manual e rotação
│   └── FormacaoTimesUseCase    # Lógica de formação automática (extraída da UI)
├── ui/
│   ├── components/       # SelecionarJogadorDialog (compartilhado)
│   ├── navigation/       # Screen.kt (rotas), NavGraph.kt (grafo)
│   └── screens/
│       ├── debug/        # DebugScreen
│       ├── formacao/     # FormacaoManualScreen, FormacaoAutomaticaScreen
│       ├── history/      # HistoricoScreen, DetalhesJogoScreen, EstatisticasJogadorScreen
│       ├── home/         # HomeScreen, JogadorItem, AdicionarJogadorDialog
│       ├── jogo/         # JogoScreen, ResultadoScreen, CronometroComponent, PlacarComponent
│       └── presenca/     # PresencaScreen, PresencaItem, SelecionarJogadorDialog
└── viewmodel/
    ├── AppViewModelFactory
    ├── JogadoresViewModel
    ├── SessaoViewModel     # Orquestra sessão + chama FormacaoTimesUseCase
    └── HistoricoViewModel
```

---

## Regras de negócio

**Duração dos jogos**
- 1º jogo da sessão: 30 minutos
- 2º jogo em diante: 15 minutos

**Rotação de jogadores**
- Menos de 30 presentes: time vencedor permanece em campo
- 30 a 32 presentes: todos que jogaram o jogo anterior saem; dois novos times são formados com quem estava na fila
- 33 ou mais presentes no 1º jogo: rotação total — todos entram por ordem de chegada
- Jogadores de linha com 2 jogos consecutivos são rotacionados automaticamente
- Goleiros podem jogar até 3 partidas consecutivas antes de rotacionar

**Empate**
- Em caso de empate, o time branco permanece por padrão

---

## Como rodar

**Pré-requisitos**
- Android Studio Hedgehog ou superior
- JDK 17
- Android SDK 34

**Passos**
```bash
git clone https://github.com/seu-usuario/amigos-da-quinta.git
cd amigos-da-quinta
```
Abra o projeto no Android Studio e execute em um emulador ou dispositivo físico (API 26+).

> O banco de dados é criado automaticamente na primeira execução com `fallbackToDestructiveMigration` ativo. Remover essa flag antes de publicar em produção.

---

## Tela de debug

A tela de debug está acessível via botão "D" na HomeScreen e permite popular o banco com 40 jogadores de teste e adicionar todos à lista de presença automaticamente. Não deve estar acessível em builds de produção.

---

## Roadmap

- [ ] Remover `fallbackToDestructiveMigration` e adicionar migrations explícitas
- [ ] Substituir factory manual por injeção de dependências (Hilt)
- [ ] Centralizar thresholds de rotação (22, 30, 33) em objeto de constantes
- [ ] Feedback visual (Snackbar) ao tentar adicionar segundo goleiro no mesmo time
- [ ] Filtro de data interativo na tela de histórico
- [ ] Tema customizado (cores e tipografia próprias)
- [ ] Separar `Screen.kt` e `NavGraph.kt` em módulo de navegação dedicado