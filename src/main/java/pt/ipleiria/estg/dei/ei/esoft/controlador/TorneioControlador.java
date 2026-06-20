package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador UC07 (Configurar Torneio) e UC08 (Validar e Iniciar Torneio).
 *
 * UC07 — Caminho Principal:
 * 1. Utilizador preenche datas, define regras (dias de descanso, número
 * total de equipas) e clica em 'Gerar torneio'.
 * 2. Sistema valida datas, número de equipas, dias de descanso e se as
 * equipas têm jogadores APTOS.
 * 3. Sistema distribui as equipas, gera os jogos e altera o estado para
 * CONFIGURADO, bloqueando a edição de equipas e jogadores.
 *
 * UC08 — Caminho Principal:
 * 1. Utilizador clica em 'Validar Calendário'.
 * 2. Sistema verifica conflitos de horários/estádios/dias de descanso.
 * 3. Sistema apresenta o relatório; se sem conflitos, activa 'Confirmar Validação'.
 * 4. Utilizador confirma -> estado passa a VALIDADO.
 * 5. Utilizador clica em 'Iniciar Torneio' -> diálogo de confirmação -> estado EM_CURSO.
 */
public class TorneioControlador {

    private final Torneio torneio;

    public TorneioControlador() {
        this.torneio = Torneio.getInstancia();
    }

    public TorneioControlador(Torneio t) {
        this.torneio = t;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC07 — Configurar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida e gera o torneio (grupos + calendário de jogos da fase de grupos).
     * <p>
     * Códigos de erro lançados (IllegalArgumentException / IllegalStateException):
     * DATAS_INVALIDAS            – CA 4.1: datas em falta, mal formatadas ou fim antes do início.
     * EQUIPAS_INCOMPATIVEIS      – CA 4.2: número de equipas não é par nem divisível por 4.
     * EQUIPAS_SEM_JOGADORES      – CA 5.1: pelo menos uma equipa sem jogadores APTOS.
     * SEM_ESTADIOS               – CA 6.1: não existe nenhum estádio criado para alocar os jogos.
     */
    public void configurarTorneio(LocalDate dataInicio, LocalDate dataFim, int diasDescanso) {

        // CA 4.1 — Datas inválidas (básicas)
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio))
            throw new IllegalArgumentException("DATAS_INVALIDAS");

        // ── VALIDAÇÃO: DATA DE INÍCIO APÓS O DIA DE HOJE ──
        if (dataInicio.isBefore(LocalDate.now()) || dataInicio.isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("A data de início do torneio tem de ser posterior ao dia de hoje.");
        }

        // ── VALIDAÇÃO: DURAÇÃO MÍNIMA DE 30 DIAS ──
        long diasDeTorneio = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim);
        if (diasDeTorneio < 30) {
            throw new IllegalArgumentException("O torneio tem de ter uma duração mínima de 30 dias.");
        }

        if (diasDescanso < 0)
            throw new IllegalArgumentException("DIAS_DESCANSO_INVALIDOS");

        List<Equipa> equipas = torneio.getEquipas();

        // CA 4.2 — Equipas incompatíveis com grupos (grupos de 4 equipas)
        if (equipas.isEmpty() || equipas.size() % 4 != 0)
            throw new IllegalArgumentException("EQUIPAS_INCOMPATIVEIS");

        // ── NOVA VALIDAÇÃO: OBRIGA A TER TREINADOR ──
        List<String> equipasSemTreinador = new ArrayList<>();
        for (Equipa eq : equipas) {
            if (eq.getTreinador() == null || eq.getTreinador().isBlank() || eq.getTreinador().equals("Sem Treinador")) {
                equipasSemTreinador.add(eq.getNome());
            }
        }
        if (!equipasSemTreinador.isEmpty()) {
            throw new IllegalArgumentException("As seguintes equipas não têm um treinador atribuído:\n" + String.join(", ", equipasSemTreinador) + "\n\nEdite estas equipas antes de gerar o torneio.");
        }
        // ────────────────────────────────────────────

        // CA 5.1 — Equipas sem jogadores APTOS suficientes
        List<String> equipasSemJogadores = equipasSemJogadoresAptos();
        if (!equipasSemJogadores.isEmpty())
            throw new IllegalArgumentException("EQUIPAS_SEM_JOGADORES: " + String.join(", ", equipasSemJogadores));

        // CA 6.1 — Impossível alocar jogos (sem estádios disponíveis)
        if (torneio.getEstadios().isEmpty())
            throw new IllegalStateException("SEM_ESTADIOS");

        torneio.definirRegras(dataInicio, dataFim, diasDescanso, equipas.size());
        torneio.gerarGruposEJogos();
        torneio.setEstado(Torneio.Estado.CONFIGURADO);
    }

    /**
     * Lista as equipas que não têm nenhum jogador no estado APTO.
     */
    public List<String> equipasSemJogadoresAptos() {
        List<String> resultado = new ArrayList<>();
        for (Equipa eq : torneio.getEquipas()) {
            boolean temApto = eq.getJogadores().stream()
                    .anyMatch(j -> j.getEstado() == Jogador.Estado.APTO);
            if (!temApto) resultado.add(eq.getNome());
        }
        return resultado;
    }

    public boolean isConfiguracaoBloqueada() {
        return torneio.gruposGerados();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC08 — Validar e Iniciar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * @return lista de conflitos encontrados (vazia significa "sem conflitos").
     * @throws IllegalStateException ESTADO_INVALIDO se o torneio ainda não estiver CONFIGURADO.
     */
    public List<String> validarCalendario() {
        if (torneio.getEstado() != Torneio.Estado.CONFIGURADO
                && torneio.getEstado() != Torneio.Estado.VALIDADO)
            throw new IllegalStateException("ESTADO_INVALIDO");
        return torneio.validarCalendario();
    }

    /**
     * @throws IllegalStateException ESTADO_INVALIDO / CONFLITOS_EXISTENTES.
     */
    public void confirmarValidacao() {
        torneio.confirmarValidacao();
    }

    /**
     * @throws IllegalStateException ESTADO_INVALIDO se o torneio não estiver VALIDADO.
     */
    public void iniciarTorneio() {
        torneio.iniciarTorneio();
    }

    public boolean podeValidar() {
        return torneio.getEstado() == Torneio.Estado.CONFIGURADO;
    }

    public boolean podeIniciar() {
        return torneio.getEstado() == Torneio.Estado.VALIDADO;
    }

    public boolean torneioEmCurso() {
        return torneio.getEstado() == Torneio.Estado.EM_CURSO;
    }

    // ── Auxiliares para a Vista ───────────────────────────────────────────────
    public List<Grupo> getGrupos() {
        return torneio.getGrupos();
    }

    public List<Jogo> getJogos() {
        return torneio.getJogos();
    }

    public Torneio.Estado getEstadoTorneio() {
        return torneio.getEstado();
    }

    /**
     * Motor progressivo do torneio. Gera a próxima fase mediante a fase atual.
     * Grupos -> Quartos -> Meias -> Final.
     */
    public void gerarProximaFase() {
        pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio torneio = pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.getInstancia();
        List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> todosJogos = torneio.getJogos();

        // 1. Descobrir a fase mais avançada atual no calendário
        pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase faseAtual = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.GRUPOS;
        for (pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo j : todosJogos) {
            if (j.getFase() == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.FINAL) faseAtual = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.FINAL;
            else if (j.getFase() == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.MEIAS && faseAtual != pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.FINAL) faseAtual = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.MEIAS;
            else if (j.getFase() == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.QUARTOS && faseAtual != pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.FINAL && faseAtual != pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.MEIAS) faseAtual = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.QUARTOS;
            else if (j.getFase() == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.OITAVOS && faseAtual == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.GRUPOS) faseAtual = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.OITAVOS;
        }

        // 2. Obter os jogos dessa fase específica e verificar se estão todos terminados
        pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase fAtual = faseAtual;
        List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> jogosFaseAtual = todosJogos.stream()
                .filter(j -> j.getFase() == fAtual)
                .toList();

        if (!jogosFaseAtual.stream().allMatch(j -> j.getEstado() == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Estado.TERMINADO)) {
            throw new IllegalStateException("Ainda existem jogos da fase atual (" + fAtual + ") por terminar!");
        }

        pt.ipleiria.estg.dei.ei.esoft.modelo.Estadio estadio = torneio.getEstadios().get(0);
        java.time.LocalDateTime ultimaData = todosJogos.stream()
                .map(pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo::getDataHora)
                .max(java.time.LocalDateTime::compareTo)
                .orElse(java.time.LocalDateTime.now());

        int diaExtra = 2;

        // ── 3. LÓGICA A: Gerar a 1ª fase eliminatória a partir dos Grupos ──
        if (faseAtual == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.GRUPOS) {
            List<pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo> grupos = torneio.getGrupos();
            int numGrupos = grupos.size();

            if (numGrupos == 1) { // 4 Equipas -> FINAL DIRETA (Top 2)
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa e1 = grupos.get(0).calcularClassificacao(todosJogos).get(0).getEquipa();
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa e2 = grupos.get(0).calcularClassificacao(todosJogos).get(1).getEquipa();
                torneio.adicionarJogo(new pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo(e1, e2, estadio, ultimaData.plusDays(diaExtra), null, pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.FINAL));
            }
            else if (numGrupos == 2) { // 8 Equipas -> MEIAS FINAIS
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa g1_1 = grupos.get(0).calcularClassificacao(todosJogos).get(0).getEquipa();
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa g1_2 = grupos.get(0).calcularClassificacao(todosJogos).get(1).getEquipa();
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa g2_1 = grupos.get(1).calcularClassificacao(todosJogos).get(0).getEquipa();
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa g2_2 = grupos.get(1).calcularClassificacao(todosJogos).get(1).getEquipa();

                torneio.adicionarJogo(new pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo(g1_1, g2_2, estadio, ultimaData.plusDays(diaExtra++), null, pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.MEIAS));
                torneio.adicionarJogo(new pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo(g2_1, g1_2, estadio, ultimaData.plusDays(diaExtra), null, pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.MEIAS));
            }
            else if (numGrupos == 4) { // 16 Equipas -> QUARTOS DE FINAL
                for (int i = 0; i < numGrupos; i += 2) {
                    pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa p1 = grupos.get(i).calcularClassificacao(todosJogos).get(0).getEquipa();
                    pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa s1 = grupos.get(i).calcularClassificacao(todosJogos).get(1).getEquipa();
                    pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa p2 = grupos.get(i+1).calcularClassificacao(todosJogos).get(0).getEquipa();
                    pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa s2 = grupos.get(i+1).calcularClassificacao(todosJogos).get(1).getEquipa();

                    torneio.adicionarJogo(new pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo(p1, s2, estadio, ultimaData.plusDays(diaExtra++), null, pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.QUARTOS));
                    torneio.adicionarJogo(new pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo(p2, s1, estadio, ultimaData.plusDays(diaExtra++), null, pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.QUARTOS));
                }
            }
        }
        // ── 4. LÓGICA B: Declarar Campeão se a Final já acabou ──
        else if (faseAtual == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.FINAL) {
            pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa vencedor = obterVencedor(jogosFaseAtual.get(0));
            throw new IllegalStateException("O Torneio já terminou! A equipa campeã é: " + vencedor.getNome() + " \uD83C\uDFC6");
        }
        // ── 5. LÓGICA C: Avançar vencedores para a fase seguinte ──
        else {
            pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase proximaFase;
            if (faseAtual == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.QUARTOS) proximaFase = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.MEIAS;
            else proximaFase = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.FINAL;

            // Pega nos jogos 2 a 2, descobre os vencedores e agenda o novo jogo
            for (int i = 0; i < jogosFaseAtual.size(); i += 2) {
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa v1 = obterVencedor(jogosFaseAtual.get(i));
                pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa v2 = obterVencedor(jogosFaseAtual.get(i+1));
                torneio.adicionarJogo(new pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo(v1, v2, estadio, ultimaData.plusDays(diaExtra++), null, proximaFase));
            }
        }
    }

    /**
     * Helper para garantir que nas eliminatórias as equipas não empatam.
     */
    private pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa obterVencedor(pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo j) {
        if (j.getGolosCasa() > j.getGolosFora()) return j.getEquipaCasa();
        if (j.getGolosFora() > j.getGolosCasa()) return j.getEquipaFora();
        throw new IllegalStateException("O jogo " + j.getEquipaCasa().getNome() + " x " + j.getEquipaFora().getNome() + " terminou empatado! Adicione um golo (ex: penálti) nas estatísticas para declarar o vencedor.");
    }

}