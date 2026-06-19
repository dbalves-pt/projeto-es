package pt.ipleiria.estg.dei.ei.esoft.modelo;

public class EventoJogo {
    public enum TipoEvento {
        GOLO, ASSISTENCIA, CARTAO_AMARELO, CARTAO_VERMELHO, DEFESA
    }

    private TipoEvento tipo;
    private Jogador jogador;
    private int minuto;
    private Equipa equipa; // pode ser inferido do jogador

    public EventoJogo(TipoEvento tipo, Jogador jogador, int minuto) {
        this.tipo = tipo;
        this.jogador = jogador;
        this.minuto = minuto;
        this.equipa = jogador.getEquipa();
    }

    public TipoEvento getTipo() { return tipo; }
    public Jogador getJogador() { return jogador; }
    public int getMinuto() { return minuto; }
    public Equipa getEquipa() { return equipa; }
}