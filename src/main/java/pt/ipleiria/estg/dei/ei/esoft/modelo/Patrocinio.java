package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Patrocinio {
    private String nomePatrocinador;
    private String nif;
    private double valor;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String tipo; // ex: CASINO, LOJA, etc.
    private List<Jogo> jogosAssociados;

    public Patrocinio(String nomePatrocinador, String nif, double valor, LocalDate dataInicio, LocalDate dataFim, String tipo) {
        if (nomePatrocinador == null || nomePatrocinador.isBlank()) throw new IllegalArgumentException("Nome do patrocinador é obrigatório.");
        if (nif == null || nif.isBlank()) throw new IllegalArgumentException("NIF é obrigatório.");
        if (valor <= 0) throw new IllegalArgumentException("Valor deve ser positivo.");
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio))
            throw new IllegalArgumentException("Datas inválidas.");
        if (tipo == null || tipo.isBlank()) throw new IllegalArgumentException("Tipo é obrigatório.");
        this.nomePatrocinador = nomePatrocinador.trim();
        this.nif = nif.trim();
        this.valor = valor;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.tipo = tipo.trim();
        this.jogosAssociados = new ArrayList<>();
    }

    public String getNomePatrocinador() { return nomePatrocinador; }
    public String getNif() { return nif; }
    public double getValor() { return valor; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public String getTipo() { return tipo; }
    public List<Jogo> getJogosAssociados() { return Collections.unmodifiableList(jogosAssociados); }

    public void adicionarJogo(Jogo jogo) {
        if (jogo != null && !jogosAssociados.contains(jogo)) jogosAssociados.add(jogo);
    }
    public void removerJogo(Jogo jogo) { jogosAssociados.remove(jogo); }

    public void setValor(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("Valor deve ser positivo.");
        this.valor = valor;
    }

    public void setDatas(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio))
            throw new IllegalArgumentException("Datas inválidas.");
        this.dataInicio = inicio;
        this.dataFim = fim;
    }

    public boolean temJogosRealizados() {
        return jogosAssociados.stream().anyMatch(j -> j.getEstado() == Jogo.Estado.TERMINADO);
    }

    public void setNomePatrocinador(String nome) { this.nomePatrocinador = nome.trim(); }
    public void setNif(String nif) { this.nif = nif.trim(); }
    public void setTipo(String tipo) { this.tipo = tipo.trim(); }
}