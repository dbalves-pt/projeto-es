package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.*;

import java.util.List;

public class BilheteControlador {
    private final Torneio torneio;

    public BilheteControlador() {
        this.torneio = Torneio.getInstancia();
    }

    public List<Jogo> listarJogosDisponiveis() {
        return torneio.listarJogosDisponiveis();
    }

    public List<Jogo> listarTodosJogos() {
        return torneio.getJogos();
    }

    public Bilhete reservarLugar(Jogo jogo, String lugarId) {
        return torneio.reservarLugar(jogo, lugarId);
    }

    public void confirmarCompra(Jogo jogo, Bilhete bilhete, String nome, String nif, String contacto) {
        torneio.confirmarCompra(jogo, bilhete, nome, nif, contacto);
    }

    public void cancelarReserva(Jogo jogo, Bilhete bilhete) {
        torneio.cancelarReserva(jogo, bilhete);
    }

    public void aplicarDesconto(Jogo jogo, String nomeBancada, double novoPreco) {
        torneio.aplicarDesconto(jogo, nomeBancada, novoPreco);
    }

    public double getTotalBilheteira() {
        return torneio.getTotalBilheteira();
    }

    public double getReceitaPorJogo(Jogo jogo) {
        return jogo.getReceitaBilheteira();
    }
}