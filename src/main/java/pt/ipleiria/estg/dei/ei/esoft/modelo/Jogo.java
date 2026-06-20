package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidade Jogo — núcleo dos casos de uso UC07 a UC12.
 *
 * Ciclo de vida (Estado):
 *   CALENDARIZADO -> COMEÇADO -> TERMINADO
 *
 * UC09 (Registar Início de Jogo) faz a transição CALENDARIZADO -> COMEÇADO.
 * UC11 (Terminar Jogo e Registar Resultado) faz a transição COMEÇADO -> TERMINADO.
 * UC10 (Registar Evento de Jogo) e UC12 (Corrigir Evento de Jogo) actuam
 * sobre a lista de eventos do jogo.
 */
public class Jogo {

    public enum Estado { CALENDARIZADO, COMECADO, TERMINADO }

    public enum Fase { GRUPOS, OITAVOS, QUARTOS, MEIAS, FINAL, TERCEIRO_LUGAR }

    private final Equipa equipa1;
    private final Equipa equipa2;
    private final Estadio estadio;
    private final LocalDateTime dataHora;
    private final Grupo grupo;       // null se for jogo de eliminatória
    private final Fase fase;
    private final Map<Bancada, Double> precosEspeciais = new HashMap<>();

    private Estado estado;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFim;

    private int golosEquipa1;
    private int golosEquipa2;

    private final List<EventoJogo> eventos;

    public Jogo(Equipa equipa1, Equipa equipa2, Estadio estadio,
                LocalDateTime dataHora, Grupo grupo, Fase fase) {

        if (equipa1 == null || equipa2 == null)
            throw new IllegalArgumentException("As duas equipas são obrigatórias.");
        if (equipa1 == equipa2)
            throw new IllegalArgumentException("Uma equipa não pode jogar contra ela própria.");
        if (estadio == null)
            throw new IllegalArgumentException("O estádio é obrigatório.");
        if (dataHora == null)
            throw new IllegalArgumentException("A data/hora do jogo é obrigatória.");
        if (fase == null)
            throw new IllegalArgumentException("A fase do jogo é obrigatória.");

        this.equipa1 = equipa1;
        this.equipa2 = equipa2;
        this.estadio = estadio;
        this.dataHora = dataHora;
        this.grupo = grupo;
        this.fase = fase;
        this.estado = Estado.CALENDARIZADO;
        this.eventos = new ArrayList<>();
        this.golosEquipa1 = 0;
        this.golosEquipa2 = 0;
    }

    // ── Getters básicos ────────────────────────────────────────────────────────
    public Equipa        getEquipaCasa()   { return equipa1; }
    public Equipa        getEquipaFora()   { return equipa2; }
    public Estadio       getEstadio()      { return estadio; }
    public LocalDateTime getDataHora()     { return dataHora; }
    public Grupo         getGrupo()        { return grupo; }
    public Fase          getFase()         { return fase; }
    public Estado        getEstado()       { return estado; }
    public LocalDateTime getHoraInicio()   { return horaInicio; }
    public LocalDateTime getHoraFim()      { return horaFim; }
    public int           getGolosCasa()    { return golosEquipa1; }
    public int           getGolosFora()    { return golosEquipa2; }

    public List<EventoJogo> getEventos() { return Collections.unmodifiableList(eventos); }

    public boolean envolveEquipa(Equipa equipa) {
        return equipa1 == equipa || equipa2 == equipa;
    }

    // ── UC09 — Registar Início de Jogo ────────────────────────────────────────

    /**
     * @throws IllegalStateException JOGO_NAO_CALENDARIZADO se o jogo já tiver começado/terminado.
     */
    public void iniciar() {
        if (estado != Estado.CALENDARIZADO)
            throw new IllegalStateException("JOGO_NAO_CALENDARIZADO");
        this.estado = Estado.COMECADO;
        this.horaInicio = LocalDateTime.now();
    }

    // ── UC10 — Registar Evento de Jogo ────────────────────────────────────────

    /**
     * Regista um novo evento no jogo, recalculando o marcador caso seja um golo.
     *
     * @throws IllegalStateException    JOGO_NAO_COMECADO se o jogo não estiver em curso.
     * @throws IllegalArgumentException JOGADOR_NAO_PERTENCE_EQUIPA / JOGADOR_INAPTO /
     *                                   JOGADOR_JA_EXPULSO conforme as regras do jogo.
     */
    public void registarEvento(EventoJogo evento) {
        // Antes era != Estado.COMECADO. Agora bloqueamos apenas se estiver CALENDARIZADO,
        // permitindo assim registar em COMEÇADO e TERMINADO.
        if (estado == Estado.CALENDARIZADO)
            throw new IllegalStateException("JOGO_NAO_COMECADO");
        if (!envolveEquipa(evento.getEquipa()))
            throw new IllegalArgumentException("EQUIPA_NAO_PARTICIPA_NO_JOGO");
        if (evento.getJogador().getEquipa() != evento.getEquipa())
            throw new IllegalArgumentException("JOGADOR_NAO_PERTENCE_EQUIPA");
        if (evento.getJogador().getEstado() != Jogador.Estado.APTO)
            throw new IllegalArgumentException("JOGADOR_INAPTO");
        if (jogadorJaExpulso(evento.getJogador()) && evento.getTipo() != EventoJogo.Tipo.SUBSTITUICAO)
            throw new IllegalArgumentException("JOGADOR_JA_EXPULSO");

        eventos.add(evento);
        recalcularMarcador();
    }

    /** Remove um evento do jogo e recalcula o marcador se necessário. */
    public void removerEvento(EventoJogo evento) {
        if (!eventos.contains(evento))
            throw new IllegalArgumentException("EVENTO_NAO_PERTENCE_AO_JOGO");

        eventos.remove(evento);
        recalcularMarcador();
    }

    /** Verifica se o jogador já tem um cartão vermelho registado neste jogo (sem ter sido corrigido para outro tipo). */
    public boolean jogadorJaExpulso(Jogador jogador) {
        return eventos.stream()
                .anyMatch(e -> e.getJogador() == jogador && e.getTipo() == EventoJogo.Tipo.CARTAO_VERMELHO);
    }

    /** Recalcula o marcador a partir dos eventos de tipo GOLO. */
    private void recalcularMarcador() {
        int g1 = 0, g2 = 0;
        for (EventoJogo e : eventos) {
            if (e.getTipo() != EventoJogo.Tipo.GOLO) continue;
            if (e.getEquipa() == equipa1) g1++;
            else if (e.getEquipa() == equipa2) g2++;
        }
        this.golosEquipa1 = g1;
        this.golosEquipa2 = g2;
    }

    // ── UC11 — Terminar Jogo e Registar Resultado ─────────────────────────────

    /**
     * @throws IllegalStateException JOGO_NAO_COMECADO se o jogo não estiver em curso.
     */
    public void terminar() {
        if (estado != Estado.COMECADO)
            throw new IllegalStateException("JOGO_NAO_COMECADO");
        recalcularMarcador();
        this.estado = Estado.TERMINADO;
        this.horaFim = LocalDateTime.now();
    }

    /** Indica se há empate (apenas relevante para jogos de eliminatória/fase final). */
    public boolean isEmpate() {
        return estado == Estado.TERMINADO && golosEquipa1 == golosEquipa2;
    }

    // ── UC12 — Corrigir Evento de Jogo ────────────────────────────────────────

    /**
     * Aplica a correção aos dados de um evento já existente no jogo e
     * recalcula o marcador. Usado pelo EventoControlador, que valida
     * previamente o prazo de correção.
     */
    public void corrigirEvento(EventoJogo evento, EventoJogo.Tipo novoTipo, Equipa novaEquipa, Jogador novoJogador, int novoMinuto) {
        corrigirEvento(evento, novoTipo, novaEquipa, novoJogador, novoMinuto, null);
    }

    public void corrigirEvento(EventoJogo evento, EventoJogo.Tipo novoTipo,
                               Equipa novaEquipa, Jogador novoJogador,
                               int novoMinuto, Jogador novaAssistencia) {
        if (!eventos.contains(evento))
            throw new IllegalArgumentException("EVENTO_NAO_PERTENCE_AO_JOGO");
        evento.aplicarCorrecao(novoTipo, novaEquipa, novoJogador, novoMinuto, novaAssistencia);
        recalcularMarcador();
    }

    @Override
    public String toString() {
        String resultado = (estado == Estado.CALENDARIZADO)
                ? "vs"
                : golosEquipa1 + " - " + golosEquipa2;
        return equipa1.getNome() + " " + resultado + " " + equipa2.getNome();
    }

    public void definirPrecoEspecial(Bancada bancada, double novoPreco) {
        if (bancada == null) throw new IllegalArgumentException("Bancada inválida.");
        if (novoPreco <= 0) throw new IllegalArgumentException("Preço deve ser positivo.");
        precosEspeciais.put(bancada, novoPreco);
    }

    public Double getPrecoEspecial(Bancada bancada) {
        return precosEspeciais.get(bancada);
    }
}
