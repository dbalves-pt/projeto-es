package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.time.LocalDateTime;

/**
 * Entidade EventoJogo — UC10 (Registar Evento de Jogo), UC11 (Terminar Jogo
 * e Registar Resultado) e UC12 (Corrigir Evento de Jogo).
 *
 * Representa um acontecimento ocorrido durante um jogo (golo, cartão,
 * substituição, etc.), associado a uma equipa, um jogador e um minuto.
 */
public class EventoJogo {

    public enum Tipo {
        GOLO("Golo"),
        ASSISTENCIA("Assistência"),
        CARTAO_AMARELO("Cartão Amarelo"),
        CARTAO_VERMELHO("Cartão Vermelho"),
        SUBSTITUICAO("Substituição"),
        DEFESA("Defesa");

        private final String descricao;
        Tipo(String descricao) { this.descricao = descricao; }

        @Override
        public String toString() { return descricao; }

        public static Tipo fromDescricao(String descricao) {
            for (Tipo t : values()) {
                if (t.descricao.equalsIgnoreCase(descricao)) return t;
            }
            throw new IllegalArgumentException("Tipo de evento inválido: " + descricao);
        }
    }

    private Tipo tipo;
    private Equipa equipa;
    private Jogador jogador;
    private int minuto;
    private Jogador assistencia;

    // Auditoria (UC12 — Corrigir Evento)
    private LocalDateTime registadoEm;
    private boolean corrigido;
    private LocalDateTime corrigidoEm;

    public EventoJogo(Tipo tipo, Equipa equipa, Jogador jogador, int minuto) {
        this(tipo, equipa, jogador, minuto, null);
    }

    public EventoJogo(Tipo tipo, Equipa equipa, Jogador jogador, int minuto, Jogador assistencia) {
        validar(tipo, equipa, jogador, minuto, assistencia);
        this.tipo = tipo;
        this.equipa = equipa;
        this.jogador = jogador;
        this.minuto = minuto;
        this.assistencia = assistencia;
        this.registadoEm = LocalDateTime.now();
        this.corrigido = false;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public Tipo            getTipo()        { return tipo; }
    public Equipa          getEquipa()      { return equipa; }
    public Jogador         getJogador()     { return jogador; }
    public int             getMinuto()      { return minuto; }
    public LocalDateTime   getRegistadoEm() { return registadoEm; }
    public boolean         isCorrigido()    { return corrigido; }
    public LocalDateTime   getCorrigidoEm() { return corrigidoEm; }
    public Jogador getAssistencia() { return assistencia; }

    // ── Correção (UC12) ───────────────────────────────────────────────────────

    /**
     * Aplica uma correção aos dados do evento, marcando-o como corrigido
     * e registando o instante da alteração para efeitos de auditoria.
     */
    public void aplicarCorrecao(Tipo novoTipo, Equipa novaEquipa, Jogador novoJogador, int novoMinuto) {
        aplicarCorrecao(novoTipo, novaEquipa, novoJogador, novoMinuto, null);
    }

    // <-- ADICIONA ESTA VERSÃO COMPLETA PARA A CORREÇÃO
    public void aplicarCorrecao(Tipo novoTipo, Equipa novaEquipa, Jogador novoJogador, int novoMinuto, Jogador novaAssistencia) {
        validar(novoTipo, novaEquipa, novoJogador, novoMinuto, novaAssistencia);
        this.tipo = novoTipo;
        this.equipa = novaEquipa;
        this.jogador = novoJogador;
        this.minuto = novoMinuto;
        this.assistencia = novaAssistencia;
        this.corrigido = true;
        this.corrigidoEm = LocalDateTime.now();
    }

    private void validar(Tipo tipo, Equipa equipa, Jogador jogador, int minuto) {
        validar(tipo, equipa, jogador, minuto, null);
    }

    private void validar(Tipo tipo, Equipa equipa, Jogador jogador, int minuto, Jogador assistencia) {
        if (tipo == null) throw new IllegalArgumentException("O tipo de evento é obrigatório.");
        if (equipa == null) throw new IllegalArgumentException("A equipa é obrigatória.");
        if (jogador == null) throw new IllegalArgumentException("O jogador é obrigatório.");
        if (minuto < 0 || minuto > 130)
            throw new IllegalArgumentException("Minuto inválido.");

        // Validação da assistência: impede auto-assistência
        if (tipo == Tipo.GOLO && assistencia != null && assistencia.equals(jogador)) {
            throw new IllegalArgumentException("O jogador não pode fazer uma assistência para si próprio.");
        }
    }

    @Override
    public String toString() {
        String texto = minuto + "' — " + tipo + " — " + jogador.getNomeCompleto() + " (" + equipa.getNome() + ")";
        if (tipo == Tipo.GOLO && assistencia != null) {
            texto += " [Assistência de: " + assistencia.getNomeCompleto() + "]";
        }
        return texto;
    }

    /**
     * Usado pelos testes unitários para simular que um evento foi registado
     * há mais tempo do que realmente foi, permitindo testar o prazo de
     * correcção (UC12, CA 1.1/3.1) sem depender de esperas reais.
     * Não deve ser usado fora de contexto de teste.
     */
    public void forcarRegistadoEmParaTeste(LocalDateTime instante) {
        this.registadoEm = instante;
    }
}
