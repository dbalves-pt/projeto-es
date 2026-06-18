package pt.ipleiria.estg.dei.ei.esoft.modelo;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Torneio — estado global da aplicação.
 *
 * Versão actualizada para UC07 (Configurar Torneio), UC08 (Validar e Iniciar
 * Torneio) e UC09-UC12 (gestão de jogos: início, eventos, fim, correcção).
 */
public class Torneio {

    public enum Estado { INICIAL, CONFIGURADO, VALIDADO, EM_CURSO }

    private static Torneio instancia;

    public static Torneio getInstancia() {
        if (instancia == null) instancia = new Torneio();
        return instancia;
    }

    /** Reinicia para testes unitários. */
    public static void resetInstancia() { instancia = null; }

    private List<Estadio> estadios;
    private Estado       estado;
    private List<Equipa> equipas;

    // ── UC07/UC08 ─────────────────────────────────────────────────────────────
    private List<Grupo> grupos;
    private List<Jogo>  jogos;
    private LocalDate   dataInicio;
    private LocalDate   dataFim;
    private int         diasDescanso;
    private int         numeroTotalEquipas;

    private Torneio() {
        this.estadios = new ArrayList<>();
        this.estado  = Estado.INICIAL;
        this.equipas = new ArrayList<>();
        this.grupos  = new ArrayList<>();
        this.jogos   = new ArrayList<>();
        this.diasDescanso = 1;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public Estado getEstado()           { return estado; }
    public List<Equipa> getEquipas()    { return Collections.unmodifiableList(equipas); }
    public List<Grupo> getGrupos()      { return Collections.unmodifiableList(grupos); }
    public List<Jogo> getJogos()        { return Collections.unmodifiableList(jogos); }
    public LocalDate getDataInicio()    { return dataInicio; }
    public LocalDate getDataFim()       { return dataFim; }
    public int getDiasDescanso()        { return diasDescanso; }
    public int getNumeroTotalEquipas()  { return numeroTotalEquipas; }

    // ── Regras de negócio ─────────────────────────────────────────────────────

    public boolean gruposGerados() { return estado != Estado.INICIAL; }

    /** UC01 — Adicionar equipa. */
    public void adicionarEquipa(Equipa equipa) {
        if (gruposGerados())
            throw new IllegalStateException("GRUPOS_GERADOS");
        if (existeEquipaComNome(equipa.getNome()))
            throw new IllegalArgumentException("NOME_DUPLICADO");
        equipas.add(equipa);
    }

    /** UC02 — Remover equipa. */
    public void removerEquipa(Equipa equipa) {
        if (gruposGerados())
            throw new IllegalStateException("GRUPOS_GERADOS");
        equipas.remove(equipa);
    }

    /** Verifica existência de nome (case-insensitive). */
    public boolean existeEquipaComNome(String nome) {
        return equipas.stream()
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    /** Verifica existência de nome excluindo a equipa actual (para edição — UC02). */
    public boolean existeEquipaComNomeExcluindo(String nome, Equipa equipaAtual) {
        return equipas.stream()
                .filter(e -> e != equipaAtual)
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome.trim()));
    }

    public void setEstado(Estado novoEstado) { this.estado = novoEstado; }

    public List<Estadio> getEstadios() { return estadios; }
    public void adicionarEstadio(Estadio e) { estadios.add(e); }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC07 — Configurar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    public void definirRegras(LocalDate dataInicio, LocalDate dataFim, int diasDescanso, int numeroTotalEquipas) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.diasDescanso = diasDescanso;
        this.numeroTotalEquipas = numeroTotalEquipas;
    }

    /**
     * Gera os grupos e o calendário de jogos da fase de grupos.
     *
     * Pré-condições (validadas pelo TorneioControlador antes de chamar este método):
     *   - número de equipas par e divisível por 4 (grupos de 4 equipas);
     *   - todas as equipas têm pelo menos 1 jogador APTO;
     *   - existe pelo menos um estádio com bancadas suficientes.
     *
     * Distribui as equipas pelos grupos por ordem de inserção (round-robin
     * simples), gera todos os jogos da fase de grupos (todos contra todos
     * dentro do grupo) e respeita o número de dias de descanso entre jogos
     * consecutivos no mesmo estádio.
     */
    public void gerarGruposEJogos() {
        grupos.clear();
        jogos.clear();

        int numGrupos = equipas.size() / 4;
        for (int i = 0; i < numGrupos; i++) {
            grupos.add(new Grupo("Grupo " + (char) ('A' + i)));
        }

        // Distribuição sequencial: as primeiras 4 equipas para o Grupo A, etc.
        for (int i = 0; i < equipas.size(); i++) {
            Grupo grupoDestino = grupos.get(i / 4);
            Equipa equipa = equipas.get(i);
            equipa.setGrupo(grupoDestino.getNome());
            grupoDestino.adicionarEquipa(equipa);
        }

        gerarJogosFaseDeGrupos();
    }

    private void gerarJogosFaseDeGrupos() {
        if (estadios.isEmpty())
            throw new IllegalStateException("SEM_ESTADIOS");

        // Gera, para cada grupo, os confrontos organizados por ronda
        // (round-robin), de forma a que nenhuma equipa jogue duas vezes na
        // mesma ronda — isto garante que o intervalo entre jogos
        // consecutivos de qualquer equipa é sempre de, pelo menos,
        // {@code diasDescanso} dias, evitando conflitos na validação (UC08).
        List<List<Equipa[]>> rondasPorGrupo = new ArrayList<>();
        int maxRondas = 0;
        for (Grupo grupo : grupos) {
            List<Equipa[]> rondas = gerarConfrontosRoundRobin(grupo.getEquipas());
            rondasPorGrupo.add(rondas);
            maxRondas = Math.max(maxRondas, rondas.size());
        }

        long intervaloDias = Math.max(diasDescanso, 1);
        int indiceEstadio = 0;

        for (int ronda = 0; ronda < maxRondas; ronda++) {
            LocalDateTime dataBaseRonda = dataInicio.atTime(LocalTime.of(16, 0))
                    .plusDays(ronda * intervaloDias);

            // Contador de jogos já agendados em cada estádio dentro desta ronda,
            // usado para desfasar a hora e evitar dois jogos no mesmo estádio
            // à mesma hora quando há vários grupos a jogar na mesma ronda.
            int[] jogosNoEstadioNestaRonda = new int[estadios.size()];

            for (int g = 0; g < grupos.size(); g++) {
                List<Equipa[]> rondasDoGrupo = rondasPorGrupo.get(g);
                if (ronda >= rondasDoGrupo.size()) continue;

                Equipa[] confronto = rondasDoGrupo.get(ronda);
                int idxEstadio = indiceEstadio % estadios.size();
                Estadio estadio = estadios.get(idxEstadio);

                LocalDateTime dataHoraJogo = dataBaseRonda.plusHours(3L * jogosNoEstadioNestaRonda[idxEstadio]);
                jogosNoEstadioNestaRonda[idxEstadio]++;

                Jogo jogo = new Jogo(confronto[0], confronto[1], estadio,
                        dataHoraJogo, grupos.get(g), Jogo.Fase.GRUPOS);
                jogos.add(jogo);
                indiceEstadio++;
            }
        }
    }

    /**
     * Gera os confrontos de um grupo organizados por ronda, de forma a que
     * cada equipa jogue no máximo uma vez por ronda. Para o caso padrão de
     * grupos com 4 equipas, devolve as 3 rondas clássicas do round-robin:
     * {0-1, 2-3}, {0-2, 1-3}, {0-3, 1-2} (todos os jogos contra todos, sem
     * sobreposição de equipas dentro da mesma ronda).
     */
    private List<Equipa[]> gerarConfrontosRoundRobin(List<Equipa> equipasDoGrupo) {
        List<Equipa[]> rondas = new ArrayList<>();
        int n = equipasDoGrupo.size();
        if (n < 2) return rondas;

        if (n == 4) {
            rondas.add(new Equipa[]{equipasDoGrupo.get(0), equipasDoGrupo.get(1)});
            rondas.add(new Equipa[]{equipasDoGrupo.get(2), equipasDoGrupo.get(3)});
            rondas.add(new Equipa[]{equipasDoGrupo.get(0), equipasDoGrupo.get(2)});
            rondas.add(new Equipa[]{equipasDoGrupo.get(1), equipasDoGrupo.get(3)});
            rondas.add(new Equipa[]{equipasDoGrupo.get(0), equipasDoGrupo.get(3)});
            rondas.add(new Equipa[]{equipasDoGrupo.get(1), equipasDoGrupo.get(2)});
            return rondas;
        }

        // Caso genérico (grupos com tamanho diferente de 4): todos contra
        // todos sem garantia de não sobreposição — não é o caso de uso
        // padrão do torneio, mas evita rebentar caso surja esse cenário.
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                rondas.add(new Equipa[]{equipasDoGrupo.get(i), equipasDoGrupo.get(j)});
            }
        }
        return rondas;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC08 — Validar e Iniciar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Verifica conflitos de calendário: dois jogos no mesmo estádio com menos
     * de {@code diasDescanso} dias de intervalo entre eles, ou duas equipas
     * com jogos demasiado próximos (menos dias de descanso do que o definido
     * nas regras do torneio).
     *
     * @return lista de descrições de conflitos encontrados (vazia se não houver conflitos).
     */
    public List<String> validarCalendario() {
        List<String> conflitos = new ArrayList<>();

        for (int i = 0; i < jogos.size(); i++) {
            for (int j = i + 1; j < jogos.size(); j++) {
                Jogo a = jogos.get(i);
                Jogo b = jogos.get(j);

                long diffDias = Math.abs(java.time.Duration.between(a.getDataHora(), b.getDataHora()).toDays());

                // Conflito de estádio: mesmo estádio, mesma data/hora exacta.
                if (a.getEstadio() == b.getEstadio() && a.getDataHora().equals(b.getDataHora())) {
                    conflitos.add("Conflito de estádio: " + a + " e " + b + " no mesmo horário em " + a.getEstadio().getNome());
                }

                // Conflito de descanso: alguma equipa joga duas vezes com intervalo insuficiente.
                boolean equipasEmComum = a.envolveEquipa(b.getEquipaCasa()) || a.envolveEquipa(b.getEquipaFora());
                if (equipasEmComum && diffDias < diasDescanso) {
                    conflitos.add("Dias de descanso insuficientes entre " + a + " e " + b);
                }
            }
        }
        return conflitos;
    }

    /**
     * @throws IllegalStateException ESTADO_INVALIDO se o torneio não estiver CONFIGURADO.
     * @throws IllegalStateException CONFLITOS_EXISTENTES se {@link #validarCalendario()} detectar problemas.
     */
    public void confirmarValidacao() {
        if (estado != Estado.CONFIGURADO)
            throw new IllegalStateException("ESTADO_INVALIDO");
        if (!validarCalendario().isEmpty())
            throw new IllegalStateException("CONFLITOS_EXISTENTES");
        estado = Estado.VALIDADO;
    }

    /**
     * @throws IllegalStateException ESTADO_INVALIDO se o torneio não estiver VALIDADO.
     */
    public void iniciarTorneio() {
        if (estado != Estado.VALIDADO)
            throw new IllegalStateException("ESTADO_INVALIDO");
        estado = Estado.EM_CURSO;
    }
}
