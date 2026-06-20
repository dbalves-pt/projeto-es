package pt.ipleiria.estg.dei.ei.esoft.controlador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Bancada;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Estadio;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import static org.junit.jupiter.api.Assertions.*;

class EstadioControladorTest {

    private EstadioControlador controlador;

    @BeforeEach
    void setUp() {
        // Limpa a "base de dados" antes de cada teste para eles não interferirem uns com os outros
        Torneio.resetInstancia();
        controlador = new EstadioControlador();
    }

    @Test
    void testAdicionarEstadioComSucesso() {
        // Caminho Principal — UC06
        controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "65000");

        assertEquals(1, controlador.getEstadios().size());
        assertEquals("Estádio da Luz", controlador.getEstadios().get(0).getNome());
    }

    @Test
    void testErroNomeEstadioDuplicado() {
        // CA 6.1 - Nome duplicado
        controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "65000");

        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarEstadio("Estádio da Luz", "Porto", "Portugal", "50000");
        });

        assertEquals("NOME_DUPLICADO", excecao.getMessage());
    }

    @Test
    void testErroLotacaoInvalida() {
        // Lotação não numérica ou <= 0
        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "abc");
        });

        assertEquals("LOTACAO_INVALIDA", excecao.getMessage());
    }

    @Test
    void testAdicionarBancadaComSucesso() {
        // Caminho Principal — Adicionar Bancada
        controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "65000");
        Estadio estadio = controlador.getEstadios().get(0);

        controlador.adicionarBancada(estadio, "Bancada Norte", "25.50", "VIP", "10", "5000");

        assertEquals(1, estadio.getBancadas().size());
        assertEquals("Bancada Norte", estadio.getBancadas().get(0).getNome());
        assertEquals(5000, estadio.getLotacaoAtual());
    }

    @Test
    void testErroNomeBancadaDuplicado() {
        // Nome de bancada duplicado dentro do mesmo estádio
        controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "65000");
        Estadio estadio = controlador.getEstadios().get(0);
        controlador.adicionarBancada(estadio, "Bancada Norte", "25.50", "VIP", "10", "5000");

        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarBancada(estadio, "Bancada Norte", "15.00", "Geral", "8", "3000");
        });

        assertEquals("NOME_BANCADA_DUPLICADO", excecao.getMessage());
    }

    @Test
    void testErroLotacaoExcedidaAoAdicionarBancada() {
        // CA 4.1 - Lugares pedidos excedem a lotação máxima do estádio
        controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "1000");
        Estadio estadio = controlador.getEstadios().get(0);

        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarBancada(estadio, "Bancada Norte", "25.50", "VIP", "10", "1500");
        });

        assertEquals("LOTACAO_EXCEDIDA", excecao.getMessage());
    }

    @Test
    void testEditarBancadaComSucesso() {
        // Editar bancada existente com novos valores válidos
        controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "65000");
        Estadio estadio = controlador.getEstadios().get(0);
        controlador.adicionarBancada(estadio, "Bancada Norte", "25.50", "VIP", "10", "5000");
        Bancada bancada = estadio.getBancadas().get(0);

        controlador.editarBancada(estadio, bancada, "Bancada Sul", "30.00", "VIP", "12", "6000");

        assertEquals("Bancada Sul", bancada.getNome());
        assertEquals(30.00, bancada.getPreco());
        assertEquals(6000, bancada.getLugares());
    }

    @Test
    void testEliminarEstadioComSucesso() {
        // Remoção de estádio do torneio
        controlador.adicionarEstadio("Estádio da Luz", "Lisboa", "Portugal", "65000");
        Estadio estadio = controlador.getEstadios().get(0);

        controlador.eliminarEstadio(estadio);

        assertEquals(0, controlador.getEstadios().size());
    }
}