package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.EventoJogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.EventoJogo.Tipo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador UC10 (Registar Evento de Jogo) e UC12 (Corrigir Evento de Jogo).
 *
 * UC10 — Caminho Principal:
 *   1. Utilizador acede ao ecrã 'Estatísticas do Jogo'.
 *   2. Utilizador clica em 'Novo Evento'.
 *   3. Sistema apresenta o formulário 'Registar Evento'.
 *   4. Utilizador preenche os dados (Tipo, Equipa, Jogador, Minuto) e
 *      clica em 'Confirmar'.
 *   5-7. Sistema valida o jogador, o minuto e as regras do jogo.
 *   8. Sistema guarda o evento e actualiza marcador/estatísticas.
 *
 * UC12 — Caminho Principal:
 *   1-2. Utilizador acede às 'Estatísticas do Jogo' de um jogo terminado.
 *   3-4. Utilizador clica em 'Editar' num evento; sistema mostra o formulário
 *        preenchido com os dados actuais.
 *   5-8. Utilizador altera os dados e guarda; sistema valida, regista a
 *        auditoria e recalcula estatísticas/classificações.
 *
 *   CA 1.1 / 3.1 — Prazo expirado: edição só permitida dentro de
 *   {@link #PRAZO_CORRECAO_HORAS} horas após o registo do evento.
 */
public class EventoControlador {

    /**
     * Prazo (em horas) durante o qual um evento pode ser corrigido após o
     * seu registo (UC12). Constante facilmente ajustável conforme as regras
     * definidas pelo "cliente" do projeto.
     */
    public static final long PRAZO_CORRECAO_HORAS = 24;

    public EventoControlador() { }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC10 — Registar Evento de Jogo
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Regista um novo evento no jogo indicado.
     *
     * Códigos de erro lançados (IllegalArgumentException.getMessage()):
     *   CAMPO_TIPO_VAZIO / CAMPO_EQUIPA_VAZIA / CAMPO_JOGADOR_VAZIO / CAMPO_MINUTO_VAZIO  – CA 4.1
     *   TIPO_INVALIDO                       – tipo de evento desconhecido
     *   MINUTO_INVALIDO                     – CA 6.1: minuto fora do intervalo válido
     *   JOGADOR_NAO_PERTENCE_EQUIPA          – CA 5.1: jogador inválido para a equipa indicada
     *   JOGADOR_INAPTO                       – CA 5.1: jogador não está apto a jogar
     *   JOGADOR_JA_EXPULSO                   – CA 7.1: violação de regras (jogador já tem cartão vermelho)
     *
     * @throws IllegalStateException JOGO_NAO_COMECADO se o jogo não estiver em curso.
     */
    public void registarEvento(Jogo jogo, String descricaoTipo, Equipa equipa,
                                Jogador jogador, String minutoStr) {

        if (jogo == null) throw new IllegalArgumentException("JOGO_NULO");

        // CA 4.1 — Campos em branco
        if (descricaoTipo == null || descricaoTipo.isBlank())
            throw new IllegalArgumentException("CAMPO_TIPO_VAZIO");
        if (equipa == null)
            throw new IllegalArgumentException("CAMPO_EQUIPA_VAZIA");
        if (jogador == null)
            throw new IllegalArgumentException("CAMPO_JOGADOR_VAZIO");
        if (minutoStr == null || minutoStr.isBlank())
            throw new IllegalArgumentException("CAMPO_MINUTO_VAZIO");

        Tipo tipo = parsarTipo(descricaoTipo);
        int minuto = parsarMinuto(minutoStr);

        // A validação de pertença à equipa / aptidão / expulsão é feita no modelo Jogo,
        // que lança os respectivos códigos de erro (JOGADOR_NAO_PERTENCE_EQUIPA, etc.).
        EventoJogo evento = new EventoJogo(tipo, equipa, jogador, minuto);
        jogo.registarEvento(evento);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC12 — Corrigir Evento de Jogo
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Indica se o evento ainda está dentro do prazo de correcção (CA 1.1 / 3.1).
     */
    public boolean podeCorrigir(EventoJogo evento) {
        long horasDesdeRegisto = ChronoUnit.HOURS.between(evento.getRegistadoEm(), LocalDateTime.now());
        return horasDesdeRegisto < PRAZO_CORRECAO_HORAS;
    }

    /**
     * Aplica a correcção aos dados de um evento já registado.
     *
     * @throws IllegalStateException    PRAZO_EXPIRADO — CA 1.1 / 3.1.
     * @throws IllegalArgumentException com os mesmos códigos de {@link #registarEvento}
     *                                   em caso de dados inválidos (CA 6.1).
     */
    public void corrigirEvento(Jogo jogo, EventoJogo evento, String descricaoTipo,
                                Equipa equipa, Jogador jogador, String minutoStr) {

        if (jogo == null) throw new IllegalArgumentException("JOGO_NULO");
        if (evento == null) throw new IllegalArgumentException("EVENTO_NULO");

        if (!podeCorrigir(evento))
            throw new IllegalStateException("PRAZO_EXPIRADO");

        if (descricaoTipo == null || descricaoTipo.isBlank())
            throw new IllegalArgumentException("CAMPO_TIPO_VAZIO");
        if (equipa == null)
            throw new IllegalArgumentException("CAMPO_EQUIPA_VAZIA");
        if (jogador == null)
            throw new IllegalArgumentException("CAMPO_JOGADOR_VAZIO");
        if (minutoStr == null || minutoStr.isBlank())
            throw new IllegalArgumentException("CAMPO_MINUTO_VAZIO");
        if (jogador.getEquipa() != equipa)
            throw new IllegalArgumentException("JOGADOR_NAO_PERTENCE_EQUIPA");

        Tipo tipo = parsarTipo(descricaoTipo);
        int minuto = parsarMinuto(minutoStr);

        jogo.corrigirEvento(evento, tipo, equipa, jogador, minuto);
    }

    // ── Auxiliares para a Vista ───────────────────────────────────────────────

    public List<String> getDescricoesTipo() {
        return Arrays.stream(Tipo.values()).map(Tipo::toString).collect(Collectors.toList());
    }

    public List<EventoJogo> getEventos(Jogo jogo) {
        return jogo.getEventos();
    }

    // ── Parsing privado ────────────────────────────────────────────────────────

    private Tipo parsarTipo(String descricao) {
        try {
            return Tipo.fromDescricao(descricao);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("TIPO_INVALIDO");
        }
    }

    private int parsarMinuto(String texto) {
        try {
            int minuto = Integer.parseInt(texto.trim());
            if (minuto < 0 || minuto > 130) throw new IllegalArgumentException();
            return minuto;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("MINUTO_INVALIDO");
        }
    }
}
