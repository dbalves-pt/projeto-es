package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.EventoJogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controlador UC09 (Registar Início de Jogo) e UC11 (Terminar Jogo e
 * Registar Resultado).
 *
 * UC09 — Caminho Principal:
 *   1. Utilizador clica no separador 'Calendário'.
 *   2. Sistema apresenta os grupos e a tabela 'Jogos'.
 *   3. Utilizador clica num jogo da tabela.
 *   4. Sistema apresenta o ecrã 'Detalhes do Jogo'.
 *   5. Utilizador clica em 'Iniciar Jogo'.
 *   6. Sistema regista a hora, altera o estado para COMEÇADO, bloqueia a
 *      venda de bilhetes e apresenta o ecrã 'Registo de Eventos'.
 *
 * UC11 — Caminho Principal:
 *   1. Utilizador clica em 'Terminar Jogo'.
 *   2. Sistema apresenta o 'Resumo do Jogo'.
 *   3. Utilizador confirma -> sistema regista hora, altera estado para
 *      TERMINADO e calcula pontos.
 *   4-9. Sistema actualiza classificação, recalcula estatísticas e, se
 *      todos os jogos do grupo terminaram, apura as duas equipas seguintes.
 */
public class JogoControlador {

    /** CA 5.2 — Início muito antecipado: aviso se faltar mais do que isto para a hora calendarizada. */
    private static final long MINUTOS_ANTECIPACAO_MAXIMA = 60;

    private final Torneio torneio;

    public JogoControlador()           { this.torneio = Torneio.getInstancia(); }
    public JogoControlador(Torneio t)  { this.torneio = t; }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC09 — Registar Início de Jogo
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Indica se o jogo é candidato ao aviso de "início muito antecipado" (CA 5.2),
     * isto é, se a hora actual antecede a hora calendarizada por mais do que a
     * margem definida em {@link #MINUTOS_ANTECIPACAO_MAXIMA}.
     */
    public boolean isInicioMuitoAntecipado(Jogo jogo) {
        long minutosParaInicio = ChronoUnit.MINUTES.between(LocalDateTime.now(), jogo.getDataHora());
        return minutosParaInicio > MINUTOS_ANTECIPACAO_MAXIMA;
    }

    /**
     * Inicia o jogo (transição CALENDARIZADO -> COMEÇADO).
     *
     * @throws IllegalArgumentException JOGO_NULO se o jogo não for fornecido.
     * @throws IllegalStateException    JOGO_NAO_CALENDARIZADO — CA 5.1: jogo já iniciado ou terminado.
     */
    public void iniciarJogo(Jogo jogo) {
        if (jogo == null) throw new IllegalArgumentException("JOGO_NULO");

        // UC08: só permite iniciar jogo se o torneio estiver EM_CURSO
        Torneio torneio = Torneio.getInstancia();
        if (torneio.getEstado() != Torneio.Estado.EM_CURSO) {
            throw new IllegalStateException("TORNEIO_NAO_INICIADO");
        }

        jogo.iniciar();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC11 — Terminar Jogo e Registar Resultado
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Termina o jogo (transição COMEÇADO -> TERMINADO), recalcula a
     * classificação do respectivo grupo e devolve as duas equipas apuradas
     * caso todos os jogos do grupo já tenham terminado.
     *
     * @throws IllegalArgumentException JOGO_NULO.
     * @throws IllegalStateException    JOGO_NAO_COMECADO — o jogo não está em curso.
     *
     * @return as duas equipas apuradas do grupo, ou lista vazia se o grupo
     *         ainda não tiver todos os jogos terminados (ou se for jogo da
     *         fase eliminatória, sem grupo associado).
     */
    public List<Equipa> terminarJogo(Jogo jogo) {
        if (jogo == null) throw new IllegalArgumentException("JOGO_NULO");

        jogo.terminar();

        Grupo grupo = jogo.getGrupo();
        if (grupo == null) return List.of();

        if (grupo.todosOsJogosTerminaram(torneio.getJogos())) {
            List<Grupo.LinhaClassificacao> classificacao = grupo.calcularClassificacao(torneio.getJogos());
            return classificacao.stream()
                    .limit(2)
                    .map(Grupo.LinhaClassificacao::getEquipa)
                    .toList();
        }
        return List.of();
    }

    /** CA 3.2 — Inconsistência nos eventos: alerta se o número de golos do marcador difere dos eventos GOLO registados. */
    public boolean haInconsistenciaNosEventos(Jogo jogo) {
        long golosRegistados = jogo.getEventos().stream()
                .filter(e -> e.getTipo() == EventoJogo.Tipo.GOLO)
                .count();
        return golosRegistados != (jogo.getGolosCasa() + jogo.getGolosFora());
    }

    // ── Auxiliares para a Vista ───────────────────────────────────────────────

    public List<Jogo> getJogos() { return torneio.getJogos(); }

    public List<Jogo> getJogosPorGrupo(Grupo grupo) {
        return torneio.getJogos().stream().filter(j -> j.getGrupo() == grupo).toList();
    }

    public List<Grupo.LinhaClassificacao> getClassificacao(Grupo grupo) {
        return grupo.calcularClassificacao(torneio.getJogos());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Dashboard Dinâmica — Estatísticas Agregadas do Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Conta os golos marcados por cada jogador, considerando apenas eventos
     * de jogos já TERMINADOS (evita "Melhor Marcador" a oscilar com jogos
     * a meio, onde um evento ainda pode ser corrigido — UC12).
     */
    private Map<Jogador, Long> contarEventosPorJogador(EventoJogo.Tipo tipo) {
        return torneio.getJogos().stream()
                .filter(j -> j.getEstado() == Jogo.Estado.TERMINADO)
                .flatMap(j -> j.getEventos().stream())
                .filter(e -> e.getTipo() == tipo)
                .collect(java.util.stream.Collectors.groupingBy(
                        EventoJogo::getJogador, java.util.stream.Collectors.counting()));
    }

    /** Devolve o par (jogador, total) do jogador com mais eventos de um dado tipo. */
    private Optional<Map.Entry<Jogador, Long>> obterTopJogadorPorTipo(EventoJogo.Tipo tipo) {
        return contarEventosPorJogador(tipo).entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    /** UC — Melhor Marcador: nome do jogador com mais golos (jogos terminados). */
    public String obterNomeMelhorMarcador() {
        return obterTopJogadorPorTipo(EventoJogo.Tipo.GOLO)
                .map(e -> e.getKey().getNomeCompleto())
                .orElse("—");
    }

    public String obterGolosMelhorMarcador() {
        return obterTopJogadorPorTipo(EventoJogo.Tipo.GOLO)
                .map(e -> String.valueOf(e.getValue()))
                .orElse("0");
    }

    /** Jogador com mais assistências. */
    public String obterNomeMaisAssistencias() {
        return obterTopJogadorPorTipo(EventoJogo.Tipo.ASSISTENCIA)
                .map(e -> e.getKey().getNomeCompleto())
                .orElse("—");
    }

    public String obterNumeroMaisAssistencias() {
        return obterTopJogadorPorTipo(EventoJogo.Tipo.ASSISTENCIA)
                .map(e -> String.valueOf(e.getValue()))
                .orElse("0");
    }

    /**
     * Golos marcados por equipa, somando apenas jogos TERMINADOS.
     * Usa golosCasa/golosFora do Jogo (já recalculados em recalcularMarcador()),
     * em vez de percorrer eventos outra vez — mais barato e sempre consistente
     * com o marcador oficial do jogo.
     */
    private Map<Equipa, Integer> golosMarcadosPorEquipa() {
        Map<Equipa, Integer> mapa = new HashMap<>();
        for (Jogo j : torneio.getJogos()) {
            if (j.getEstado() != Jogo.Estado.TERMINADO) continue;
            mapa.merge(j.getEquipaCasa(), j.getGolosCasa(), Integer::sum);
            mapa.merge(j.getEquipaFora(), j.getGolosFora(), Integer::sum);
        }
        return mapa;
    }

    private Map<Equipa, Integer> golosSofridosPorEquipa() {
        Map<Equipa, Integer> mapa = new HashMap<>();
        for (Jogo j : torneio.getJogos()) {
            if (j.getEstado() != Jogo.Estado.TERMINADO) continue;
            mapa.merge(j.getEquipaCasa(), j.getGolosFora(), Integer::sum);
            mapa.merge(j.getEquipaFora(), j.getGolosCasa(), Integer::sum);
        }
        return mapa;
    }

    /** Equipa com melhor ataque (mais golos marcados). */
    public String obterEquipaMelhorAtaque() {
        return golosMarcadosPorEquipa().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().getNome())
                .orElse("—");
    }

    public String obterGolosMarcadosMelhorAtaque() {
        return golosMarcadosPorEquipa().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.valueOf(e.getValue()))
                .orElse("0");
    }

    /**
     * Equipa com melhor defesa (menos golos sofridos).
     * Só considera equipas que já têm pelo menos 1 jogo TERMINADO —
     * caso contrário, uma equipa que ainda não jogou "ganharia" sempre
     * por ter 0 golos sofridos.
     */
    public String obterEquipaMelhorDefesa() {
        return golosSofridosPorEquipa().entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(e -> e.getKey().getNome())
                .orElse("—");
    }

    public String obterGolosSofridosMelhorDefesa() {
        return golosSofridosPorEquipa().entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(e -> String.valueOf(e.getValue()))
                .orElse("0");
    }

    /**
     * Vencedor do torneio: equipa vencedora do jogo de fase FINAL, apenas
     * quando esse jogo já está TERMINADO. Antes disso devolve "—".
     */
    public String obterVencedorTorneio() {
        return torneio.getJogos().stream()
                .filter(j -> j.getFase() == Jogo.Fase.FINAL && j.getEstado() == Jogo.Estado.TERMINADO)
                .findFirst()
                .map(j -> j.getGolosCasa() > j.getGolosFora() ? j.getEquipaCasa().getNome()
                        : j.getGolosFora() > j.getGolosCasa() ? j.getEquipaFora().getNome()
                        : "Empate")
                .orElse("—");
    }
}


