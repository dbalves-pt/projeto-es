package pt.ipleiria.estg.dei.ei.esoft.modelo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Torneio — centraliza o estado global da aplicação.
 *
 * Estado do torneio:
 *   INICIAL       → equipas podem ser adicionadas/editadas
 *   CONFIGURADO   → grupos gerados, link "Inserir equipa…" bloqueado (UC07)
 *   VALIDADO      → calendário validado (UC08)
 *   EM_CURSO      → torneio a decorrer (UC08)
 */
public class Torneio {

    // ── Estados possíveis ──────────────────────────────────────────────────────
    public enum Estado { INICIAL, CONFIGURADO, VALIDADO, EM_CURSO }

    // ── Singleton ──────────────────────────────────────────────────────────────
    private static Torneio instancia;

    public static Torneio getInstancia() {
        if (instancia == null) {
            instancia = new Torneio();
        }
        return instancia;
    }

    /** Reinicia o singleton — útil para testes unitários. */
    public static void resetInstancia() {
        instancia = null;
    }

    // ── Atributos ──────────────────────────────────────────────────────────────
    private Estado       estado;
    private List<Equipa> equipas;

    private Torneio() {
        this.estado  = Estado.INICIAL;
        this.equipas = new ArrayList<>();
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Estado getEstado() { return estado; }

    /** Retorna cópia imutável da lista de equipas. */
    public List<Equipa> getEquipas() {
        return Collections.unmodifiableList(equipas);
    }

    // ── Lógica de negócio (usada pelo EquipaControlador) ──────────────────────

    /**
     * Verifica se os grupos já foram gerados.
     * Quando {@code true}, o link "Inserir equipa…" deve ficar bloqueado (UC01 — CA "Grupos gerados").
     */
    public boolean gruposGerados() {
        return estado != Estado.INICIAL;
    }

    /**
     * Adiciona uma equipa ao torneio.
     *
     * @throws IllegalStateException  se os grupos já foram gerados.
     * @throws IllegalArgumentException se o nome já existir (duplicado).
     */
    public void adicionarEquipa(Equipa equipa) {
        if (gruposGerados()) {
            throw new IllegalStateException(
                    "Não é possível adicionar equipas: os grupos já foram gerados.");
        }
        if (existeEquipaComNome(equipa.getNome())) {
            throw new IllegalArgumentException(
                    "Já existe uma equipa com o nome \"" + equipa.getNome() + "\".");
        }
        equipas.add(equipa);
    }

    /**
     * Remove uma equipa do torneio (UC02).
     *
     * @throws IllegalStateException se os grupos já foram gerados.
     */
    public void removerEquipa(Equipa equipa) {
        if (gruposGerados()) {
            throw new IllegalStateException(
                    "Não é possível remover equipas: os grupos já foram gerados.");
        }
        equipas.remove(equipa);
    }

    /** Verifica (case-insensitive) se já existe equipa com o mesmo nome. */
    public boolean existeEquipaComNome(String nome) {
        return equipas.stream()
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    /** Verifica (case-insensitive) se já existe equipa com o mesmo nome,
     *  excluindo a própria equipa — útil para edição (UC02). */
    public boolean existeEquipaComNomeExcluindo(String nome, Equipa equipaAtual) {
        return equipas.stream()
                .filter(e -> e != equipaAtual)
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    /** Altera o estado do torneio — chamado pelos controladores de UC07/UC08. */
    public void setEstado(Estado novoEstado) {
        this.estado = novoEstado;
    }
}
