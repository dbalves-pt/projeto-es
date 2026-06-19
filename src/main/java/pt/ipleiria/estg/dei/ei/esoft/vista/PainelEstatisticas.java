package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EstatisticaControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelEstatisticas extends JPanel {
    private final EstatisticaControlador controlador;
    private JTable tabelaMarcadores, tabelaAssistencias, tabelaDefesas;
    private DefaultTableModel modelMarcadores, modelAssistencias, modelDefesas;

    public PainelEstatisticas(EstatisticaControlador controlador) {
        this.controlador = controlador;
        setLayout(new GridLayout(1, 3, 10, 10));
        initComponents();
        carregarEstatisticas();
    }

    private void initComponents() {
        // Melhores marcadores
        modelMarcadores = new DefaultTableModel(new Object[]{"Pos", "Jogador", "Golos"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabelaMarcadores = new JTable(modelMarcadores);
        JScrollPane sp1 = new JScrollPane(tabelaMarcadores);
        sp1.setBorder(BorderFactory.createTitledBorder("Melhor Marcador"));

        // Mais assistências
        modelAssistencias = new DefaultTableModel(new Object[]{"Pos", "Jogador", "Assistências"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabelaAssistencias = new JTable(modelAssistencias);
        JScrollPane sp2 = new JScrollPane(tabelaAssistencias);
        sp2.setBorder(BorderFactory.createTitledBorder("Mais Assistências"));

        // Mais defesas
        modelDefesas = new DefaultTableModel(new Object[]{"Pos", "Jogador", "Defesas"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabelaDefesas = new JTable(modelDefesas);
        JScrollPane sp3 = new JScrollPane(tabelaDefesas);
        sp3.setBorder(BorderFactory.createTitledBorder("Mais Defesas"));

        add(sp1);
        add(sp2);
        add(sp3);
    }

    private void carregarEstatisticas() {
        // Marcadores
        modelMarcadores.setRowCount(0);
        List<Jogador> marcadores = controlador.getMelhoresMarcadores();
        int pos = 1;
        for (Jogador j : marcadores) {
            // Para obter o número de golos, seria necessário contar eventos, mas podemos usar um método auxiliar
            // Como não temos contagem, colocamos placeholder
            modelMarcadores.addRow(new Object[]{pos++, j.getNomeCompleto(), "?"});
        }

        // Assistencias
        modelAssistencias.setRowCount(0);
        List<Jogador> assistencias = controlador.getMaisAssistencias();
        pos = 1;
        for (Jogador j : assistencias) {
            modelAssistencias.addRow(new Object[]{pos++, j.getNomeCompleto(), "?"});
        }

        // Defesas
        modelDefesas.setRowCount(0);
        List<Jogador> defesas = controlador.getMaisDefesas();
        pos = 1;
        for (Jogador j : defesas) {
            modelDefesas.addRow(new Object[]{pos++, j.getNomeCompleto(), "?"});
        }
    }
}