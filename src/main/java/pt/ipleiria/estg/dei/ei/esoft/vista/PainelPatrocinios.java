package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.PatrocinioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PainelPatrocinios extends JPanel {
    private final PatrocinioControlador controlador;
    private JTable table;
    private DefaultTableModel model;

    public PainelPatrocinios(PatrocinioControlador controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        initComponents();
        carregarPatrocinios();
    }

    private void initComponents() {
        model = new DefaultTableModel(new Object[]{"Empresa", "Tipo", "Valor (€)", "Jogos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdicionar = new JButton("Inserir Patrocínio");
        JButton btnEliminar = new JButton("Eliminar");
        btnAdicionar.addActionListener(e -> adicionarPatrocinio());
        btnEliminar.addActionListener(e -> eliminarPatrocinio());
        btnPanel.add(btnAdicionar);
        btnPanel.add(btnEliminar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void carregarPatrocinios() {
        model.setRowCount(0);
        for (Patrocinio p : controlador.listarPatrocinios()) {
            String jogosStr = p.getJogos().stream()
                    .map(j -> j.getEquipaA().getNome() + "x" + j.getEquipaB().getNome())
                    .reduce((a, b) -> a + ", " + b).orElse("");
            model.addRow(new Object[]{p.getNomeEmpresa(), p.getTipo(), p.getValor(), jogosStr});
        }
    }

    private void adicionarPatrocinio() {
        JTextField nome = new JTextField();
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Principal", "Oficial", "Fornecedor"});
        JTextField valor = new JTextField();
        // Seleção múltipla de jogos
        List<Jogo> jogosDisponiveis = controlador.listarTodosJogos();
        JList<Jogo> jogoList = new JList<>(jogosDisponiveis.toArray(new Jogo[0]));
        jogoList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scroll = new JScrollPane(jogoList);
        scroll.setPreferredSize(new Dimension(200, 100));

        Object[] msg = {"Nome Empresa:", nome, "Tipo:", tipo, "Valor (€):", valor, "Selecione Jogos:", scroll};
        int op = JOptionPane.showConfirmDialog(this, msg, "Inserir Patrocínio", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            try {
                List<Jogo> selecionados = jogoList.getSelectedValuesList();
                if (selecionados.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Selecione pelo menos um jogo.");
                    return;
                }
                controlador.adicionarPatrocinio(nome.getText(), (String) tipo.getSelectedItem(),
                        Double.parseDouble(valor.getText()), selecionados);
                carregarPatrocinios();
                JOptionPane.showMessageDialog(this, "Patrocínio adicionado!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        }
    }

    private void eliminarPatrocinio() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um patrocínio.");
            return;
        }
        String nome = (String) model.getValueAt(row, 0);
        Patrocinio p = controlador.listarPatrocinios().stream()
                .filter(pat -> pat.getNomeEmpresa().equals(nome))
                .findFirst().orElse(null);
        if (p == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Remover " + p.getNomeEmpresa() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controlador.removerPatrocinio(p);
                carregarPatrocinios();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        }
    }
}