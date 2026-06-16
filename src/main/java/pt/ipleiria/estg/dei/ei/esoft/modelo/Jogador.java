package pt.ipleiria.estg.dei.ei.esoft.modelo;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Entidade Jogador — UC03 (Adicionar Jogador a Equipa)
 *
 * Atributos conforme o formulário Figma:
 *   Nome Completo | Equipa (bloqueado) | Posição | Data de Nascimento | Número de Camisola | Estado
 */
public class Jogador {

    // ── Enumerações conforme o domínio do problema ─────────────────────────────
    public enum Posicao {
        GUARDA_REDES("Guarda-Redes"),
        DEFESA("Defesa"),
        MEDIO("Médio"),
        AVANCADO("Avançado");

        private final String descricao;
        Posicao(String descricao) { this.descricao = descricao; }

        @Override
        public String toString() { return descricao; }

        public static Posicao fromDescricao(String descricao) {
            for (Posicao p : values()) {
                if (p.descricao.equalsIgnoreCase(descricao)) return p;
            }
            throw new IllegalArgumentException("Posição inválida: " + descricao);
        }
    }

    public enum Estado {
        APTO("Apto"),
        INAPTO("Inapto");

        private final String descricao;
        Estado(String descricao) { this.descricao = descricao; }

        @Override
        public String toString() { return descricao; }

        public static Estado fromDescricao(String descricao) {
            for (Estado e : values()) {
                if (e.descricao.equalsIgnoreCase(descricao)) return e;
            }
            throw new IllegalArgumentException("Estado inválido: " + descricao);
        }
    }

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Atributos ──────────────────────────────────────────────────────────────
    private String    nomeCompleto;
    private Equipa    equipa;           // Bloqueado no formulário — vem do contexto
    private Posicao   posicao;
    private LocalDate dataNascimento;
    private int       numeroCamisola;   // Único dentro da equipa
    private Estado    estado;

    public Jogador(String nomeCompleto,
                   Equipa equipa,
                   Posicao posicao,
                   LocalDate dataNascimento,
                   int numeroCamisola,
                   Estado estado) {

        validarNome(nomeCompleto);
        if (equipa == null)        throw new IllegalArgumentException("A equipa não pode ser nula.");
        if (posicao == null)       throw new IllegalArgumentException("A posição é obrigatória.");
        if (dataNascimento == null) throw new IllegalArgumentException("A data de nascimento é obrigatória.");
        if (numeroCamisola <= 0 || numeroCamisola > 99)
            throw new IllegalArgumentException("O número de camisola deve estar entre 1 e 99.");
        if (estado == null)        throw new IllegalArgumentException("O estado é obrigatório.");

        this.nomeCompleto    = nomeCompleto.trim();
        this.equipa          = equipa;
        this.posicao         = posicao;
        this.dataNascimento  = dataNascimento;
        this.numeroCamisola  = numeroCamisola;
        this.estado          = estado;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String    getNomeCompleto()   { return nomeCompleto; }
    public Equipa    getEquipa()         { return equipa; }
    public Posicao   getPosicao()        { return posicao; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public int       getNumeroCamisola() { return numeroCamisola; }
    public Estado    getEstado()         { return estado; }

    public String getDataNascimentoFormatada() {
        return dataNascimento.format(FORMATO_DATA);
    }

    // ── Setters (uso restrito ao controlador) ──────────────────────────────────
    public void setNomeCompleto(String nome)       { validarNome(nome); this.nomeCompleto = nome.trim(); }
    public void setPosicao(Posicao posicao)        { this.posicao = posicao; }
    public void setDataNascimento(LocalDate data)  { this.dataNascimento = data; }
    public void setNumeroCamisola(int numero)      { this.numeroCamisola = numero; }
    public void setEstado(Estado estado)           { this.estado = estado; }

    // ── Utilitários ────────────────────────────────────────────────────────────
    public static LocalDate parsarData(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato dd/MM/yyyy.");
        }
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("O nome completo é obrigatório.");
    }

    @Override
    public String toString() {
        return nomeCompleto + " (#" + numeroCamisola + " — " + posicao + ")";
    }
}