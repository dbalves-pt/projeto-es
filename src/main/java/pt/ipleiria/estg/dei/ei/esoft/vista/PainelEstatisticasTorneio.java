package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EventoControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PainelEstatisticasTorneio extends JPanel {

    private final EventoControlador eventoControlador;

    public PainelEstatisticasTorneio() {
        this.eventoControlador = new EventoControlador();
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new GridLayout(1, 3, 20, 0));
        add(criarTabela("Melhores Marcadores", eventoControlador.getGolosPorJogador()));
        add(criarTabela("Mais Assistências", eventoControlador.getAssistenciasPorJogador()));
        add(criarTabela("Mais Defesas", eventoControlador.getDefesasPorJogador()));
    }

    private JPanel criarTabela(String titulo, Map<Jogador, Integer> dados) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(0xE0E0E0));
        painel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        painel.add(lblTitulo, BorderLayout.NORTH);

        // Ordenar por valor decrescente
        List<Map.Entry<Jogador, Integer>> sorted = dados.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        DefaultTableModel model = new DefaultTableModel(new String[]{"Jogador", "Total"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for (Map.Entry<Jogador, Integer> entry : sorted) {
            model.addRow(new Object[]{entry.getKey().getNomeCompleto(), entry.getValue()});
        }

        JTable tabela = new JTable(model);
        tabela.setRowHeight(25);
        tabela.setShowGrid(false);
        tabela.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    public void atualizar() {
        removeAll();
        add(criarTabela("Melhores Marcadores", eventoControlador.getGolosPorJogador()));
        add(criarTabela("Mais Assistências", eventoControlador.getAssistenciasPorJogador()));
        add(criarTabela("Mais Defesas", eventoControlador.getDefesasPorJogador()));
        revalidate();
        repaint();
    }
}