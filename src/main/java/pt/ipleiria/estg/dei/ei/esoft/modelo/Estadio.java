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

    public String getNome() { return nome; }
    public String getCidade() { return cidade; }
    public String getPais() { return pais; }
    public int getLotacaoMaxima() { return lotacaoMaxima; }
    public List<Bancada> getBancadas() { return new ArrayList<>(bancadas); }

    public void adicionarBancada(Bancada bancada) {
        int total = bancadas.stream().mapToInt(Bancada::getCapacidade).sum() + bancada.getCapacidade();
        if (total > lotacaoMaxima)
            throw new IllegalArgumentException("Lotação excedida");
        bancadas.add(bancada);
    }

    public Bancada getBancadaPorNome(String nome) {
        return bancadas.stream()
                .filter(b -> b.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtém a bancada a partir de um identificador de lugar (ex: "Norte-F1-L12").
     * Assumimos que o nome da bancada é a primeira palavra (antes do '-')
     */
    public Bancada getBancadaPorLugarId(String lugarId) {
        String nomeBancada = lugarId.split("-")[0];
        return getBancadaPorNome(nomeBancada);
    }
}