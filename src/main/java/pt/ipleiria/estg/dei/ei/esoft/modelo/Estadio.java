package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.util.ArrayList;
import java.util.List;

public class Estadio {
    private String nome;
    private String cidade;
    private String pais;
    private int lotacaoMaxima;
    private List<Bancada> bancadas;

    public Estadio(String nome, String cidade, String pais, int lotacaoMaxima) {
        this.nome = nome;
        this.cidade = cidade;
        this.pais = pais;
        this.lotacaoMaxima = lotacaoMaxima;
        this.bancadas = new ArrayList<>();
    }

    // Lógica para validar a Lotação Excedida (Caminho Alternativo 4.1)
    public int getLotacaoAtual() {
        return bancadas.stream().mapToInt(Bancada::getLugares).sum();
    }

    public void adicionarBancada(Bancada bancada) {
        bancadas.add(bancada);
    }

    public void removerBancada(Bancada bancada) {
        bancadas.remove(bancada);
    }

    // Getters
    public String getNome() { return nome; }
    public String getCidade() { return cidade; }
    public String getPais() { return pais; }
    public int getLotacaoMaxima() { return lotacaoMaxima; }
    public List<Bancada> getBancadas() { return bancadas; }
}