package pt.ipleiria.estg.dei.ei.esoft.controlador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.modelo.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EventoControladorTest {

    private Torneio torneio;
    private JogoControlador jogoControlador;
    private EventoControlador eventoControlador;
    private Jogo jogo;
    private Equipa casa;
    private Equipa fora;
    private Jogador jogadorCasa;
    private Jogador jogadorFora;

    @BeforeEach
    void setUp() {
        Torneio.resetInstancia();
        torneio = Torneio.getInstancia();
        jogoControlador = new JogoControlador(torneio);
        eventoControlador = new EventoControlador();

        EquipaControlador equipaControlador = new EquipaControlador(torneio);
        JogadorControlador jogadorControlador = new JogadorControlador(torneio);
        EstadioControlador estadioControlador = new EstadioControlador(torneio);
        TorneioControlador torneioControlador = new TorneioControlador(torneio);

        for (String nome : new String[]{"Portugal", "Espanha", "França", "Brasil"}) {
            equipaControlador.adicionarEquipa(nome, nome.equals("Brasil") ? "Brasil" : nome);
        }
        for (Equipa eq : torneio.getEquipas()) {
            jogadorControlador.adicionarJogador(eq, "Jogador de " + eq.getNome(),
                    "Avançado", "01/01/2000", "10", "Apto");
        }
        estadioControlador.adicionarEstadio("Estádio Nacional", "Lisboa", "Portugal", "50000");
        torneioControlador.configurarTorneio(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 30), 1);

        jogo = torneio.getJogos().get(0);
        casa = jogo.getEquipaCasa();
        fora = jogo.getEquipaFora();
        jogadorCasa = casa.getJogadores().get(0);
        jogadorFora = fora.getJogadores().get(0);

        jogoControlador.iniciarJogo(jogo);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC10 — Registar Evento de Jogo
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void testRegistarGoloComSucessoAtualizaMarcador() {
        eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "23");

        assertEquals(1, jogo.getEventos().size());
        assertEquals(1, jogo.getGolosCasa());
        assertEquals(0, jogo.getGolosFora());
    }

    @Test
    void testErroCampoTipoVazio() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                eventoControlador.registarEvento(jogo, "", casa, jogadorCasa, "23"));
        assertEquals("CAMPO_TIPO_VAZIO", ex.getMessage());
    }

    @Test
    void testErroMinutoInvalido() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "200"));
        assertEquals("MINUTO_INVALIDO", ex.getMessage());
    }

    @Test
    void testErroJogadorNaoPertenceEquipa() {
        // jogadorFora pertence à equipa "fora", mas estamos a tentar registar como sendo da "casa"
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                eventoControlador.registarEvento(jogo, "Golo", casa, jogadorFora, "10"));
        assertEquals("JOGADOR_NAO_PERTENCE_EQUIPA", ex.getMessage());
    }

    @Test
    void testErroJogadorInapto() {
        jogadorCasa.setEstado(Jogador.Estado.INAPTO);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "10"));
        assertEquals("JOGADOR_INAPTO", ex.getMessage());
    }

    @Test
    void testErroJogadorJaExpulso() {
        eventoControlador.registarEvento(jogo, "Cartão Vermelho", casa, jogadorCasa, "30");

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "40"));
        assertEquals("JOGADOR_JA_EXPULSO", ex.getMessage());
    }

    @Test
    void testErroJogoNaoComecado() {
        Jogo outroJogo = torneio.getJogos().get(1); // ainda CALENDARIZADO
        Equipa outraCasa = outroJogo.getEquipaCasa();
        Jogador outroJogador = outraCasa.getJogadores().get(0);

        Exception ex = assertThrows(IllegalStateException.class, () ->
                eventoControlador.registarEvento(outroJogo, "Golo", outraCasa, outroJogador, "10"));
        assertEquals("JOGO_NAO_COMECADO", ex.getMessage());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC12 — Corrigir Evento de Jogo
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void testCorrigirEventoComSucesso() {
        eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "23");
        EventoJogo evento = jogo.getEventos().get(0);

        eventoControlador.corrigirEvento(jogo, evento, "Golo", casa, jogadorCasa, "25");

        assertEquals(25, evento.getMinuto());
        assertTrue(evento.isCorrigido());
    }

    @Test
    void testCorrigirEventoAlteraMarcador() {
        eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "23");
        EventoJogo evento = jogo.getEventos().get(0);

        // Corrige para a equipa visitante marcar em vez da equipa da casa
        eventoControlador.corrigirEvento(jogo, evento, "Golo", fora, jogadorFora, "23");

        assertEquals(0, jogo.getGolosCasa());
        assertEquals(1, jogo.getGolosFora());
    }

    @Test
    void testPodeCorrigirDentroDoPrazo() {
        eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "23");
        EventoJogo evento = jogo.getEventos().get(0);

        assertTrue(eventoControlador.podeCorrigir(evento));
    }

    @Test
    void testErroCorrigirEventoForaDoPrazo() {
        eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "23");
        EventoJogo evento = jogo.getEventos().get(0);

        // Simula que o evento foi registado há mais tempo do que o prazo permitido (24h)
        evento.forcarRegistadoEmParaTeste(
                java.time.LocalDateTime.now().minusHours(EventoControlador.PRAZO_CORRECAO_HORAS + 1));

        assertFalse(eventoControlador.podeCorrigir(evento));

        Exception ex = assertThrows(IllegalStateException.class, () ->
                eventoControlador.corrigirEvento(jogo, evento, "Golo", casa, jogadorCasa, "30"));
        assertEquals("PRAZO_EXPIRADO", ex.getMessage());
    }

    @Test
    void testErroCorrigirComCamposInvalidos() {
        eventoControlador.registarEvento(jogo, "Golo", casa, jogadorCasa, "23");
        EventoJogo evento = jogo.getEventos().get(0);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                eventoControlador.corrigirEvento(jogo, evento, "Golo", casa, jogadorCasa, "999"));
        assertEquals("MINUTO_INVALIDO", ex.getMessage());
    }
}
