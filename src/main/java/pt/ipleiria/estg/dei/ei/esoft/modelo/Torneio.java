package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Torneio {

    public enum Estado { INICIAL, CONFIGURADO, VALIDADO, EM_CURSO }

    private static Torneio instancia;

    public static Torneio getInstancia() {
        if (instancia == null) instancia = new Torneio();
        return instancia;
    }

    public static void resetInstancia() { instancia = null; }

    private Estado estado;
    private List<Equipa> equipas;
    private List<Estadio> estadios;
    private List<Jogo> jogos;
    private List<Patrocinio> patrocinios;
    private Map<String, List<Equipa>> grupos; // nome do grupo -> equipas

    private Torneio() {
        this.estado = Estado.INICIAL;
        this.equipas = new ArrayList<>();
        this.estadios = new ArrayList<>();
        this.jogos = new ArrayList<>();
        this.patrocinios = new ArrayList<>();
        this.grupos = new LinkedHashMap<>();
    }

    // --- Getters existentes ---
    public Estado getEstado() { return estado; }
    public List<Equipa> getEquipas() { return Collections.unmodifiableList(equipas); }
    public List<Estadio> getEstadios() { return Collections.unmodifiableList(estadios); }
    public List<Jogo> getJogos() { return Collections.unmodifiableList(jogos); }
    public List<Patrocinio> getPatrocinios() { return Collections.unmodifiableList(patrocinios); }
    public Map<String, List<Equipa>> getGrupos() { return Collections.unmodifiableMap(grupos); }

    // --- Métodos existentes (UC01, UC02, UC03) mantidos ---
    public boolean gruposGerados() { return estado != Estado.INICIAL; }

    public void adicionarEquipa(Equipa equipa) {
        if (gruposGerados())
            throw new IllegalStateException("GRUPOS_GERADOS");
        if (existeEquipaComNome(equipa.getNome()))
            throw new IllegalArgumentException("NOME_DUPLICADO");
        equipas.add(equipa);
    }

    public void removerEquipa(Equipa equipa) {
        if (gruposGerados())
            throw new IllegalStateException("GRUPOS_GERADOS");
        equipas.remove(equipa);
    }

    public boolean existeEquipaComNome(String nome) {
        return equipas.stream().anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    public boolean existeEquipaComNomeExcluindo(String nome, Equipa equipaAtual) {
        return equipas.stream()
                .filter(e -> e != equipaAtual)
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    public void setEstado(Estado novoEstado) { this.estado = novoEstado; }

    // --- Novos métodos para gerir estádios ---
    public void adicionarEstadio(Estadio estadio) {
        // validar nome duplicado?
        estadios.add(estadio);
    }

    // --- Novos métodos para gerir jogos ---
    public Jogo criarJogo(Equipa equipaA, Equipa equipaB, Estadio estadio, LocalDateTime dataHora) {
        Jogo jogo = new Jogo(equipaA, equipaB, estadio, dataHora);
        jogos.add(jogo);
        return jogo;
    }

    public List<Jogo> listarJogosDisponiveis() {
        return jogos.stream()
                .filter(j -> j.getEstado() == Jogo.EstadoJogo.CALENDARIZADO)
                .collect(Collectors.toList());
    }

    // --- UC13 ---
    public Bilhete reservarLugar(Jogo jogo, String lugarId) {
        if (jogo.getEstado() != Jogo.EstadoJogo.CALENDARIZADO)
            throw new IllegalStateException("Jogo não disponível para reserva");
        return jogo.reservarLugar(lugarId);
    }

    public void confirmarCompra(Jogo jogo, Bilhete bilhete, String nome, String nif, String contacto) {
        if (jogo.getEstado() != Jogo.EstadoJogo.CALENDARIZADO)
            throw new IllegalStateException("Jogo não disponível para compra");
        jogo.confirmarCompra(bilhete, nome, nif, contacto);
    }

    public void cancelarReserva(Jogo jogo, Bilhete bilhete) {
        jogo.cancelarReserva(bilhete);
    }

    // --- UC14 ---
    public void aplicarDesconto(Jogo jogo, String nomeBancada, double novoPreco) {
        if (jogo.getEstado() != Jogo.EstadoJogo.CALENDARIZADO)
            throw new IllegalStateException("Não é possível aplicar desconto a jogo iniciado ou terminado.");
        Bancada bancada = jogo.getEstadio().getBancadaPorNome(nomeBancada);
        if (bancada == null) throw new IllegalArgumentException("Bancada não encontrada");
        if (novoPreco <= 0) throw new IllegalArgumentException("Preço deve ser positivo");
        // Verifica se há bilhetes disponíveis (não vendidos)
        int vendidos = jogo.getLugaresVendidos();
        int capacidade = bancada.getCapacidade();
        if (vendidos >= capacidade) {
            throw new IllegalStateException("Todos os bilhetes já foram vendidos.");
        }
        bancada.setPreco(novoPreco);
        // Nota: os bilhetes já vendidos mantêm o preço antigo.
    }

    // --- UC15 ---
    public void criarPatrocinio(String nomeEmpresa, String tipo, double valor, List<Jogo> jogosAssociados) {
        if (valor <= 0) throw new IllegalArgumentException("Valor deve ser positivo");
        Patrocinio p = new Patrocinio(nomeEmpresa, tipo, valor, jogosAssociados);
        patrocinios.add(p);
    }

    public void removerPatrocinio(Patrocinio patrocinio) {
        if (patrocinio.isAssociadoAJogoIniciado())
            throw new IllegalStateException("Não é possível remover: patrocínio associado a jogo já iniciado.");
        patrocinios.remove(patrocinio);
    }

    // --- UC16 ---
    public double getTotalPatrocinios() {
        return patrocinios.stream().mapToDouble(Patrocinio::getValor).sum();
    }

    public double getTotalBilheteira() {
        return jogos.stream().mapToDouble(Jogo::getReceitaBilheteira).sum();
    }

    public Map<Jogo, Double> getReceitaPorJogo() {
        return jogos.stream()
                .collect(Collectors.toMap(j -> j, Jogo::getReceitaBilheteira));
    }

    // --- UC17: Estatísticas ---
    public List<Jogador> getMelhoresMarcadores() {
        Map<Jogador, Integer> golos = new HashMap<>();
        for (Jogo j : jogos) {
            for (EventoJogo e : j.getEventos()) {
                if (e.getTipo() == EventoJogo.TipoEvento.GOLO) {
                    golos.put(e.getJogador(), golos.getOrDefault(e.getJogador(), 0) + 1);
                }
            }
        }
        return golos.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Jogador> getMaisAssistencias() {
        Map<Jogador, Integer> assistencias = new HashMap<>();
        for (Jogo j : jogos) {
            for (EventoJogo e : j.getEventos()) {
                if (e.getTipo() == EventoJogo.TipoEvento.ASSISTENCIA) {
                    assistencias.put(e.getJogador(), assistencias.getOrDefault(e.getJogador(), 0) + 1);
                }
            }
        }
        return assistencias.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Jogador> getMaisDefesas() {
        Map<Jogador, Integer> defesas = new HashMap<>();
        for (Jogo j : jogos) {
            for (EventoJogo e : j.getEventos()) {
                if (e.getTipo() == EventoJogo.TipoEvento.DEFESA) {
                    defesas.put(e.getJogador(), defesas.getOrDefault(e.getJogador(), 0) + 1);
                }
            }
        }
        return defesas.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // --- UC18: Classificação de grupos ---
    public List<ClassificacaoEquipa> getClassificacaoGrupo(String nomeGrupo) {
        List<Equipa> equipasGrupo = grupos.getOrDefault(nomeGrupo, Collections.emptyList());
        if (equipasGrupo.isEmpty()) return Collections.emptyList();

        Map<Equipa, ClassificacaoEquipa> map = new HashMap<>();
        for (Equipa eq : equipasGrupo) {
            map.put(eq, new ClassificacaoEquipa(eq));
        }

        // Processar jogos do grupo (todos os jogos entre equipas do grupo)
        for (Jogo j : jogos) {
            if (j.getEstado() != Jogo.EstadoJogo.TERMINADO) continue;
            Equipa a = j.getEquipaA();
            Equipa b = j.getEquipaB();
            if (!map.containsKey(a) || !map.containsKey(b)) continue;
            ClassificacaoEquipa ca = map.get(a);
            ClassificacaoEquipa cb = map.get(b);
            ca.setJogos(ca.getJogos() + 1);
            cb.setJogos(cb.getJogos() + 1);
            ca.setGolosMarcados(ca.getGolosMarcados() + j.getGolosA());
            ca.setGolosSofridos(ca.getGolosSofridos() + j.getGolosB());
            cb.setGolosMarcados(cb.getGolosMarcados() + j.getGolosB());
            cb.setGolosSofridos(cb.getGolosSofridos() + j.getGolosA());

            if (j.getGolosA() > j.getGolosB()) {
                ca.setVitorias(ca.getVitorias() + 1);
                cb.setDerrotas(cb.getDerrotas() + 1);
                ca.setPontos(ca.getPontos() + 3);
            } else if (j.getGolosA() < j.getGolosB()) {
                cb.setVitorias(cb.getVitorias() + 1);
                ca.setDerrotas(ca.getDerrotas() + 1);
                cb.setPontos(cb.getPontos() + 3);
            } else {
                ca.setEmpates(ca.getEmpates() + 1);
                cb.setEmpates(cb.getEmpates() + 1);
                ca.setPontos(ca.getPontos() + 1);
                cb.setPontos(cb.getPontos() + 1);
            }
        }

        List<ClassificacaoEquipa> lista = new ArrayList<>(map.values());
        lista.sort((c1, c2) -> {
            // Pontos desc, GD desc, GM desc
            if (c1.getPontos() != c2.getPontos()) return c2.getPontos() - c1.getPontos();
            if (c1.getDiferencaGolos() != c2.getDiferencaGolos())
                return c2.getDiferencaGolos() - c1.getDiferencaGolos();
            return c2.getGolosMarcados() - c1.getGolosMarcados();
        });
        for (int i = 0; i < lista.size(); i++) {
            lista.get(i).setPosicao(i + 1);
        }
        return lista;
    }

    public Map<String, List<ClassificacaoEquipa>> getClassificacaoTodosGrupos() {
        Map<String, List<ClassificacaoEquipa>> resultado = new LinkedHashMap<>();
        for (String nomeGrupo : grupos.keySet()) {
            resultado.put(nomeGrupo, getClassificacaoGrupo(nomeGrupo));
        }
        return resultado;
    }

    // --- Métodos auxiliares para testes ---
    public void adicionarEquipaAGrupo(String grupo, Equipa equipa) {
        equipa.setGrupo(grupo);
        grupos.computeIfAbsent(grupo, k -> new ArrayList<>()).add(equipa);
    }
}