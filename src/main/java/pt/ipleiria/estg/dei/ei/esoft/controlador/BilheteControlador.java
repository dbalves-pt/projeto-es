package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BilheteControlador {
    private final List<Bilhete> bilhetes = new ArrayList<>();
    private final Torneio torneio;

    public BilheteControlador() {
        this.torneio = Torneio.getInstancia();
    }

    public List<Bilhete> getBilhetes() { return bilhetes; }

    public List<Bilhete> getBilhetesPorJogo(Jogo jogo) {
        return bilhetes.stream().filter(b -> b.getJogo().equals(jogo)).collect(Collectors.toList());
    }

    /**
     * Vende um bilhete para um jogo, bancada, lugar específico.
     * Verifica se o jogo está calendarizado (não começado/terminado),
     * se o lugar está disponível e se o comprador é válido.
     * Aplica desconto se a bancada tiver desconto ativo para esse jogo (a implementar).
     */
    public Bilhete venderBilhete(Jogo jogo, Bancada bancada, int fila, int assento, Comprador comprador, double precoBase) {
        if (jogo == null || bancada == null || comprador == null)
            throw new IllegalArgumentException("Dados incompletos para venda.");
        if (jogo.getEstado() != Jogo.Estado.CALENDARIZADO)
            throw new IllegalStateException("Não é possível vender bilhetes para jogos já começados ou terminados.");
        if (fila <= 0 || fila > bancada.getFilas() || assento <= 0 || assento > bancada.getLugares())
            throw new IllegalArgumentException("Fila ou lugar inválido.");

        // Verifica se já existe bilhete para este lugar neste jogo
        boolean ocupado = bilhetes.stream().anyMatch(b ->
                b.getJogo().equals(jogo) &&
                        b.getBancada().equals(bancada) &&
                        b.getFila() == fila &&
                        b.getAssento() == assento &&
                        b.getEstado() != Bilhete.Estado.DISPONIVEL
        );
        if (ocupado) throw new IllegalStateException("Este lugar já está ocupado (vendido ou reservado).");

        double precoFinal = precoBase; // aqui poderá aplicar descontos no futuro

        Bilhete bilhete = new Bilhete(jogo, bancada, fila, assento, precoFinal);
        bilhete.vender(comprador);
        bilhetes.add(bilhete);
        return bilhete;
    }

    /**
     * Calcula a receita total de bilheteira para todos os jogos.
     */
    public double getReceitaTotalBilheteira() {
        return bilhetes.stream()
                .filter(b -> b.getEstado() == Bilhete.Estado.VENDIDO)
                .mapToDouble(Bilhete::getPrecoPago)
                .sum();
    }

    /**
     * Receita de bilheteira por jogo.
     */
    public double getReceitaPorJogo(Jogo jogo) {
        return bilhetes.stream()
                .filter(b -> b.getJogo().equals(jogo) && b.getEstado() == Bilhete.Estado.VENDIDO)
                .mapToDouble(Bilhete::getPrecoPago)
                .sum();
    }
}