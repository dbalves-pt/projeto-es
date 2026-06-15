package pt.ipleiria.estg.dei.ei.esoft;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.vista.JanelaPrincipal;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // 1. MUDAR O ESTILO VISUAL AQUI!
        try {
            // Força o Java a usar o estilo bonito do teu Windows ou Mac
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Inicializa o Controlador
        EquipaControlador equipaControlador = new EquipaControlador();

        // 3. Arranca a interface
        SwingUtilities.invokeLater(() -> {
            JanelaPrincipal janela = new JanelaPrincipal(equipaControlador);
            janela.setVisible(true);
        });
    }
}