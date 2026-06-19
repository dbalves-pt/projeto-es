package pt.ipleiria.estg.dei.ei.esoft.modelo;

public class Bilhete {
    public enum Estado { DISPONIVEL, RESERVADO, VENDIDO }

    private final int id;
    private final Jogo jogo;
    private final Bancada bancada;
    private final int fila;
    private final int assento;
    private double precoPago;
    private Estado estado;
    private Comprador comprador;

    private static int proximoId = 1;

    public Bilhete(Jogo jogo, Bancada bancada, int fila, int assento, double precoPago) {
        this.id = proximoId++;
        this.jogo = jogo;
        this.bancada = bancada;
        this.fila = fila;
        this.assento = assento;
        this.precoPago = precoPago;
        this.estado = Estado.DISPONIVEL;
        this.comprador = null;
    }

    public int getId() { return id; }
    public Jogo getJogo() { return jogo; }
    public Bancada getBancada() { return bancada; }
    public int getFila() { return fila; }
    public int getAssento() { return assento; }
    public double getPrecoPago() { return precoPago; }
    public Estado getEstado() { return estado; }
    public Comprador getComprador() { return comprador; }

    public void reservar() {
        if (estado != Estado.DISPONIVEL) throw new IllegalStateException("Bilhete não está disponível.");
        this.estado = Estado.RESERVADO;
    }

    public void vender(Comprador comprador) {
        if (estado == Estado.VENDIDO) throw new IllegalStateException("Bilhete já vendido.");
        if (estado == Estado.DISPONIVEL) this.estado = Estado.RESERVADO; // transição implícita
        this.estado = Estado.VENDIDO;
        this.comprador = comprador;
    }

    public void cancelarReserva() {
        if (estado != Estado.RESERVADO) throw new IllegalStateException("Bilhete não está reservado.");
        this.estado = Estado.DISPONIVEL;
        this.comprador = null;
    }

    @Override
    public String toString() {
        return "Bilhete #" + id + " - " + jogo.getEquipaCasa().getNome() + " vs " + jogo.getEquipaFora().getNome()
                + " | " + bancada.getNome() + " | Fila " + fila + " Lugar " + assento + " | " + estado;
    }
}