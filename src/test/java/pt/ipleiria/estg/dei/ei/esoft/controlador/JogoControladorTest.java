package pt.ipleiria.estg.dei.ei.esoft.controlador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.modelo.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JogoControladorTest {

    private Torneio torneio;
    private JogoControlador jogoControlador;
    private EventoControlador eventoControlador;
    private TorneioControlador torneioControlador;
    private EquipaControlador equipaControlador;
    private JogadorControlador jogadorControlador;
    private EstadioControlador estadioControlador;

    @BeforeEach
    void setUp() {
        Torneio.resetInstancia();
        torneio = Torneio.getInstancia();
        jogoControlador = new JogoControlador(torneio);
        eventoControlador = new EventoControlador();
        torneioControlador = new TorneioControlador(torneio);
        equipaControlador = new EquipaControlador(torneio);
        jogadorControlador = new JogadorControlador(torneio);
        estadioControlador = new EstadioControlador(torneio);

        for (String nome : new String[]{"Portugal", "Espanha", "França", "Brasil"}) {
            equipaControlador.adicionarEquipa(nome, nome.equals("Brasil") ? "Brasil" : nome);
        }
        for (Equipa eq : torneio.getEquipas()) {
            jogadorControlador.adicionarJogador(eq, "Jogador de " + eq.getNome(),
                    "Avançado", "01/01/2000", "10", "Apto");
        }
        estadioControlador.adicionarEstadio("Estádio Nacional", "Lisboa", "Portugal", "50000");
        torneioControlador.configurarTorneio(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 30), 1);
    }

    private Jogo primeiroJogo() {
        return torneio.getJogos().get(0);
    }

    @Test
    void testIniciarJogoComSucesso() {
        Jogo jogo = primeiroJogo();
        jogoControlador.iniciarJogo(jogo);
        assertEquals(Jogo.Estado.COMECADO, jogo.getEstado());
        assertNotNull(jogo.getHoraInicio());
    }

    @Test
    void testErroIniciarJogoJaComecado() {
        Jogo jogo = primeiroJogo();
        jogoControlador.iniciarJogo(jogo);

        Exception ex = assertThrows(IllegalStateException.class, () -> jogoControlador.iniciarJogo(jogo));
        assertEquals("JOGO_NAO_CALENDARIZADO", ex.getMessage());
    }

    @Test
    void testErroIniciarJogoNulo() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> jogoControlador.iniciarJogo(null));
        assertEquals("JOGO_NULO", ex.getMessage());
    }

    @Test
    void testTerminarJogoComSucessoCalculaResultado() {
        Jogo jogo = primeiroJogo();
        jogoControlador.iniciarJogo(jogo);

        Equipa casa = jogo.getEquipaCasa();
        Jogador marcador = casa.getJogadores().get(0);
        eventoControlador.registarEvento(jogo, "Golo", casa, marcador, "10");

        jogoControlador.terminarJogo(jogo);

        assertEquals(Jogo.Estado.TERMINADO, jogo.getEstado());
        assertEquals(1, jogo.getGolosCasa());
        assertEquals(0, jogo.getGolosFora());
        assertNotNull(jogo.getHoraFim());
    }

    @Test
    void testErroTerminarJogoNaoComecado() {
        Jogo jogo = primeiroJogo();
        Exception ex = assertThrows(IllegalStateException.class, () -> jogoControlador.terminarJogo(jogo));
        assertEquals("JOGO_NAO_COMECADO", ex.getMessage());
    }

    @Test
    void testApuramentoQuandoTodosOsJogosDoGrupoTerminam() {
        Grupo grupo = torneio.getGrupos().get(0);
        List<Jogo> jogosDoGrupo = jogoControlador.getJogosPorGrupo(grupo);

        List<Equipa> apurados = List.of();
        for (Jogo j : jogosDoGrupo) {
            jogoControlador.iniciarJogo(j);
            apurados = jogoControlador.terminarJogo(j);
        }

        // Depois do último jogo do grupo terminar, devem ser devolvidas 2 equipas apuradas.
        assertEquals(2, apurados.size());
    }

    @Test
    void testSemApuramentoEnquantoFaltamJogosDoGrupo() {
        Grupo grupo = torneio.getGrupos().get(0);
        List<Jogo> jogosDoGrupo = jogoControlador.getJogosPorGrupo(grupo);

        Jogo primeiro = jogosDoGrupo.get(0);
        jogoControlador.iniciarJogo(primeiro);
        List<Equipa> apurados = jogoControlador.terminarJogo(primeiro);

        assertTrue(apurados.isEmpty());
    }
}
