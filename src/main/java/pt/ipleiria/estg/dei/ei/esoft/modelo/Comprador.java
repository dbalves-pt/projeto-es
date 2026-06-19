package pt.ipleiria.estg.dei.ei.esoft.modelo;

public class Comprador {
    private String nome;
    private String nif;

    public Comprador(String nome, String nif) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome do comprador é obrigatório.");
        if (nif == null || nif.isBlank()) throw new IllegalArgumentException("NIF é obrigatório.");
        this.nome = nome.trim();
        this.nif = nif.trim();
    }

    public String getNome() { return nome; }
    public String getNif() { return nif; }

    @Override
    public String toString() { return nome + " (NIF: " + nif + ")"; }
}