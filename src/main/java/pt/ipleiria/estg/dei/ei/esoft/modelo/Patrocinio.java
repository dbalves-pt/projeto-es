package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.util.ArrayList;
import java.util.List;

public class Patrocinio {
    private String nomeEmpresa;
    private String tipo;
    private double valor;
    private List<Jogo> jogos;

    public Patrocinio(String nomeEmpresa, String tipo, double valor, List<Jogo> jogos) {
        this.nomeEmpresa = nomeEmpresa;
        this.tipo = tipo;
        this.valor = valor;
        this.jogos = new ArrayList<>(jogos);
    }

    public String getNomeEmpresa() { return nomeEmpresa; }
    public String getTipo() { return tipo; }
    public double getValor() { return valor; }
    public List<Jogo> getJogos() { return new ArrayList<>(jogos); }

    public boolean isAssociadoAJogoIniciado() {
        return jogos.stream()
                .anyMatch(j -> j.getEstado() == Jogo.EstadoJogo.COMEÇADO
                        || j.getEstado() == Jogo.EstadoJogo.TERMINADO);
    }
}