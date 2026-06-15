package pt.ipleiria.estg.dei.ei.esoft.modelo;


import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Equipa — UC01 (Adicionar Equipa)
 * Representa uma equipa participante no Campeonato do Mundo.
 */
public class Equipa {

    private String nome;
    private String pais;
    private String grupo;               // Atribuído automaticamente pelo sistema (UC07)
    private List<String> jogadores;     // Lista de nomes dos jogadores (expandida no UC03)

    public Equipa(String nome, String pais) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da equipa não pode estar vazio.");
        }
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException("O país da equipa não pode estar vazio.");
        }
        this.nome      = nome.trim();
        this.pais      = pais.trim();
        this.grupo     = "";             // Ainda não atribuído — campo bloqueado no formulário
        this.jogadores = new ArrayList<>();
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String getNome()              { return nome; }
    public String getPais()              { return pais; }
    public String getGrupo()             { return grupo; }
    public List<String> getJogadores()   { return new ArrayList<>(jogadores); }

    // ── Setters (uso restrito ao controlador/modelo) ───────────────────────────

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da equipa não pode estar vazio.");
        }
        this.nome = nome.trim();
    }

    public void setPais(String pais) {
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException("O país não pode estar vazio.");
        }
        this.pais = pais.trim();
    }

    /** Chamado pelo sistema (UC07 — Gerir Torneio) ao gerar os grupos. */
    public void setGrupo(String grupo)   { this.grupo = grupo; }

    public void adicionarJogador(String nomeJogador) {
        if (nomeJogador != null && !nomeJogador.isBlank()) {
            jogadores.add(nomeJogador.trim());
        }
    }

    @Override
    public String toString() {
        return nome + " (" + pais + ")";
    }
}
