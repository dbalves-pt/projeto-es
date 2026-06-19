package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.BilheteControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Bilhete;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelBilhetes extends JPanel {
    private final BilheteControlador controlador;
    private JTable table;
    private DefaultTableModel model;
    private Jogo jogoSelecionado;

    public PainelBilhetes(BilheteControlador controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        initComponents();
        carregarJogos();
    }

    private void initComponents() {
        model = new DefaultTableModel(new Object[]{"ID", "Equipa A", "Equipa B", "Data", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) model.getValueAt(row, 0);
                jogoSelecionado = controlador.listarTodosJogos().stream()
                        .filter(j -> j.getId().equals(id))
                        .findFirst().orElse(null);
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnComprar = new JButton("Comprar Bilhete");
        btnComprar.addActionListener(e -> comprarBilhete());
        btnPanel.add(btnComprar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void carregarJogos() {
        model.setRowCount(0);
        for (Jogo j : controlador.listarTodosJogos()) {
            model.addRow(new Object[]{
                    j.getId(),
                    j.getEquipaA().getNome(),
                    j.getEquipaB().getNome(),
                    j.getDataHora(),
                    j.getEstado()
            });
        }
    }

    private void comprarBilhete() {
        if (jogoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um jogo.");
            return;
        }
        // Simular escolha de lugar (bancada e lugar)
        String lugarId = JOptionPane.showInputDialog(this, "Digite o identificador do lugar (ex: Norte-F1-L12):");
        if (lugarId == null || lugarId.isBlank()) return;

        try {
            Bilhete bilhete = controlador.reservarLugar(jogoSelecionado, lugarId);
            // Pedir dados do comprador
            JTextField nome = new JTextField();
            JTextField nif = new JTextField();
            JTextField contacto = new JTextField();
            Object[] msg = {"Nome Completo:", nome, "NIF:", nif, "Contacto:", contacto};
            int op = JOptionPane.showConfirmDialog(this, msg, "Dados do Comprador", JOptionPane.OK_CANCEL_OPTION);
            if (op == JOptionPane.OK_OPTION) {
                controlador.confirmarCompra(jogoSelecionado, bilhete, nome.getText(), nif.getText(), contacto.getText());
                JOptionPane.showMessageDialog(this, "Bilhete comprado! Código: " + bilhete.getId());
                carregarJogos();
            } else {
                controlador.cancelarReserva(jogoSelecionado, bilhete);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}