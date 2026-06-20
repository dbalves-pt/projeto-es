package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador; // Importa o Jogador
import java.time.LocalDate;                           // Importa a data
/**
 * Entidade Equipa — versão actualizada para UC02 e UC03.
 *
 * Alterações face ao UC01:
 *   • jogadores passa a List&lt;Jogador&gt; (tipado) em vez de List&lt;String&gt;
 *   • métodos de gestão de jogadores (UC03)
 *   • setter de nome e país para edição (UC02)
 */
public class Equipa {

    private String treinador = "Sem Treinador"; // Atributo
    private String         nome;
    private String         pais;
    private String         grupo;          // Atribuído automaticamente (UC07)
    private List<Jogador>  jogadores;

    public Equipa(String nome, String pais) {
        validarNome(nome);
        validarPais(pais);
        this.nome      = nome.trim();
        this.pais      = pais.trim();
        this.grupo     = "";
        this.jogadores = new ArrayList<>();
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String          getNome()      { return nome; }
    public String          getPais()      { return pais; }
    public String          getGrupo()     { return grupo; }

    // Getter e Setter
    public String getTreinador() { return treinador; }
    public void setTreinador(String treinador) { this.treinador = treinador; }

    /** Devolve cópia imutável da lista de jogadores. */
    public List<Jogador>   getJogadores() { return Collections.unmodifiableList(jogadores); }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setNome(String nome)      { validarNome(nome);  this.nome  = nome.trim(); }
    public void setPais(String pais)      { validarPais(pais);  this.pais  = pais.trim(); }
    public void setGrupo(String grupo)    { this.grupo = grupo; }

    // ── Gestão de Jogadores (UC03) ─────────────────────────────────────────────

    /**
     * Adiciona um jogador à equipa.
     *
     * @throws IllegalArgumentException se o número de camisola já existir na equipa.
     */
    public void adicionarJogador(Jogador jogador) {
        if (existeNumeroCamisola(jogador.getNumeroCamisola())) {
            throw new IllegalArgumentException(
                    "Já existe um jogador com o número de camisola "
                            + jogador.getNumeroCamisola() + " nesta equipa.");
        }
        jogadores.add(jogador);
    }

    /**
     * Remove um jogador da equipa.
     */
    public void removerJogador(Jogador jogador) {
        jogadores.remove(jogador);
    }

    /** Verifica se já existe um jogador com o número de camisola dado. */
    public boolean existeNumeroCamisola(int numero) {
        return jogadores.stream()
                .anyMatch(j -> j.getNumeroCamisola() == numero);
    }

    /**
     * Verifica se já existe o número de camisola, excluindo o próprio jogador (para edição).
     */
    public boolean existeNumeroCamisolaExcluindo(int numero, Jogador jogadorAtual) {
        return jogadores.stream()
                .filter(j -> j != jogadorAtual)
                .anyMatch(j -> j.getNumeroCamisola() == numero);
    }

    // ── Privados ──────────────────────────────────────────────────────────────
    private void validarNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("O nome da equipa não pode estar vazio.");
    }
    private void validarPais(String pais) {
        if (pais == null || pais.isBlank())
            throw new IllegalArgumentException("O país não pode estar vazio.");
    }

    @Override
    public String toString() { return nome + " (" + pais + ")"; }


}