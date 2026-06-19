package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.time.LocalDateTime;
import java.util.UUID;

public class Bilhete {
    public enum EstadoBilhete { RESERVADO, VENDIDO }

    private String id;
    private String lugarId;
    private Jogo jogo;
    private double preco;
    private EstadoBilhete estado;
    private String compradorNome;
    private String compradorNIF;
    private String compradorContacto;
    private LocalDateTime dataReserva;
    private LocalDateTime dataVenda;

    public Bilhete(String lugarId, Jogo jogo, double preco) {
        this.id = UUID.randomUUID().toString();
        this.lugarId = lugarId;
        this.jogo = jogo;
        this.preco = preco;
        this.estado = EstadoBilhete.RESERVADO;
        this.dataReserva = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getLugarId() { return lugarId; }
    public Jogo getJogo() { return jogo; }
    public double getPreco() { return preco; }
    public EstadoBilhete getEstado() { return estado; }
    public void setEstado(EstadoBilhete estado) { this.estado = estado; }
    public String getCompradorNome() { return compradorNome; }
    public void setCompradorNome(String compradorNome) { this.compradorNome = compradorNome; }
    public String getCompradorNIF() { return compradorNIF; }
    public void setCompradorNIF(String compradorNIF) { this.compradorNIF = compradorNIF; }
    public String getCompradorContacto() { return compradorContacto; }
    public void setCompradorContacto(String compradorContacto) { this.compradorContacto = compradorContacto; }
    public LocalDateTime getDataReserva() { return dataReserva; }
    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime dataVenda) { this.dataVenda = dataVenda; }

    public boolean tempoReservaExpirado() {
        if (estado != EstadoBilhete.RESERVADO) return false;
        return dataReserva.plusMinutes(10).isBefore(LocalDateTime.now());
    }
}