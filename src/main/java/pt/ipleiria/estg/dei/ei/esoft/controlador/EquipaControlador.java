package pt.ipleiria.estg.dei.ei.esoft.controlador;


import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.util.List;

/**
 * Controlador UC01 + UC02 — Adicionar / Editar / Eliminar Equipa.
 *
 * UC02 — Editar / Eliminar Equipa:
 *   Caminho Editar:
 *     1. Utilizador clica sobre o nome de uma equipa no painel esquerdo.
 *     2. Utilizador clica no nome no painel central (ou botão de edição).
 *     3. Sistema abre o formulário 'Inserir Equipa' pré-preenchido.
 *     4. Utilizador altera os dados e clica em 'Concluído'.
 *     5. Sistema guarda as alterações e actualiza o ecrã.
 *
 *   Caminho Eliminar:
 *     1. Utilizador clica no link 'Eliminar equipa…'.
 *     2. Sistema abre diálogo de confirmação.
 *     3. Utilizador clica em 'Confirmar Eliminação'.
 *     4. Sistema remove a equipa e actualiza a lista.
 *
 *   CA 8.1 — Nome duplicado (edição): erro no campo 'Nome'.
 *   CA 4.1 — Cancelamento (eliminar): fecha o diálogo.
 *   CA "Grupos gerados": links de edição/eliminação ficam desativados.
 */
public class EquipaControlador {

    private static final List<String> PAISES_VALIDOS = List.of(
            "Alemanha", "Argentina", "Austrália", "Bélgica", "Brasil",
            "Canadá", "Croácia", "Dinamarca", "Egipto", "Espanha",
            "EUA", "França", "Holanda", "Inglaterra", "Irão",
            "Japão", "Marrocos", "México", "Nigéria", "Polónia",
            "Portugal", "Qatar", "Senegal", "Sérvia", "Suíça",
            "Turquia", "Uruguai"
    );

    private final Torneio torneio;

    public EquipaControlador()                { this.torneio = Torneio.getInstancia(); }
    public EquipaControlador(Torneio t)       { this.torneio = t; }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC01 — Adicionar Equipa
    // ══════════════════════════════════════════════════════════════════════════

    public void adicionarEquipa(String nome, String pais) {
        validarCamposComuns(nome, pais, null);
        torneio.adicionarEquipa(new Equipa(nome.trim(), pais.trim()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC02 — Editar Equipa
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Actualiza os dados de uma equipa existente.
     *
     * @param equipa    A equipa a editar (referência directa ao objecto do modelo).
     * @param novoNome  Novo nome introduzido pelo utilizador.
     * @param novoPais  Novo país seleccionado.
     *
     * @throws IllegalStateException    CA "Grupos gerados".
     * @throws IllegalArgumentException CA 5.1 campo vazio / 7.1 país inválido / 8.1 nome duplicado.
     */
    public void editarEquipa(Equipa equipa, String novoNome, String novoPais) {
        if (equipa == null) throw new IllegalArgumentException("EQUIPA_NULA");
        if (torneio.gruposGerados()) throw new IllegalStateException("GRUPOS_GERADOS");

        validarCamposComuns(novoNome, novoPais, equipa);

        equipa.setNome(novoNome.trim());
        equipa.setPais(novoPais.trim());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC02 — Eliminar Equipa
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Remove uma equipa do torneio.
     *
     * @throws IllegalStateException CA "Grupos gerados".
     */
    public void eliminarEquipa(Equipa equipa) {
        if (equipa == null) throw new IllegalArgumentException("EQUIPA_NULA");
        if (torneio.gruposGerados()) throw new IllegalStateException("GRUPOS_GERADOS");
        torneio.removerEquipa(equipa);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Auxiliares para a Vista
    // ══════════════════════════════════════════════════════════════════════════

    public List<String>  getPaisesDisponiveis()        { return PAISES_VALIDOS; }
    public List<Equipa>  getEquipas()                  { return torneio.getEquipas(); }
    public boolean       isEdicaoBloqueada()           { return torneio.gruposGerados(); }

    // ── Validação comum (UC01 + UC02) ─────────────────────────────────────────

    /**
     * @param equipaAtual null = inserção (UC01); não-null = edição (UC02).
     */
    private void validarCamposComuns(String nome, String pais, Equipa equipaAtual) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("CAMPO_NOME_VAZIO");
        if (pais == null || pais.isBlank())
            throw new IllegalArgumentException("CAMPO_PAIS_VAZIO");
        if (!isPaisValido(pais))
            throw new IllegalArgumentException("PAIS_INVALIDO");

        boolean duplicado = (equipaAtual == null)
                ? torneio.existeEquipaComNome(nome)
                : torneio.existeEquipaComNomeExcluindo(nome, equipaAtual);

        if (duplicado)
            throw new IllegalArgumentException("NOME_DUPLICADO");
    }

    private boolean isPaisValido(String pais) {
        return PAISES_VALIDOS.stream().anyMatch(p -> p.equalsIgnoreCase(pais.trim()));
    }
}