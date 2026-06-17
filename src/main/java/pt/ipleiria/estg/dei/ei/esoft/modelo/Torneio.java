package pt.ipleiria.estg.dei.ei.esoft.modelo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Torneio — estado global da aplicação.
 * Versão actualizada para UC02 e UC03 (sem alterações estruturais, apenas javadoc).
 */
public class Torneio {

    public enum Estado { INICIAL, CONFIGURADO, VALIDADO, EM_CURSO }

    private static Torneio instancia;

    public static Torneio getInstancia() {
        if (instancia == null) instancia = new Torneio();
        return instancia;
    }

    /** Reinicia para testes unitários. */
    public static void resetInstancia() { instancia = null; }

    private List<Estadio> estadios;
    private Estado       estado;
    private List<Equipa> equipas;

    private Torneio() {
        this.estadios = new ArrayList<>();
        this.estado  = Estado.INICIAL;
        this.equipas = new ArrayList<>();
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public Estado getEstado()           { return estado; }
    public List<Equipa> getEquipas()    { return Collections.unmodifiableList(equipas); }

    // ── Regras de negócio ─────────────────────────────────────────────────────

    public boolean gruposGerados() { return estado != Estado.INICIAL; }

    /** UC01 — Adicionar equipa. */
    public void adicionarEquipa(Equipa equipa) {
        if (gruposGerados())
            throw new IllegalStateException("GRUPOS_GERADOS");
        if (existeEquipaComNome(equipa.getNome()))
            throw new IllegalArgumentException("NOME_DUPLICADO");
        equipas.add(equipa);
    }

    /** UC02 — Remover equipa. */
    public void removerEquipa(Equipa equipa) {
        if (gruposGerados())
            throw new IllegalStateException("GRUPOS_GERADOS");
        equipas.remove(equipa);
    }

    /** Verifica existência de nome (case-insensitive). */
    public boolean existeEquipaComNome(String nome) {
        return equipas.stream()
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    /** Verifica existência de nome excluindo a equipa actual (para edição — UC02). */
    public boolean existeEquipaComNomeExcluindo(String nome, Equipa equipaAtual) {
        return equipas.stream()
                .filter(e -> e != equipaAtual)
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    public void setEstado(Estado novoEstado) { this.estado = novoEstado; }

    public List<Estadio> getEstadios() { return estadios; }
    public void adicionarEstadio(Estadio e) { estadios.add(e); }
}
