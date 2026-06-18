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
 *   1. Utilizador preenche datas, define regras (dias de descanso, número
 *      total de equipas) e clica em 'Gerar torneio'.
 *   2. Sistema valida datas, número de equipas, dias de descanso e se as
 *      equipas têm jogadores APTOS.
 *   3. Sistema distribui as equipas, gera os jogos e altera o estado para
 *      CONFIGURADO, bloqueando a edição de equipas e jogadores.
 *
 * UC08 — Caminho Principal:
 *   1. Utilizador clica em 'Validar Calendário'.
 *   2. Sistema verifica conflitos de horários/estádios/dias de descanso.
 *   3. Sistema apresenta o relatório; se sem conflitos, activa 'Confirmar Validação'.
 *   4. Utilizador confirma -> estado passa a VALIDADO.
 *   5. Utilizador clica em 'Iniciar Torneio' -> diálogo de confirmação -> estado EM_CURSO.
 */
public class TorneioControlador {

    private final Torneio torneio;

    public TorneioControlador()            { this.torneio = Torneio.getInstancia(); }
    public TorneioControlador(Torneio t)   { this.torneio = t; }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC07 — Configurar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida e gera o torneio (grupos + calendário de jogos da fase de grupos).
     *
     * Códigos de erro lançados (IllegalArgumentException / IllegalStateException):
     *   DATAS_INVALIDAS            – CA 4.1: datas em falta, mal formatadas ou fim antes do início.
     *   EQUIPAS_INCOMPATIVEIS      – CA 4.2: número de equipas não é par nem divisível por 4.
     *   EQUIPAS_SEM_JOGADORES      – CA 5.1: pelo menos uma equipa sem jogadores APTOS.
     *   SEM_ESTADIOS               – CA 6.1: não existe nenhum estádio criado para alocar os jogos.
     */
    public void configurarTorneio(LocalDate dataInicio, LocalDate dataFim, int diasDescanso) {

        // CA 4.1 — Datas inválidas
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio))
            throw new IllegalArgumentException("DATAS_INVALIDAS");
        if (diasDescanso < 0)
            throw new IllegalArgumentException("DIAS_DESCANSO_INVALIDOS");

        List<Equipa> equipas = torneio.getEquipas();

        // CA 4.2 — Equipas incompatíveis com grupos (grupos de 4 equipas)
        if (equipas.isEmpty() || equipas.size() % 4 != 0)
            throw new IllegalArgumentException("EQUIPAS_INCOMPATIVEIS");

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

    /** Lista as equipas que não têm nenhum jogador no estado APTO. */
    public List<String> equipasSemJogadoresAptos() {
        List<String> resultado = new ArrayList<>();
        for (Equipa eq : torneio.getEquipas()) {
            boolean temApto = eq.getJogadores().stream()
                    .anyMatch(j -> j.getEstado() == Jogador.Estado.APTO);
            if (!temApto) resultado.add(eq.getNome());
        }
        return resultado;
    }

    public boolean isConfiguracaoBloqueada() { return torneio.gruposGerados(); }

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

    public boolean podeValidar()       { return torneio.getEstado() == Torneio.Estado.CONFIGURADO; }
    public boolean podeIniciar()       { return torneio.getEstado() == Torneio.Estado.VALIDADO; }
    public boolean torneioEmCurso()    { return torneio.getEstado() == Torneio.Estado.EM_CURSO; }

    // ── Auxiliares para a Vista ───────────────────────────────────────────────
    public List<Grupo> getGrupos()   { return torneio.getGrupos(); }
    public List<Jogo>  getJogos()    { return torneio.getJogos(); }
    public Torneio.Estado getEstadoTorneio() { return torneio.getEstado(); }
}
