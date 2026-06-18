package pt.ipleiria.estg.dei.ei.esoft.controlador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.modelo.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TorneioControladorTest {

    private Torneio torneio;
    private TorneioControlador controlador;
    private EquipaControlador equipaControlador;
    private JogadorControlador jogadorControlador;
    private EstadioControlador estadioControlador;

    @BeforeEach
    void setUp() {
        Torneio.resetInstancia();
        torneio = Torneio.getInstancia();
        controlador = new TorneioControlador(torneio);
        equipaControlador = new EquipaControlador(torneio);
        jogadorControlador = new JogadorControlador(torneio);
        estadioControlador = new EstadioControlador(torneio);
    }

    /** Cria 4 equipas, cada uma com 1 jogador APTO, e 1 estádio simples. */
    private void prepararTorneioValido() {
        for (String nome : new String[]{"Portugal", "Espanha", "França", "Brasil"}) {
            equipaControlador.adicionarEquipa(nome, nome.equals("Brasil") ? "Brasil" : nome);
        }
        for (Equipa eq : torneio.getEquipas()) {
            jogadorControlador.adicionarJogador(eq, "Jogador de " + eq.getNome(),
                    "Avançado", "01/01/2000", "10", "Apto");
        }
        estadioControlador.adicionarEstadio("Estádio Nacional", "Lisboa", "Portugal", "50000");
    }

    @Test
    void testConfigurarTorneioComSucesso() {
        prepararTorneioValido();
        controlador.configurarTorneio(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 2);

        assertEquals(Torneio.Estado.CONFIGURADO, torneio.getEstado());
        assertEquals(1, controlador.getGrupos().size());
        // Grupo de 4 equipas: C(4,2) = 6 jogos
        assertEquals(6, controlador.getJogos().size());
    }

    @Test
    void testErroDatasInvalidas() {
        prepararTorneioValido();
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                controlador.configurarTorneio(LocalDate.of(2026, 6, 30), LocalDate.of(2026, 6, 1), 2));
        assertEquals("DATAS_INVALIDAS", ex.getMessage());
    }

    @Test
    void testErroEquipasIncompativeisComGrupos() {
        // Apenas 3 equipas (não divisível por 4)
        equipaControlador.adicionarEquipa("Portugal", "Portugal");
        equipaControlador.adicionarEquipa("Espanha", "Espanha");
        equipaControlador.adicionarEquipa("França", "França");

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                controlador.configurarTorneio(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 2));
        assertEquals("EQUIPAS_INCOMPATIVEIS", ex.getMessage());
    }

    @Test
    void testErroEquipasSemJogadoresAptos() {
        for (String nome : new String[]{"Portugal", "Espanha", "França", "Brasil"}) {
            equipaControlador.adicionarEquipa(nome, nome.equals("Brasil") ? "Brasil" : nome);
        }
        estadioControlador.adicionarEstadio("Estádio Nacional", "Lisboa", "Portugal", "50000");
        // Nenhuma equipa tem jogadores

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                controlador.configurarTorneio(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 2));
        assertTrue(ex.getMessage().startsWith("EQUIPAS_SEM_JOGADORES"));
    }

    @Test
    void testErroSemEstadios() {
        for (String nome : new String[]{"Portugal", "Espanha", "França", "Brasil"}) {
            equipaControlador.adicionarEquipa(nome, nome.equals("Brasil") ? "Brasil" : nome);
        }
        for (Equipa eq : torneio.getEquipas()) {
            jogadorControlador.adicionarJogador(eq, "Jogador de " + eq.getNome(),
                    "Avançado", "01/01/2000", "10", "Apto");
        }
        // Sem estádios

        Exception ex = assertThrows(IllegalStateException.class, () ->
                controlador.configurarTorneio(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 2));
        assertEquals("SEM_ESTADIOS", ex.getMessage());
    }

    @Test
    void testValidarCalendarioSemConflitos() {
        prepararTorneioValido();
        controlador.configurarTorneio(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 2);

        List<String> conflitos = controlador.validarCalendario();
        assertTrue(conflitos.isEmpty());
    }

    @Test
    void testFluxoCompletoValidarEIniciar() {
        prepararTorneioValido();
        controlador.configurarTorneio(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 2);

        controlador.confirmarValidacao();
        assertEquals(Torneio.Estado.VALIDADO, torneio.getEstado());

        controlador.iniciarTorneio();
        assertEquals(Torneio.Estado.EM_CURSO, torneio.getEstado());
    }

    @Test
    void testErroIniciarSemValidar() {
        prepararTorneioValido();
        controlador.configurarTorneio(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 2);

        Exception ex = assertThrows(IllegalStateException.class, controlador::iniciarTorneio);
        assertEquals("ESTADO_INVALIDO", ex.getMessage());
    }

    @Test
    void testErroValidarCalendarioAntesDeConfigurar() {
        Exception ex = assertThrows(IllegalStateException.class, controlador::validarCalendario);
        assertEquals("ESTADO_INVALIDO", ex.getMessage());
    }
}
