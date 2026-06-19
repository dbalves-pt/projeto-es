package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.BilheteControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Bancada;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PainelDescontos extends JPanel {
    private final BilheteControlador controlador;
    private JComboBox<Jogo> jogoCombo;
    private JComboBox<String> bancadaCombo;
    private JTextField novoPrecoField;

    public PainelDescontos(BilheteControlador controlador) {
        this.controlador = controlador;
        setLayout(new GridBagLayout());
        initComponents();
        carregarJogos();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridy = y++;
        gbc.gridx = 0;
        add(new JLabel("Jogo:"), gbc);
        gbc.gridx = 1;
        jogoCombo = new JComboBox<>();
        jogoCombo.addActionListener(e -> carregarBancadas());
        add(jogoCombo, gbc);

        gbc.gridy = y++;
        gbc.gridx = 0;
        add(new JLabel("Bancada:"), gbc);
        gbc.gridx = 1;
        bancadaCombo = new JComboBox<>();
        add(bancadaCombo, gbc);

        gbc.gridy = y++;
        gbc.gridx = 0;
        add(new JLabel("Novo Preço (€):"), gbc);
        gbc.gridx = 1;
        novoPrecoField = new JTextField(10);
        add(novoPrecoField, gbc);

        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnAplicar = new JButton("Aplicar Desconto");
        btnAplicar.addActionListener(e -> aplicarDesconto());
        add(btnAplicar, gbc);
    }

    private void carregarJogos() {
        jogoCombo.removeAllItems();
        for (Jogo j : controlador.listarTodosJogos()) {
            jogoCombo.addItem(j);
        }
        if (jogoCombo.getItemCount() > 0) carregarBancadas();
    }

    private void carregarBancadas() {
        bancadaCombo.removeAllItems();
        Jogo j = (Jogo) jogoCombo.getSelectedItem();
        if (j == null) return;
        for (Bancada b : j.getEstadio().getBancadas()) {
            bancadaCombo.addItem(b.getNome());
        }
    }

    private void aplicarDesconto() {
        Jogo jogo = (Jogo) jogoCombo.getSelectedItem();
        if (jogo == null) return;
        String nomeBancada = (String) bancadaCombo.getSelectedItem();
        if (nomeBancada == null) return;
        try {
            double novoPreco = Double.parseDouble(novoPrecoField.getText().trim());
            controlador.aplicarDesconto(jogo, nomeBancada, novoPreco);
            JOptionPane.showMessageDialog(this, "Preço atualizado!");
            novoPrecoField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}