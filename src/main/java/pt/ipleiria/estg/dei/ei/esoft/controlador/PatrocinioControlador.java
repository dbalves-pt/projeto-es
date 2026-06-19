package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatrocinioControlador {
    private final List<Patrocinio> patrocinios = new ArrayList<>();
    private final Torneio torneio;

    public PatrocinioControlador() {
        this.torneio = Torneio.getInstancia();
    }

    public List<Patrocinio> getPatrocinios() { return patrocinios; }

    public Patrocinio adicionarPatrocinioRetornando(String nome, String nif, double valor,
                                                    LocalDate inicio, LocalDate fim, String tipo) {
        Patrocinio p = new Patrocinio(nome, nif, valor, inicio, fim, tipo);
        patrocinios.add(p);
        return p;
    }

    public void adicionarPatrocinio(String nome, String nif, double valor,
                                    LocalDate inicio, LocalDate fim, String tipo) {
        adicionarPatrocinioRetornando(nome, nif, valor, inicio, fim, tipo);
    }

    public void editarPatrocinio(Patrocinio patrocinio, String nome, String nif,
                                 double valor, LocalDate inicio, LocalDate fim, String tipo) {
        if (patrocinio.temJogosRealizados())
            throw new IllegalStateException("Não é possível editar um patrocínio com jogos já realizados.");
        patrocinio.setNomePatrocinador(nome);
        patrocinio.setNif(nif);
        patrocinio.setValor(valor);
        patrocinio.setDatas(inicio, fim);
        patrocinio.setTipo(tipo);
    }

    public void removerPatrocinio(Patrocinio patrocinio) {
        if (patrocinio.temJogosRealizados())
            throw new IllegalStateException("Não é possível remover um patrocínio com jogos já realizados.");
        patrocinios.remove(patrocinio);
    }

    public double getReceitaTotalPatrocinios() {
        return patrocinios.stream().mapToDouble(Patrocinio::getValor).sum();
    }

    public void associarJogo(Patrocinio patrocinio, Jogo jogo) {
        patrocinio.adicionarJogo(jogo);
    }

    public void desassociarJogo(Patrocinio patrocinio, Jogo jogo) {
        patrocinio.removerJogo(jogo);
    }

    public List<Jogo> getJogosDisponiveis() {
        return torneio.getJogos();
    }
}