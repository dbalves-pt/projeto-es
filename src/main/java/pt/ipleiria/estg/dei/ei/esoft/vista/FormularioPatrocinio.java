package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.PatrocinioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FormularioPatrocinio extends JDialog {

    private final PatrocinioControlador ctrl;
    private final Patrocinio patrocinio;
    private final Runnable aoAtualizar;

    private JTextField campNome;
    private JTextField campNif;
    private JTextField campValor;
    private JTextField campDataInicio;
    private JTextField campDataFim;
    private JTextField campTipo;

    private DefaultListModel<Jogo> modelJogos;
    private JList<Jogo> listaJogos;

    public FormularioPatrocinio(Window owner, PatrocinioControlador ctrl,
                                Patrocinio patrocinio, Runnable aoAtualizar) {
        super(owner, patrocinio == null ? "Adicionar Patrocínio" : "Editar Patrocínio",
                ModalityType.APPLICATION_MODAL);
        this.ctrl = ctrl;
        this.patrocinio = patrocinio;
        this.aoAtualizar = aoAtualizar;
        construirUI();
        if (patrocinio != null) preencher();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void construirUI() {
        // Painel raiz com fundo azul
        JPanel raiz = new JPanel();
        raiz.setBackground(new Color(0xAED6F1));
        raiz.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(raiz);

        // Painel branco arredondado
        JPanel cartao = new JPanel();
        cartao.setBackground(Color.WHITE);
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));

        // Título
        JLabel titulo = new JLabel(patrocinio == null ? "Adicionar Patrocínio" : "Editar Patrocínio");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        cartao.add(titulo);
        cartao.add(Box.createVerticalStrut(20));

        // --- Campos com FlowLayout para evitar problemas de layout ---
        cartao.add(criarLinha("Nome:", campNome = criarCampo("Nome")));
        cartao.add(Box.createVerticalStrut(6));
        cartao.add(criarLinha("NIF:", campNif = criarCampo("NIF")));
        cartao.add(Box.createVerticalStrut(6));
        cartao.add(criarLinha("Valor (€):", campValor = criarCampo("Valor")));
        cartao.add(Box.createVerticalStrut(6));
        cartao.add(criarLinha("Data Início (dd/MM/yyyy):", campDataInicio = criarCampo("DataInicio")));
        cartao.add(Box.createVerticalStrut(6));
        cartao.add(criarLinha("Data Fim (dd/MM/yyyy):", campDataFim = criarCampo("DataFim")));
        cartao.add(Box.createVerticalStrut(6));
        cartao.add(criarLinha("Tipo:", campTipo = criarCampo("Tipo")));
        cartao.add(Box.createVerticalStrut(16));

        // --- Secção Jogos Associados ---
        JLabel lblJogos = new JLabel("Jogos Associados (dentro do período):");
        lblJogos.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblJogos.setAlignmentX(Component.CENTER_ALIGNMENT);
        cartao.add(lblJogos);
        cartao.add(Box.createVerticalStrut(6));

        modelJogos = new DefaultListModel<>();
        listaJogos = new JList<>(modelJogos);
        listaJogos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Jogo) {
                    Jogo j = (Jogo) value;
                    label.setText(j.getEquipaCasa().getNome() + " vs " + j.getEquipaFora().getNome()
                            + " (" + j.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ")");
                }
                return label;
            }
        });
        JScrollPane scrollJogos = new JScrollPane(listaJogos);
        scrollJogos.setPreferredSize(new Dimension(380, 80));
        cartao.add(scrollJogos);
        cartao.add(Box.createVerticalStrut(6));

        JPanel botoesJogos = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoesJogos.setOpaque(false);

        JButton btnAdicionarJogo = new JButton("Adicionar Jogo");
        btnAdicionarJogo.addActionListener(e -> adicionarJogo());
        botoesJogos.add(btnAdicionarJogo);

        JButton btnRemoverJogo = new JButton("Remover Jogo");
        btnRemoverJogo.addActionListener(e -> removerJogo());
        botoesJogos.add(btnRemoverJogo);

        cartao.add(botoesJogos);
        cartao.add(Box.createVerticalStrut(16));

        // --- Botão Guardar ---
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnGuardar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGuardar.addActionListener(e -> guardar());
        cartao.add(btnGuardar);

        raiz.add(cartao);
    }

    // Cria um campo de texto com diagnóstico
    private JTextField criarCampo(String nome) {
        JTextField campo = new JTextField(20);
        campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campo.setEditable(true);
        campo.setEnabled(true);
        campo.setFocusable(true);
        campo.setBackground(Color.WHITE);
        campo.setForeground(Color.BLACK);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCCCCC), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));



        return campo;
    }

    // Cria uma linha com rótulo e campo (FlowLayout)
    private JPanel criarLinha(String rotulo, JTextField campo) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha.setOpaque(false);

        JLabel lbl = new JLabel(rotulo);
        lbl.setPreferredSize(new Dimension(160, 25));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        linha.add(lbl);
        linha.add(campo);
        return linha;
    }

    private void preencher() {
        campNome.setText(patrocinio.getNomePatrocinador());
        campNif.setText(patrocinio.getNif());
        campValor.setText(String.valueOf(patrocinio.getValor()));
        campDataInicio.setText(patrocinio.getDataInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        campDataFim.setText(patrocinio.getDataFim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        campTipo.setText(patrocinio.getTipo());
        for (Jogo j : patrocinio.getJogosAssociados()) {
            modelJogos.addElement(j);
        }
    }

    private void adicionarJogo() {
        try {
            LocalDate inicio = LocalDate.parse(campDataInicio.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate fim = LocalDate.parse(campDataFim.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            List<Jogo> todos = ctrl.getJogosDisponiveis();
            List<Jogo> candidatos = new ArrayList<>();
            for (Jogo j : todos) {
                LocalDate dataJogo = j.getDataHora().toLocalDate();
                if ((dataJogo.isEqual(inicio) || dataJogo.isAfter(inicio)) &&
                        (dataJogo.isEqual(fim) || dataJogo.isBefore(fim)) &&
                        !modelJogos.contains(j)) {
                    candidatos.add(j);
                }
            }

            if (candidatos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não há jogos disponíveis no período para associar.");
                return;
            }

            JDialog dialog = new JDialog(this, "Selecionar Jogos", ModalityType.APPLICATION_MODAL);
            dialog.setLayout(new BorderLayout());
            DefaultListModel<Jogo> modelCandidatos = new DefaultListModel<>();
            for (Jogo j : candidatos) modelCandidatos.addElement(j);
            JList<Jogo> listCandidatos = new JList<>(modelCandidatos);
            listCandidatos.setCellRenderer(listaJogos.getCellRenderer());
            JScrollPane scroll = new JScrollPane(listCandidatos);
            scroll.setPreferredSize(new Dimension(400, 200));
            dialog.add(scroll, BorderLayout.CENTER);

            JPanel botoes = new JPanel();
            JButton btnOk = new JButton("OK");
            btnOk.addActionListener(e -> {
                List<Jogo> selecionados = listCandidatos.getSelectedValuesList();
                for (Jogo j : selecionados) {
                    modelJogos.addElement(j);
                }
                dialog.dispose();
            });
            botoes.add(btnOk);
            JButton btnCancel = new JButton("Cancelar");
            btnCancel.addActionListener(e -> dialog.dispose());
            botoes.add(btnCancel);
            dialog.add(botoes, BorderLayout.SOUTH);

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Certifique-se que as datas estão preenchidas corretamente.");
        }
    }

    private void removerJogo() {
        Jogo selecionado = listaJogos.getSelectedValue();
        if (selecionado != null) {
            modelJogos.removeElement(selecionado);
        }
    }

    private void guardar() {
        try {
            String nome = campNome.getText().trim();
            String nif = campNif.getText().trim();
            double valor = Double.parseDouble(campValor.getText().trim());
            LocalDate inicio = LocalDate.parse(campDataInicio.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate fim = LocalDate.parse(campDataFim.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String tipo = campTipo.getText().trim();

            if (patrocinio == null) {
                Patrocinio novo = ctrl.adicionarPatrocinioRetornando(nome, nif, valor, inicio, fim, tipo);
                for (int i = 0; i < modelJogos.size(); i++) {
                    ctrl.associarJogo(novo, modelJogos.get(i));
                }
            } else {
                ctrl.editarPatrocinio(patrocinio, nome, nif, valor, inicio, fim, tipo);
                List<Jogo> atuais = new ArrayList<>(patrocinio.getJogosAssociados());
                for (Jogo j : atuais) ctrl.desassociarJogo(patrocinio, j);
                for (int i = 0; i < modelJogos.size(); i++) {
                    ctrl.associarJogo(patrocinio, modelJogos.get(i));
                }
            }
            if (aoAtualizar != null) aoAtualizar.run();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}