package pt.ipleiria.estg.dei.ei.esoft.modelo;

public class ClassificacaoEquipa {
    private int posicao;
    private Equipa equipa;
    private int jogos;
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golosMarcados;
    private int golosSofridos;
    private int pontos;

    public ClassificacaoEquipa(Equipa equipa) {
        this.equipa = equipa;
        this.jogos = 0;
        this.vitorias = 0;
        this.empates = 0;
        this.derrotas = 0;
        this.golosMarcados = 0;
        this.golosSofridos = 0;
        this.pontos = 0;
    }

    // Getters e setters
    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; }
    public Equipa getEquipa() { return equipa; }
    public int getJogos() { return jogos; }
    public void setJogos(int jogos) { this.jogos = jogos; }
    public int getVitorias() { return vitorias; }
    public void setVitorias(int vitorias) { this.vitorias = vitorias; }
    public int getEmpates() { return empates; }
    public void setEmpates(int empates) { this.empates = empates; }
    public int getDerrotas() { return derrotas; }
    public void setDerrotas(int derrotas) { this.derrotas = derrotas; }
    public int getGolosMarcados() { return golosMarcados; }
    public void setGolosMarcados(int golosMarcados) { this.golosMarcados = golosMarcados; }
    public int getGolosSofridos() { return golosSofridos; }
    public void setGolosSofridos(int golosSofridos) { this.golosSofridos = golosSofridos; }
    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public int getDiferencaGolos() {
        return golosMarcados - golosSofridos;
    }

    @Override
    public String toString() {
        return String.format("%d. %-15s | J:%d  V:%d  E:%d  D:%d  GM:%d  GS:%d  GD:%d  PTS:%d",
                posicao, equipa.getNome(), jogos, vitorias, empates, derrotas,
                golosMarcados, golosSofridos, getDiferencaGolos(), pontos);
    }
}