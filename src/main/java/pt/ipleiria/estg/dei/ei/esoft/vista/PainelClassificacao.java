package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EstatisticaControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.ClassificacaoEquipa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PainelClassificacao extends JPanel {
    private final EstatisticaControlador controlador;
    private JPanel painelGrupos;

    public PainelClassificacao(EstatisticaControlador controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        initComponents();
        carregarClassificacao();
    }

    private void initComponents() {
        painelGrupos = new JPanel();
        painelGrupos.setLayout(new BoxLayout(painelGrupos, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(painelGrupos);
        add(scroll, BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarClassificacao());
        add(btnAtualizar, BorderLayout.SOUTH);
    }

    private void carregarClassificacao() {
        painelGrupos.removeAll();
        Map<String, List<ClassificacaoEquipa>> classificacoes = controlador.getClassificacaoTodosGrupos();
        for (String grupo : classificacoes.keySet()) {
            JPanel grupoPanel = new JPanel(new BorderLayout());
            grupoPanel.setBorder(BorderFactory.createTitledBorder("Grupo " + grupo));
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"P", "Equipa", "J", "V", "E", "D", "GM", "GS", "GD", "PTS"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
            for (ClassificacaoEquipa ce : classificacoes.get(grupo)) {
                model.addRow(new Object[]{
                        ce.getPosicao(),
                        ce.getEquipa().getNome(),
                        ce.getJogos(),
                        ce.getVitorias(),
                        ce.getEmpates(),
                        ce.getDerrotas(),
                        ce.getGolosMarcados(),
                        ce.getGolosSofridos(),
                        ce.getDiferencaGolos(),
                        ce.getPontos()
                });
            }
            JTable table = new JTable(model);
            grupoPanel.add(new JScrollPane(table), BorderLayout.CENTER);
            painelGrupos.add(grupoPanel);
        }
        painelGrupos.revalidate();
        painelGrupos.repaint();
    }
}