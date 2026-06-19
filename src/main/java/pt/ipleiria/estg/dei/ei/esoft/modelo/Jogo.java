package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Jogo {
    public enum EstadoJogo { CALENDARIZADO, COMEÇADO, TERMINADO }

    private String id;
    private Equipa equipaA;
    private Equipa equipaB;
    private Estadio estadio;
    private LocalDateTime dataHora;
    private EstadoJogo estado;
    private int golosA;
    private int golosB;
    private List<EventoJogo> eventos;
    private List<Bilhete> bilhetes;

    public Jogo(Equipa equipaA, Equipa equipaB, Estadio estadio, LocalDateTime dataHora) {
        this.id = UUID.randomUUID().toString();
        this.equipaA = equipaA;
        this.equipaB = equipaB;
        this.estadio = estadio;
        this.dataHora = dataHora;
        this.estado = EstadoJogo.CALENDARIZADO;
        this.golosA = 0;
        this.golosB = 0;
        this.eventos = new ArrayList<>();
        this.bilhetes = new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public Equipa getEquipaA() { return equipaA; }
    public Equipa getEquipaB() { return equipaB; }
    public Estadio getEstadio() { return estadio; }
    public LocalDateTime getDataHora() { return dataHora; }
    public EstadoJogo getEstado() { return estado; }
    public int getGolosA() { return golosA; }
    public int getGolosB() { return golosB; }
    public List<EventoJogo> getEventos() { return new ArrayList<>(eventos); }
    public List<Bilhete> getBilhetes() { return new ArrayList<>(bilhetes); }

    public void setEstado(EstadoJogo estado) { this.estado = estado; }
    public void setResultado(int golosA, int golosB) {
        this.golosA = golosA;
        this.golosB = golosB;
    }

    public void adicionarEvento(EventoJogo evento) {
        eventos.add(evento);
        if (evento.getTipo() == EventoJogo.TipoEvento.GOLO) {
            if (evento.getEquipa() == equipaA) golosA++;
            else if (evento.getEquipa() == equipaB) golosB++;
        }
    }

    // UC13 – Reserva / Venda
    public synchronized Bilhete reservarLugar(String lugarId) {
        boolean jaOcupado = bilhetes.stream()
                .anyMatch(b -> b.getLugarId().equals(lugarId)
                        && (b.getEstado() == Bilhete.EstadoBilhete.RESERVADO
                        || b.getEstado() == Bilhete.EstadoBilhete.VENDIDO));
        if (jaOcupado) throw new IllegalStateException("Lugar indisponível");

        Bancada bancada = estadio.getBancadaPorLugarId(lugarId);
        if (bancada == null) throw new IllegalArgumentException("Bancada não encontrada para o lugar");

        Bilhete bilhete = new Bilhete(lugarId, this, bancada.getPreco());
        bilhetes.add(bilhete);
        return bilhete;
    }

    public void confirmarCompra(Bilhete bilhete, String nome, String nif, String contacto) {
        if (!ValidadorNIF.validar(nif))
            throw new IllegalArgumentException("NIF inválido");
        if (bilhete.getEstado() != Bilhete.EstadoBilhete.RESERVADO)
            throw new IllegalStateException("Bilhete não está reservado");
        if (bilhete.tempoReservaExpirado()) {
            bilhetes.remove(bilhete);
            throw new IllegalStateException("Reserva expirada");
        }
        bilhete.setEstado(Bilhete.EstadoBilhete.VENDIDO);
        bilhete.setCompradorNome(nome);
        bilhete.setCompradorNIF(nif);
        bilhete.setCompradorContacto(contacto);
        bilhete.setDataVenda(LocalDateTime.now());
    }

    public void cancelarReserva(Bilhete bilhete) {
        if (bilhete.getEstado() == Bilhete.EstadoBilhete.RESERVADO) {
            bilhetes.remove(bilhete);
        }
    }

    public double getReceitaBilheteira() {
        return bilhetes.stream()
                .filter(b -> b.getEstado() == Bilhete.EstadoBilhete.VENDIDO)
                .mapToDouble(Bilhete::getPreco)
                .sum();
    }

    public int getLugaresVendidos() {
        return (int) bilhetes.stream()
                .filter(b -> b.getEstado() == Bilhete.EstadoBilhete.VENDIDO)
                .count();
    }

    public int getLugaresReservados() {
        return (int) bilhetes.stream()
                .filter(b -> b.getEstado() == Bilhete.EstadoBilhete.RESERVADO)
                .count();
    }
}