package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador UC07 (Configurar Torneio) e UC08 (Validar e Iniciar Torneio).
 *
 * UC07 — Caminho Principal:
 *   1. Utilizador preenche datas, define regras (dias de descanso, número
 *      total de equipas) e clica em 'Gerar torneio'.
 *   2. Sistema valida datas, número de equipas, dias de descanso e se as
 *      equipas têm jogadores APTOS.
 *   3. Sistema distribui as equipas, gera os jogos e altera o estado para
 *      CONFIGURADO, bloqueando a edição de equipas e jogadores.
 *
 * UC08 — Caminho Principal:
 *   1. Utilizador clica em 'Validar Calendário'.
 *   2. Sistema verifica conflitos de horários/estádios/dias de descanso.
 *   3. Sistema apresenta o relatório; se sem conflitos, activa 'Confirmar Validação'.
 *   4. Utilizador confirma -> estado passa a VALIDADO.
 *   5. Utilizador clica em 'Iniciar Torneio' -> diálogo de confirmação -> estado EM_CURSO.
 */
public class TorneioControlador {

    private final Torneio torneio;

    public TorneioControlador()            { this.torneio = Torneio.getInstancia(); }
    public TorneioControlador(Torneio t)   { this.torneio = t; }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC07 — Configurar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida e gera o torneio (grupos + calendário de jogos da fase de grupos).
     *
     * Códigos de erro lançados (IllegalArgumentException / IllegalStateException):
     *   DATAS_INVALIDAS            – CA 4.1: datas em falta, mal formatadas ou fim antes do início.
     *   EQUIPAS_INCOMPATIVEIS      – CA 4.2: número de equipas não é par nem divisível por 4.
     *   EQUIPAS_SEM_JOGADORES      – CA 5.1: pelo menos uma equipa sem jogadores APTOS.
     *   SEM_ESTADIOS               – CA 6.1: não existe nenhum estádio criado para alocar os jogos.
     */
    // ══════════════════════════════════════════════════════════════════════════
    //  UC07 — Configurar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida e gera o torneio (grupos + calendário de jogos da fase de grupos).
     *
     * Códigos de erro lançados (IllegalArgumentException / IllegalStateException):
     * DATAS_INVALIDAS            – CA 4.1: datas em falta, mal formatadas ou fim antes do início.
     * EQUIPAS_INCOMPATIVEIS      – CA 4.2: número de equipas não é par nem divisível por 4.
     * EQUIPAS_SEM_JOGADORES      – CA 5.1: pelo menos uma equipa sem jogadores APTOS.
     * SEM_ESTADIOS               – CA 6.1: não existe nenhum estádio criado para alocar os jogos.
     */
    // ══════════════════════════════════════════════════════════════════════════
    //  UC07 — Configurar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida e gera o torneio (grupos + calendário de jogos da fase de grupos).
     *
     * Códigos de erro lançados (IllegalArgumentException / IllegalStateException):
     * DATAS_INVALIDAS            – CA 4.1: datas em falta, mal formatadas ou fim antes do início.
     * EQUIPAS_INCOMPATIVEIS      – CA 4.2: número de equipas não é par nem divisível por 4.
     * EQUIPAS_SEM_JOGADORES      – CA 5.1: pelo menos uma equipa sem jogadores APTOS.
     * SEM_ESTADIOS               – CA 6.1: não existe nenhum estádio criado para alocar os jogos.
     */
    public void configurarTorneio(LocalDate dataInicio, LocalDate dataFim, int diasDescanso) {

        // CA 4.1 — Datas inválidas (básicas)
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio))
            throw new IllegalArgumentException("DATAS_INVALIDAS");

        // ── VALIDAÇÃO: DATA DE INÍCIO APÓS O DIA DE HOJE ──
        if (dataInicio.isBefore(LocalDate.now()) || dataInicio.isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("A data de início do torneio tem de ser posterior ao dia de hoje.");
        }

        // ── VALIDAÇÃO: DURAÇÃO MÍNIMA DE 30 DIAS ──
        long diasDeTorneio = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim);
        if (diasDeTorneio < 30) {
            throw new IllegalArgumentException("O torneio tem de ter uma duração mínima de 30 dias.");
        }

        if (diasDescanso < 0)
            throw new IllegalArgumentException("DIAS_DESCANSO_INVALIDOS");

        List<Equipa> equipas = torneio.getEquipas();

        // CA 4.2 — Equipas incompatíveis com grupos (grupos de 4 equipas)
        if (equipas.isEmpty() || equipas.size() % 4 != 0)
            throw new IllegalArgumentException("EQUIPAS_INCOMPATIVEIS");

        // ── NOVA VALIDAÇÃO: OBRIGA A TER TREINADOR ──
        List<String> equipasSemTreinador = new ArrayList<>();
        for (Equipa eq : equipas) {
            if (eq.getTreinador() == null || eq.getTreinador().isBlank() || eq.getTreinador().equals("Sem Treinador")) {
                equipasSemTreinador.add(eq.getNome());
            }
        }
        if (!equipasSemTreinador.isEmpty()) {
            throw new IllegalArgumentException("As seguintes equipas não têm um treinador atribuído:\n" + String.join(", ", equipasSemTreinador) + "\n\nEdite estas equipas antes de gerar o torneio.");
        }
        // ────────────────────────────────────────────

        // CA 5.1 — Equipas sem jogadores APTOS suficientes
        List<String> equipasSemJogadores = equipasSemJogadoresAptos();
        if (!equipasSemJogadores.isEmpty())
            throw new IllegalArgumentException("EQUIPAS_SEM_JOGADORES: " + String.join(", ", equipasSemJogadores));

        // CA 6.1 — Impossível alocar jogos (sem estádios disponíveis)
        if (torneio.getEstadios().isEmpty())
            throw new IllegalStateException("SEM_ESTADIOS");

        torneio.definirRegras(dataInicio, dataFim, diasDescanso, equipas.size());
        torneio.gerarGruposEJogos();
        torneio.setEstado(Torneio.Estado.CONFIGURADO);
    }



    /** Lista as equipas que não têm nenhum jogador no estado APTO. */
    public List<String> equipasSemJogadoresAptos() {
        List<String> resultado = new ArrayList<>();
        for (Equipa eq : torneio.getEquipas()) {
            boolean temApto = eq.getJogadores().stream()
                    .anyMatch(j -> j.getEstado() == Jogador.Estado.APTO);
            if (!temApto) resultado.add(eq.getNome());
        }
        return resultado;
    }

    public boolean isConfiguracaoBloqueada() { return torneio.gruposGerados(); }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC08 — Validar e Iniciar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * @return lista de conflitos encontrados (vazia significa "sem conflitos").
     * @throws IllegalStateException ESTADO_INVALIDO se o torneio ainda não estiver CONFIGURADO.
     */
    public List<String> validarCalendario() {
        if (torneio.getEstado() != Torneio.Estado.CONFIGURADO
                && torneio.getEstado() != Torneio.Estado.VALIDADO)
            throw new IllegalStateException("ESTADO_INVALIDO");
        return torneio.validarCalendario();
    }

    /**
     * @throws IllegalStateException ESTADO_INVALIDO / CONFLITOS_EXISTENTES.
     */
    public void confirmarValidacao() {
        torneio.confirmarValidacao();
    }

    /**
     * @throws IllegalStateException ESTADO_INVALIDO se o torneio não estiver VALIDADO.
     */
    public void iniciarTorneio() {
        torneio.iniciarTorneio();
    }

    public boolean podeValidar()       { return torneio.getEstado() == Torneio.Estado.CONFIGURADO; }
    public boolean podeIniciar()       { return torneio.getEstado() == Torneio.Estado.VALIDADO; }
    public boolean torneioEmCurso()    { return torneio.getEstado() == Torneio.Estado.EM_CURSO; }

    // ── Auxiliares para a Vista ───────────────────────────────────────────────
    public List<Grupo> getGrupos()   { return torneio.getGrupos(); }
    public List<Jogo>  getJogos()    { return torneio.getJogos(); }
    public Torneio.Estado getEstadoTorneio() { return torneio.getEstado(); }

    /**
     * Gera os jogos da Fase Eliminatória (Quartos de Final) cruzando os
     * 1ºs e 2ºs classificados dos 4 grupos.
     */
    public void gerarQuartosDeFinal() {
        pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio torneio = pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.getInstancia();

        // 1. Verificar se a fase de grupos já acabou (todos os jogos de GRUPOS estão TERMINADOS)
        boolean todosGruposTerminados = torneio.getJogos().stream()
                .filter(j -> j.getFase() == Jogo.Fase.GRUPOS)
                .allMatch(j -> j.getEstado() == Jogo.Estado.TERMINADO);

        if (!todosGruposTerminados) {
            throw new IllegalStateException("Ainda existem jogos da Fase de Grupos por terminar!");
        }

        // 2. Verificar se já não gerámos os quartos de final antes (para não duplicar)
        boolean jaTemQuartos = torneio.getJogos().stream()
                .anyMatch(j -> j.getFase() == Jogo.Fase.QUARTOS);

        if (jaTemQuartos) {
            throw new IllegalStateException("A fase eliminatória já foi gerada!");
        }

        List<pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo> grupos = torneio.getGrupos();
        if (grupos.size() < 4) {
            throw new IllegalStateException("É necessário ter pelo menos 4 grupos para gerar Quartos de Final.");
        }

        /* 3. Obter as equipas apuradas (Cruzamento clássico de torneios) */
        List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> todosJogos = torneio.getJogos();

        Equipa q1Casa = grupos.get(0).calcularClassificacao(todosJogos).get(0).getEquipa(); // 1º Grupo A
        Equipa q1Fora = grupos.get(1).calcularClassificacao(todosJogos).get(1).getEquipa(); // 2º Grupo B

        Equipa q2Casa = grupos.get(2).calcularClassificacao(todosJogos).get(0).getEquipa(); // 1º Grupo C
        Equipa q2Fora = grupos.get(3).calcularClassificacao(todosJogos).get(1).getEquipa(); // 2º Grupo D

        Equipa q3Casa = grupos.get(1).calcularClassificacao(todosJogos).get(0).getEquipa(); // 1º Grupo B
        Equipa q3Fora = grupos.get(0).calcularClassificacao(todosJogos).get(1).getEquipa(); // 2º Grupo A

        Equipa q4Casa = grupos.get(3).calcularClassificacao(todosJogos).get(0).getEquipa(); // 1º Grupo D
        Equipa q4Fora = grupos.get(2).calcularClassificacao(todosJogos).get(1).getEquipa(); // 2º Grupo C

        // 4. Agendar os jogos (vamos buscar o primeiro estádio da lista e a data do último jogo)
        pt.ipleiria.estg.dei.ei.esoft.modelo.Estadio estadio = torneio.getEstadios().get(0);
        java.time.LocalDateTime ultimaData = torneio.getJogos().stream()
                .map(Jogo::getDataHora)
                .max(java.time.LocalDateTime::compareTo)
                .orElse(java.time.LocalDateTime.now());

        // 5. Adicionar os 4 jogos ao torneio (sem pertencerem a nenhum grupo, daí o 'null')
        torneio.adicionarJogo(new Jogo(q1Casa, q1Fora, estadio, ultimaData.plusDays(2), null, Jogo.Fase.QUARTOS));
        torneio.adicionarJogo(new Jogo(q2Casa, q2Fora, estadio, ultimaData.plusDays(3), null, Jogo.Fase.QUARTOS));
        torneio.adicionarJogo(new Jogo(q3Casa, q3Fora, estadio, ultimaData.plusDays(4), null, Jogo.Fase.QUARTOS));
        torneio.adicionarJogo(new Jogo(q4Casa, q4Fora, estadio, ultimaData.plusDays(5), null, Jogo.Fase.QUARTOS));
    }
}

