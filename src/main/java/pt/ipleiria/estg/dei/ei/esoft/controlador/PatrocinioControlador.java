package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.util.List;

public class PatrocinioControlador {
    private final Torneio torneio;

    public PatrocinioControlador() {
        this.torneio = Torneio.getInstancia();
    }

    public List<Patrocinio> listarPatrocinios() {
        return torneio.getPatrocinios();
    }

    public void adicionarPatrocinio(String nome, String tipo, double valor, List<Jogo> jogos) {
        torneio.criarPatrocinio(nome, tipo, valor, jogos);
    }

    public void removerPatrocinio(Patrocinio patrocinio) {
        torneio.removerPatrocinio(patrocinio);
    }

    public double getTotalPatrocinios() {
        return torneio.getTotalPatrocinios();
    }

    public List<Jogo> listarTodosJogos() {
        return torneio.getJogos();
    }
}