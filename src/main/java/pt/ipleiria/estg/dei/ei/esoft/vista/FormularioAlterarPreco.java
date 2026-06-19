package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Bancada;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FormularioAlterarPreco extends JDialog {

    private final Jogo jogo;
    private final Runnable aoAtualizar;

    private JComboBox<Bancada> comboBancada;
    private JTextField campNovoPreco;
    private JLabel lblErro;

    public FormularioAlterarPreco(Window owner, Jogo jogo, Runnable aoAtualizar) {
        super(owner, "Alterar Preço da Bancada", ModalityType.APPLICATION_MODAL);
        this.jogo = jogo;
        this.aoAtualizar = aoAtualizar;
        construirUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new GridBagLayout());
        raiz.setBackground(FormularioEquipa.COR_FUNDO);
        raiz.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(raiz);

        JPanel cartao = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));
        cartao.setOpaque(false);
        cartao.setBorder(new EmptyBorder(22, 22, 22, 22));
        cartao.setPreferredSize(new Dimension(320, 220));

        JLabel titulo = new JLabel("Alterar Preço para " + jogo.getEquipaCasa().getNome() + " vs " + jogo.getEquipaFora().getNome());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(titulo);
        cartao.add(Box.createVerticalStrut(15));

        // Bancada
        JPanel linhaBancada = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        linhaBancada.setOpaque(false);
        linhaBancada.setAlignmentX(LEFT_ALIGNMENT);
        linhaBancada.add(new JLabel("Bancada:"));
        comboBancada = new JComboBox<>();
        for (Bancada b : jogo.getEstadio().getBancadas()) {
            comboBancada.addItem(b);
        }
        comboBancada.setFont(FormularioEquipa.FONTE_NORMAL);
        linhaBancada.add(comboBancada);
        cartao.add(linhaBancada);
        cartao.add(Box.createVerticalStrut(8));

        // Novo preço
        JPanel linhaPreco = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        linhaPreco.setOpaque(false);
        linhaPreco.setAlignmentX(LEFT_ALIGNMENT);
        linhaPreco.add(new JLabel("Novo Preço (€):"));
        campNovoPreco = new JTextField(10);
        campNovoPreco.setFont(FormularioEquipa.FONTE_NORMAL);
        linhaPreco.add(campNovoPreco);
        cartao.add(linhaPreco);

        lblErro = new JLabel(" ");
        lblErro.setFont(FormularioEquipa.FONTE_ERRO);
        lblErro.setForeground(FormularioEquipa.COR_ERRO);
        lblErro.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(lblErro);

        cartao.add(Box.createVerticalStrut(18));

        JButton btnConcluido = new JButton("Concluído");
        btnConcluido.setAlignmentX(LEFT_ALIGNMENT);
        btnConcluido.addActionListener(e -> guardar());
        cartao.add(btnConcluido);

        raiz.add(cartao);
    }

    private void guardar() {
        Bancada bancada = (Bancada) comboBancada.getSelectedItem();
        String precoStr = campNovoPreco.getText().trim();

        try {
            if (precoStr.isBlank()) throw new IllegalArgumentException("Preço obrigatório.");
            double preco = Double.parseDouble(precoStr.replace(",", "."));
            if (preco <= 0) throw new IllegalArgumentException("Preço deve ser positivo.");

            jogo.definirPrecoEspecial(bancada, preco);
            if (aoAtualizar != null) aoAtualizar.run();
            dispose();

        } catch (NumberFormatException ex) {
            lblErro.setText("Introduza um número válido.");
        } catch (IllegalArgumentException ex) {
            lblErro.setText(ex.getMessage());
        }
    }
}