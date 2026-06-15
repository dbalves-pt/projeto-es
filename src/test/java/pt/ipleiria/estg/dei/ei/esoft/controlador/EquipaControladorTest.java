package pt.ipleiria.estg.dei.ei.esoft.controlador; // <-- Corrigido aqui!

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import static org.junit.jupiter.api.Assertions.*;

class EquipaControladorTest {

    private EquipaControlador controlador;

    @BeforeEach
    void setUp() {
        // Limpa a "base de dados" antes de cada teste para eles não interferirem uns com os outros
        Torneio.resetInstancia();
        controlador = new EquipaControlador();
    }

    @Test
    void testAdicionarEquipaComSucesso() {
        // Caminho Principal
        controlador.adicionarEquipa("Portugal", "Portugal");
        assertEquals(1, controlador.getEquipas().size());
        assertEquals("Portugal", controlador.getEquipas().get(0).getNome());
    }

    @Test
    void testErroNomeDuplicado() {
        // CA 6.1 - Nome duplicado
        controlador.adicionarEquipa("Portugal", "Portugal");

        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarEquipa("Portugal", "Brasil"); // Tenta adicionar nome repetido
        });

        assertEquals("NOME_DUPLICADO", excecao.getMessage());
    }

    @Test
    void testErroPaisInvalido() {
        // CA 7.1 - País inválido (não está na lista)
        Exception excecao = assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarEquipa("Narnia FC", "Narnia");
        });

        assertEquals("PAIS_INVALIDO", excecao.getMessage());
    }

    @Test
    void testErroCampoVazio() {
        // CA 5.1 - Campo vazio
        assertThrows(IllegalArgumentException.class, () -> {
            controlador.adicionarEquipa("", "Portugal");
        });
    }
}