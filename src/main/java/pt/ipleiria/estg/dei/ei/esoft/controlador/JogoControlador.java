package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.EventoJogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

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
}
