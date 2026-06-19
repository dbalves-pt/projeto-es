package pt.ipleiria.estg.dei.ei.esoft.modelo;

public class Bancada {
    private String nome;
    private double preco;
    private String categoria;
    private int capacidade;

    public Bancada(String nome, double preco, String categoria, int capacidade) {
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
        this.capacidade = capacidade;
    }

    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public String getCategoria() { return categoria; }
    public int getCapacidade() { return capacidade; }
}