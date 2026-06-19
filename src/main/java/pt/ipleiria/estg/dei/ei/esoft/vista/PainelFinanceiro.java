package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.BilheteControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.PatrocinioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import java.awt.*;

public class PainelFinanceiro extends JPanel {
    private final BilheteControlador bilheteControlador;
    private final PatrocinioControlador patrocinioControlador;
    private JLabel totalBilheteira, totalPatrocinios;

    public PainelFinanceiro(BilheteControlador bilheteControlador,
                            PatrocinioControlador patrocinioControlador) {
        this.bilheteControlador = bilheteControlador;
        this.patrocinioControlador = patrocinioControlador;
        setLayout(new BorderLayout());
        initComponents();
        atualizarValores();
    }

    private void initComponents() {
        JPanel painelDados = new JPanel(new GridLayout(3, 1, 10, 10));
        painelDados.setBorder(BorderFactory.createTitledBorder("Resumo Financeiro"));

        totalBilheteira = new JLabel("Total Bilheteira: 0€");
        totalPatrocinios = new JLabel("Total Patrocínios: 0€");
        JLabel totalGeral = new JLabel("Total Geral: 0€");

        painelDados.add(totalBilheteira);
        painelDados.add(totalPatrocinios);
        painelDados.add(totalGeral);

        add(painelDados, BorderLayout.NORTH);

        // Tabela de receita por jogo
        JTextArea area = new JTextArea(10, 40);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        add(scroll, BorderLayout.CENTER);

        // Botão atualizar
        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> {
            atualizarValores();
            StringBuilder sb = new StringBuilder("Receita por Jogo:\n");
            for (Jogo j : bilheteControlador.listarTodosJogos()) {
                sb.append(j.getEquipaA().getNome()).append(" x ")
                        .append(j.getEquipaB().getNome())
                        .append(": ").append(bilheteControlador.getReceitaPorJogo(j)).append("€\n");
            }
            area.setText(sb.toString());
        });
        add(btnAtualizar, BorderLayout.SOUTH);
    }

    private void atualizarValores() {
        double totalBil = bilheteControlador.getTotalBilheteira();
        double totalPat = patrocinioControlador.getTotalPatrocinios();
        totalBilheteira.setText("Total Bilheteira: " + totalBil + "€");
        totalPatrocinios.setText("Total Patrocínios: " + totalPat + "€");
        // O total geral seria a soma, mas pode ser separado
    }
}