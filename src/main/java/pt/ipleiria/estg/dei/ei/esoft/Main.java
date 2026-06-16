package pt.ipleiria.estg.dei.ei.esoft;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.JogadorControlador;
import pt.ipleiria.estg.dei.ei.esoft.vista.JanelaPrincipal;

import javax.swing.*;

/**
 * Classe de entrada principal (Motor de arranque) da aplicação.
 * Configura o Look and Feel do sistema, inicializa os controladores centrais
 * e inicia a interface gráfica principal.
 */
public class Main {
    public static void main(String[] args) {

        // 1. Configurar o estilo visual moderno (Look and Feel) do Sistema Operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // 2. Inicializar APENAS o controlador de equipas (pois a Janela cria o outro)
        EquipaControlador equipaControlador = new EquipaControlador();

        // 3. Executar e apresentar a janela principal
        SwingUtilities.invokeLater(() -> {
            // ATENÇÃO: letra minúscula (variável) e apenas 1 parâmetro!
            JanelaPrincipal janela = new JanelaPrincipal(equipaControlador);
            janela.setVisible(true);
        });
    }
}