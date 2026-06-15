package pt.ipleiria.estg.dei.ei.esoft.controlador;


import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.util.List;

/**
 * Controlador para UC01 — Adicionar Equipa
 *
 * Recebe pedidos da camada Vista, executa validações de negócio
 * e delega as operações persistentes ao modelo (Torneio).
 *
 * Fluxo ICONIX:
 *   Vista → EquipaControlador → Torneio / Equipa
 */
public class EquipaControlador {

    // ── Lista oficial de países aceites pelo sistema ───────────────────────────
    // Reflecte o dropdown "País" do formulário Figma.
    // Esta lista pode ser carregada de ficheiro em iterações futuras.
    private static final List<String> PAISES_VALIDOS = List.of(
            "Alemanha", "Argentina", "Austrália", "Bélgica", "Brasil",
            "Canadá", "Croácia", "Dinamarca", "Egipto", "Espanha",
            "EUA", "França", "Holanda", "Inglaterra", "Irão",
            "Japão", "Marrocos", "México", "Nigéria", "Polónia",
            "Portugal", "Qatar", "Senegal", "Sérvia", "Suíça",
            "Turquia", "Uruguai"
    );

    private final Torneio torneio;

    public EquipaControlador() {
        this.torneio = Torneio.getInstancia();
    }

    /** Construtor com injeção — facilita testes unitários. */
    public EquipaControlador(Torneio torneio) {
        this.torneio = torneio;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC01 — Caminho Principal
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Tenta registar uma nova equipa.
     *
     * @param nome  Valor do campo "Nome" introduzido pelo utilizador.
     * @param pais  País selecionado no dropdown "País".
     * @throws IllegalStateException    CA "Grupos gerados" — link devia estar bloqueado.
     * @throws IllegalArgumentException CA 5.1 campo vazio / 6.1 nome duplicado / 7.1 país inválido.
     */
    public void adicionarEquipa(String nome, String pais) {

        // ── CA 5.1 — Campo obrigatório vazio ──────────────────────────────────
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("CAMPO_NOME_VAZIO");
        }
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException("CAMPO_PAIS_VAZIO");
        }

        // ── CA "Grupos gerados" — torneio já avançou de estado ────────────────
        if (torneio.gruposGerados()) {
            throw new IllegalStateException("GRUPOS_GERADOS");
        }

        // ── CA 7.1 — País inválido ────────────────────────────────────────────
        if (!isPaisValido(pais)) {
            throw new IllegalArgumentException("PAIS_INVALIDO");
        }

        // ── CA 6.1 — Nome duplicado ───────────────────────────────────────────
        if (torneio.existeEquipaComNome(nome)) {
            throw new IllegalArgumentException("NOME_DUPLICADO");
        }

        // ── Caminho principal — registo ───────────────────────────────────────
        Equipa novaEquipa = new Equipa(nome.trim(), pais.trim());
        torneio.adicionarEquipa(novaEquipa);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Métodos auxiliares para a Vista
    // ══════════════════════════════════════════════════════════════════════════

    /** Devolve a lista de países disponíveis no dropdown. */
    public List<String> getPaisesDisponiveis() {
        return PAISES_VALIDOS;
    }

    /** Indica se o link "Inserir equipa…" deve aparecer bloqueado. */
    public boolean isInsercaoDeEquipaBloqueada() {
        return torneio.gruposGerados();
    }

    /** Devolve a lista de equipas registadas (para actualizar o painel esquerdo). */
    public List<Equipa> getEquipas() {
        return torneio.getEquipas();
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private boolean isPaisValido(String pais) {
        return PAISES_VALIDOS.stream()
                .anyMatch(p -> p.equalsIgnoreCase(pais.trim()));
    }
}