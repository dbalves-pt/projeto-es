package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EstadioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Bancada;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Estadio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PainelEstadios extends JPanel {

    private final EstadioControlador ctrlEstadio;
    private DefaultListModel<Estadio> listModelEstadios;
    private JList<Estadio> listaEstadios;

    private JLabel lblNomeEstadioDireita;
    private JTable tabelaBancadas;
    private DefaultTableModel tableModelBancadas;
    private Estadio estadioSelecionadoAtual = null;

    private static final Color COR_FUNDO_CINZA = new Color(0xE0E0E0);
    private static final Color COR_BRANCO      = Color.WHITE;

    public PainelEstadios(EstadioControlador ctrlEstadio) {
        this.ctrlEstadio = ctrlEstadio;
        construirUI();
    }

    private void construirUI() {
        setBackground(COR_BRANCO);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Coluna Esquerda (Estádios)
        gbc.gridx = 0; gbc.weightx = 0.3; gbc.insets = new Insets(0, 0, 0, 20);
        add(criarColunaEsquerda(), gbc);

        // Coluna Direita (Bancadas)
        gbc.gridx = 1; gbc.weightx = 0.7; gbc.insets = new Insets(0, 20, 0, 0);
        add(criarColunaDireita(), gbc);

        atualizarListaEstadios();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAINEL ESQUERDO: Lista de Estádios
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarColunaEsquerda() {
        JPanel painel = criarCartaoCinza();

        JLabel lblTitulo = new JLabel("Estádios");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(15));

        listModelEstadios = new DefaultListModel<>();
        listaEstadios = new JList<>(listModelEstadios);
        listaEstadios.setFixedCellHeight(30);

        listaEstadios.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Estadio) {
                    label.setText("  " + ((Estadio) value).getNome());
                }
                return label;
            }
        });

        listaEstadios.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarDetalhesEstadio(listaEstadios.getSelectedValue());
            }
        });

        JScrollPane scroll = new JScrollPane(listaEstadios);
        scroll.setBorder(null);
        painel.add(scroll);

        JButton btnInserir = criarBotaoLink("Inserir Estádio...");
        btnInserir.addActionListener(e -> {
            FormularioEstadio form = new FormularioEstadio(
                    SwingUtilities.getWindowAncestor(this),
                    ctrlEstadio,
                    this::atualizarListaEstadios
            );
            form.setVisible(true);
        });
        painel.add(btnInserir);

        JButton btnEliminar = criarBotaoLink("Eliminar Estádio...");
        btnEliminar.addActionListener(e -> {
            if (estadioSelecionadoAtual != null) {
                ctrlEstadio.eliminarEstadio(estadioSelecionadoAtual);
                estadioSelecionadoAtual = null;
                atualizarListaEstadios();
                atualizarDetalhesEstadio(null);
            }
        });
        painel.add(btnEliminar);

        return painel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAINEL DIREITO: Detalhes e Tabela de Bancadas
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarColunaDireita() {
        JPanel painel = criarCartaoCinza();

        lblNomeEstadioDireita = new JLabel("Selecione um estádio...");
        lblNomeEstadioDireita.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNomeEstadioDireita.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblNomeEstadioDireita);
        painel.add(Box.createVerticalStrut(15));

        JLabel lblBancadas = new JLabel("Bancadas");
        lblBancadas.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblBancadas.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblBancadas);
        painel.add(Box.createVerticalStrut(5));

        String[] colunas = {"ID", "Nome", "Preço", "Categoria", "Lugares"};

        // ── 1. BLOQUEIA A EDIÇÃO DIRETA NAS CÉLULAS DA TABELA ──
        tableModelBancadas = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaBancadas = new JTable(tableModelBancadas);
        tabelaBancadas.setRowHeight(25);
        tabelaBancadas.setShowGrid(false);
        tabelaBancadas.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabelaBancadas.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // ── 2. EVENTO: DUPLO CLIQUE PARA ABRIR O FORMULÁRIO DE EDIÇÃO ──
        tabelaBancadas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaBancadas.getSelectedRow() != -1 && estadioSelecionadoAtual != null) {
                    int linha = tabelaBancadas.getSelectedRow();
                    Bancada bancadaParaEditar = estadioSelecionadoAtual.getBancadas().get(linha);

                    FormularioBancada form = new FormularioBancada(
                            SwingUtilities.getWindowAncestor(PainelEstadios.this),
                            ctrlEstadio,
                            estadioSelecionadoAtual,
                            bancadaParaEditar, // Passa a bancada selecionada!
                            () -> atualizarDetalhesEstadio(estadioSelecionadoAtual)
                    );
                    form.setVisible(true);
                }
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabelaBancadas);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        scrollTabela.getViewport().setBackground(COR_BRANCO);
        scrollTabela.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(scrollTabela);
        painel.add(Box.createVerticalStrut(10));

        JButton btnInserirBancada = criarBotaoLink("Inserir Bancada...");
        btnInserirBancada.addActionListener(e -> {
            if (estadioSelecionadoAtual != null) {
                FormularioBancada form = new FormularioBancada(
                        SwingUtilities.getWindowAncestor(this),
                        ctrlEstadio,
                        estadioSelecionadoAtual,
                        null, // NULL significa que estamos no modo Inserir
                        () -> atualizarDetalhesEstadio(estadioSelecionadoAtual)
                );
                form.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um estádio primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        painel.add(btnInserirBancada);

        JButton btnEliminarBancada = criarBotaoLink("Eliminar Bancada...");
        btnEliminarBancada.addActionListener(e -> {
            int linha = tabelaBancadas.getSelectedRow();
            if (linha != -1 && estadioSelecionadoAtual != null) {
                Bancada b = estadioSelecionadoAtual.getBancadas().get(linha);
                estadioSelecionadoAtual.removerBancada(b);
                atualizarDetalhesEstadio(estadioSelecionadoAtual);
            }
        });
        painel.add(btnEliminarBancada);

        return painel;
    }

    private void atualizarListaEstadios() {
        listModelEstadios.clear();
        for (Estadio est : ctrlEstadio.getEstadios()) {
            listModelEstadios.addElement(est);
        }
    }

    private void atualizarDetalhesEstadio(Estadio est) {
        estadioSelecionadoAtual = est;
        tableModelBancadas.setRowCount(0);

        if (est == null) {
            lblNomeEstadioDireita.setText("Selecione um estádio...");
        } else {
            lblNomeEstadioDireita.setText(est.getNome() + " (" + est.getCidade() + ")");

            int id = 1;
            for (Bancada b : est.getBancadas()) {
                tableModelBancadas.addRow(new Object[]{
                        id++,
                        b.getNome(),
                        String.format("%.2f€", b.getPreco()),
                        b.getCategoria(),
                        b.getLugares()
                });
            }
        }
    }

    private JPanel criarCartaoCinza() {
        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_FUNDO_CINZA);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return painel;
    }

    private JButton criarBotaoLink(String texto) {
        JButton btn = new JButton(texto);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(5, 0, 5, 0));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}