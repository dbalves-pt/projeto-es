package pt.ipleiria.estg.dei.ei.esoft.controlador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import static org.junit.jupiter.api.Assertions.*;

class JogadorControladorTest {

    private JogadorControlador controlador;
    private EquipaControlador controladorEquipa;
    private Equipa equipa;

    @BeforeEach
    void setUp() {
        // Limpa a "base de dados" antes de cada teste para eles não interferirem uns com os outros
        Torneio.resetInstancia();
        controlador = new JogadorControlador();
        controladorEquipa = new EquipaControlador();

        controladorEquipa.adicionarEquipa("Portugal", "Portugal");
        equipa = controladorEquipa.getEquipas().get(0);
    }

    @Test
    void testAdicionarJogadorComSucesso() {
        // Caminho Principal — UC03
        controlador.adicionarJogador(equipa, "Cristiano Ronaldo", "Avançado", "05/02/1985", "7", "Apto");

        assertEquals(1, equipa.getJogadores().size());
        assertEquals("Cristiano Ronaldo", equipa.getJogadores().get(0).getNomeCompleto());
    }

    @Test
    void testErroCampoNomeVazio() {
        // CA 5.1 - Campo obrigatório vazio
        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarJogador(equipa, "", "Avançado", "05/02/1985", "7", "Apto");
        });

        assertEquals("CAMPO_NOME_VAZIO", excecao.getMessage());
    }

    @Test
    void testErroNumeroCamisolaDuplicado() {
        // Número de camisola já existente na mesma equipa
        controlador.adicionarJogador(equipa, "Cristiano Ronaldo", "Avançado", "05/02/1985", "7", "Apto");

        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarJogador(equipa, "Pepe", "Defesa", "26/02/1983", "7", "Apto");
        });

        assertEquals("NUMERO_CAMISOLA_DUPLICADO", excecao.getMessage());
    }

    @Test
    void testEditarJogadorComSucesso() {
        // UC03 - Editar (formulário reutilizado)
        controlador.adicionarJogador(equipa, "Cristiano Ronaldo", "Avançado", "05/02/1985", "7", "Apto");
        Jogador jogador = equipa.getJogadores().get(0);

        controlador.editarJogador(jogador, "CR7", "Médio", "05/02/1985", "10", "Apto");

        assertEquals("CR7", jogador.getNomeCompleto());
        assertEquals(Jogador.Posicao.MEDIO, jogador.getPosicao());
        assertEquals(10, jogador.getNumeroCamisola());
    }

    @Test
    void testMarcarComoInaptoComSucesso() {
        // UC05 - Marcar Jogador como Inapto (Lesão)
        controlador.adicionarJogador(equipa, "Cristiano Ronaldo", "Avançado", "05/02/1985", "7", "Apto");
        Jogador jogador = equipa.getJogadores().get(0);

        controlador.marcarComoInapto(jogador);

        assertEquals(Jogador.Estado.INAPTO, jogador.getEstado());
    }

    @Test
    void testErroLimiteJogadoresAptoExcedido() {
        // Limite de 23 jogadores aptos por equipa
        for (int i = 1; i <= 23; i++) {
            controlador.adicionarJogador(equipa, "Jogador " + i, "Defesa", "01/01/2000", String.valueOf(i), "Apto");
        }

        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarJogador(equipa, "Jogador 24", "Defesa", "01/01/2000", "24", "Apto");
        });

        assertEquals("LIMITE_JOGADORES_APTO_EXCEDIDO", excecao.getMessage());
    }

    @Test
    void testEliminarJogadorComSucesso() {
        // Remoção de jogador da equipa
        controlador.adicionarJogador(equipa, "Cristiano Ronaldo", "Avançado", "05/02/1985", "7", "Apto");
        Jogador jogador = equipa.getJogadores().get(0);

        controlador.eliminarJogador(equipa, jogador);

        assertEquals(0, equipa.getJogadores().size());
    }
}