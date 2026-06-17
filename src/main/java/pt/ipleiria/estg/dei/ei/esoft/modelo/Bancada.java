package pt.ipleiria.estg.dei.ei.esoft.modelo;

public class Bancada {
    private String nome;
    private double preco;
    private String categoria;
    private int filas;
    private int lugares;

    public Bancada(String nome, double preco, String categoria, int filas, int lugares) {
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
        this.filas = filas;
        this.lugares = lugares;
    }

    // Getters
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public String getCategoria() { return categoria; }
    public int getFilas() { return filas; }
    public int getLugares() { return lugares; }

    // Setters para permitir edição
    public void setNome(String nome) { this.nome = nome; }
    public void setPreco(double preco) { this.preco = preco; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setFilas(int filas) { this.filas = filas; }
    public void setLugares(int lugares) { this.lugares = lugares; }
}