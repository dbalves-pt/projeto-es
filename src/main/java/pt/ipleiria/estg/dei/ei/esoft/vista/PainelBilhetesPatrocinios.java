package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.*;
import pt.ipleiria.estg.dei.ei.esoft.modelo.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static pt.ipleiria.estg.dei.ei.esoft.vista.FormularioEquipa.*;

public class PainelBilhetesPatrocinios extends JPanel {

    private final BilheteControlador bilheteControlador;
    private final PatrocinioControlador patrocinioControlador;
    private final FinanceiroControlador financeiroControlador;
    private final JogoControlador jogoControlador;

    // Componentes de venda
    private JComboBox<Jogo> comboJogo;
    private JComboBox<Bancada> comboBancada;
    private JTextField campFila;
    private JTextField campAssento;
    private JTextField campNomeComprador;
    private JTextField campNifComprador;
    private JLabel lblPreco;
    private DefaultTableModel modelBilhetes;
    private JButton btnVender;

    // Componentes de patrocínios
    private DefaultTableModel modelPatrocinios;
    private JTable tabelaPatrocinios;

    // Componentes financeiros
    private JLabel lblReceitaBilheteira;
    private JLabel lblReceitaPatrocinios;
    private JLabel lblReceitaTotal;

    public PainelBilhetesPatrocinios(BilheteControlador bilheteControlador,
                                     PatrocinioControlador patrocinioControlador,
                                     FinanceiroControlador financeiroControlador,
                                     JogoControlador jogoControlador) {
        this.bilheteControlador = bilheteControlador;
        this.patrocinioControlador = patrocinioControlador;
        this.financeiroControlador = financeiroControlador;
        this.jogoControlador = jogoControlador;

        setBackground(COR_BRANCO);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.setBackground(COR_FUNDO);

        tabs.addTab("Vender Bilhetes", criarCartaoVenda());
        tabs.addTab("Patrocínios", criarCartaoPatrocinios());
        tabs.addTab("Resumo Financeiro", criarCartaoFinanceiro());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel criarCartaoCinza() {
        JPanel cartao = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE0E0E0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));
        cartao.setOpaque(false);
        cartao.setBorder(new EmptyBorder(20, 20, 20, 20));
        return cartao;
    }

    // ── Venda de Bilhetes ──
    private JPanel criarCartaoVenda() {
        JPanel cartao = criarCartaoCinza();
        cartao.setLayout(new GridBagLayout());  // <-- usar GridBagLayout diretamente
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titulo = new JLabel("Venda de Bilhetes");
        titulo.setFont(FONTE_TITULO);
        cartao.add(titulo, gbc);

        // Linha 1: Jogo
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        cartao.add(new JLabel("Jogo:"), gbc);
        gbc.gridx = 1;
        comboJogo = new JComboBox<>();
        comboJogo.addActionListener(e -> atualizarBancadas());
        cartao.add(comboJogo, gbc);

        // Linha 2: Bancada
        gbc.gridy = 2;
        gbc.gridx = 0;
        cartao.add(new JLabel("Bancada:"), gbc);
        gbc.gridx = 1;
        comboBancada = new JComboBox<>();
        comboBancada.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Bancada) {
                    label.setText(((Bancada) value).getNome());
                }
                return label;
            }
        });
        comboBancada.addActionListener(e -> atualizarPreco());
        cartao.add(comboBancada, gbc);

        // Linha 3: Fila e Assento (lado a lado)
        gbc.gridy = 3;
        gbc.gridx = 0;
        cartao.add(new JLabel("Fila:"), gbc);
        gbc.gridx = 1;
        JPanel painelFilaAssento = new JPanel(new GridLayout(1, 2, 10, 0));
        painelFilaAssento.setOpaque(false);
        campFila = new JTextField(5);
        campFila.setFont(FONTE_NORMAL);
        painelFilaAssento.add(campFila);
        painelFilaAssento.add(new JLabel("Assento:"));
        campAssento = new JTextField(5);
        campAssento.setFont(FONTE_NORMAL);
        painelFilaAssento.add(campAssento);
        cartao.add(painelFilaAssento, gbc);

        // Linha 4: Comprador e NIF (lado a lado)
        gbc.gridy = 4;
        gbc.gridx = 0;
        cartao.add(new JLabel("Comprador:"), gbc);
        gbc.gridx = 1;
        JPanel painelComprador = new JPanel(new GridLayout(1, 2, 10, 0));
        painelComprador.setOpaque(false);
        campNomeComprador = new JTextField(12);
        campNomeComprador.setFont(FONTE_NORMAL);
        painelComprador.add(campNomeComprador);
        painelComprador.add(new JLabel("NIF:"));
        campNifComprador = new JTextField(10);
        campNifComprador.setFont(FONTE_NORMAL);
        painelComprador.add(campNifComprador);
        cartao.add(painelComprador, gbc);

        // Linha 5: Preço e Botão Vender
        gbc.gridy = 5;
        gbc.gridx = 0;
        cartao.add(new JLabel("Preço (€):"), gbc);
        gbc.gridx = 1;
        JPanel painelPrecoBotao = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelPrecoBotao.setOpaque(false);
        lblPreco = new JLabel("0.00");
        lblPreco.setFont(FONTE_TITULO);
        painelPrecoBotao.add(lblPreco);
        btnVender = new JButton("Vender");
        btnVender.setBackground(new Color(0x2E7D32));
        btnVender.setForeground(Color.WHITE);
        btnVender.setFocusPainted(false);
        btnVender.addActionListener(e -> venderBilhete());
        painelPrecoBotao.add(btnVender);
        cartao.add(painelPrecoBotao, gbc);

        // Tabela de bilhetes (ocupa mais espaço)
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        String[] colunas = {"ID", "Jogo", "Bancada", "Fila", "Lugar", "Preço", "Estado", "Comprador"};
        modelBilhetes = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tabela = new JTable(modelBilhetes);
        tabela.setRowHeight(25);
        tabela.setShowGrid(false);
        tabela.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabela.getTableHeader().setFont(FONTE_HEADER);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new LineBorder(COR_BORDA, 1));
        scroll.getViewport().setBackground(COR_BRANCO);
        cartao.add(scroll, gbc);

        // Inicializar dados
        carregarJogos();
        atualizarTabelaBilhetes();

        return cartao;
    }

    // ── Patrocínios ──
    private JPanel criarCartaoPatrocinios() {
        JPanel cartao = criarCartaoCinza();
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Gestão de Patrocínios");
        titulo.setFont(FONTE_TITULO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(titulo);
        cartao.add(Box.createVerticalStrut(15));

        String[] colunas = {"Nome", "NIF", "Valor (€)", "Início", "Fim", "Tipo", "Jogos"};
        modelPatrocinios = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabelaPatrocinios = new JTable(modelPatrocinios);
        tabelaPatrocinios.setRowHeight(25);
        tabelaPatrocinios.setShowGrid(false);
        tabelaPatrocinios.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabelaPatrocinios.getTableHeader().setFont(FONTE_HEADER);
        JScrollPane scroll = new JScrollPane(tabelaPatrocinios);
        scroll.setBorder(new LineBorder(COR_BORDA, 1));
        scroll.getViewport().setBackground(COR_BRANCO);
        cartao.add(scroll);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        botoes.setOpaque(false);

        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.addActionListener(e -> abrirFormularioPatrocinio(null));
        botoes.add(btnAdicionar);

        JButton btnEditar = new JButton("Editar");
        btnEditar.addActionListener(e -> {
            int linha = tabelaPatrocinios.getSelectedRow();
            if (linha >= 0) {
                Patrocinio p = patrocinioControlador.getPatrocinios().get(linha);
                abrirFormularioPatrocinio(p);
            }
        });
        botoes.add(btnEditar);

        JButton btnRemover = new JButton("Remover");
        btnRemover.addActionListener(e -> {
            int linha = tabelaPatrocinios.getSelectedRow();
            if (linha >= 0) {
                Patrocinio p = patrocinioControlador.getPatrocinios().get(linha);
                try {
                    patrocinioControlador.removerPatrocinio(p);
                    atualizarTabelaPatrocinios();
                    atualizarFinanceiro();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        botoes.add(btnRemover);

        cartao.add(botoes);
        cartao.add(Box.createVerticalStrut(8));

        atualizarTabelaPatrocinios();
        return cartao;
    }

    // ── Financeiro ──
    private JPanel criarCartaoFinanceiro() {
        JPanel cartao = criarCartaoCinza();
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Resumo Financeiro");
        titulo.setFont(FONTE_TITULO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        cartao.add(titulo);
        cartao.add(Box.createVerticalStrut(20));

        JPanel linhas = new JPanel(new GridBagLayout());
        linhas.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        linhas.add(new JLabel("Receita de Bilheteira:"), gbc);
        gbc.gridx = 1;
        lblReceitaBilheteira = new JLabel("0.00 €");
        lblReceitaBilheteira.setFont(FONTE_TITULO);
        linhas.add(lblReceitaBilheteira, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        linhas.add(new JLabel("Receita de Patrocínios:"), gbc);
        gbc.gridx = 1;
        lblReceitaPatrocinios = new JLabel("0.00 €");
        lblReceitaPatrocinios.setFont(FONTE_TITULO);
        linhas.add(lblReceitaPatrocinios, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        linhas.add(new JLabel("Receita Total:"), gbc);
        gbc.gridx = 1;
        lblReceitaTotal = new JLabel("0.00 €");
        lblReceitaTotal.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblReceitaTotal.setForeground(new Color(0x1565C0));
        linhas.add(lblReceitaTotal, gbc);

        cartao.add(linhas);
        cartao.add(Box.createVerticalStrut(20));

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setAlignmentX(LEFT_ALIGNMENT);
        btnAtualizar.addActionListener(e -> atualizarFinanceiro());
        cartao.add(btnAtualizar);

        atualizarFinanceiro();
        return cartao;
    }

    // ── Métodos auxiliares ──

    private void carregarJogos() {
        comboJogo.removeAllItems();
        for (Jogo j : jogoControlador.getJogos()) {
            if (j.getEstado() == Jogo.Estado.CALENDARIZADO) {
                comboJogo.addItem(j);
            }
        }
        if (comboJogo.getItemCount() > 0) {
            comboJogo.setSelectedIndex(0);
            atualizarBancadas();
        }
    }

    private void atualizarBancadas() {
        Jogo jogo = (Jogo) comboJogo.getSelectedItem();
        comboBancada.removeAllItems();
        if (jogo != null) {
            for (Bancada b : jogo.getEstadio().getBancadas()) {
                comboBancada.addItem(b);
            }
        }
        atualizarPreco();
    }

    private void atualizarPreco() {
        Bancada b = (Bancada) comboBancada.getSelectedItem();
        if (b != null) {
            lblPreco.setText(String.format("%.2f", b.getPreco()));
        } else {
            lblPreco.setText("0.00");
        }
    }

    private void venderBilhete() {
        try {
            Jogo jogo = (Jogo) comboJogo.getSelectedItem();
            Bancada bancada = (Bancada) comboBancada.getSelectedItem();
            int fila = Integer.parseInt(campFila.getText().trim());
            int assento = Integer.parseInt(campAssento.getText().trim());
            String nome = campNomeComprador.getText().trim();
            String nif = campNifComprador.getText().trim();

            if (jogo == null || bancada == null) {
                JOptionPane.showMessageDialog(this, "Selecione jogo e bancada.");
                return;
            }
            if (nome.isBlank() || nif.isBlank()) {
                JOptionPane.showMessageDialog(this, "Preencha os dados do comprador.");
                return;
            }

            Comprador comprador = new Comprador(nome, nif);
            Bilhete bilhete = bilheteControlador.venderBilhete(jogo, bancada, fila, assento, comprador, bancada.getPreco());
            JOptionPane.showMessageDialog(this, "Bilhete vendido com sucesso! ID: " + bilhete.getId());
            atualizarTabelaBilhetes();
            atualizarFinanceiro();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Fila e assento devem ser números.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarTabelaBilhetes() {
        modelBilhetes.setRowCount(0);
        for (Bilhete b : bilheteControlador.getBilhetes()) {
            String estado = b.getEstado().toString();
            String comprador = b.getComprador() != null ? b.getComprador().getNome() : "";
            modelBilhetes.addRow(new Object[]{
                    b.getId(),
                    b.getJogo().getEquipaCasa().getNome() + " vs " + b.getJogo().getEquipaFora().getNome(),
                    b.getBancada().getNome(),
                    b.getFila(),
                    b.getAssento(),
                    String.format("%.2f", b.getPrecoPago()),
                    estado,
                    comprador
            });
        }
    }

    private void abrirFormularioPatrocinio(Patrocinio p) {
        FormularioPatrocinio form = new FormularioPatrocinio(
                SwingUtilities.getWindowAncestor(this),
                patrocinioControlador, p,
                () -> {
                    atualizarTabelaPatrocinios();
                    atualizarFinanceiro();
                });
        form.setVisible(true);
    }

    private void atualizarTabelaPatrocinios() {
        modelPatrocinios.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Patrocinio p : patrocinioControlador.getPatrocinios()) {
            String jogosStr = p.getJogosAssociados().stream()
                    .map(j -> j.getEquipaCasa().getNome() + " vs " + j.getEquipaFora().getNome())
                    .collect(Collectors.joining(", "));
            if (jogosStr.isEmpty()) jogosStr = "Nenhum";
            modelPatrocinios.addRow(new Object[]{
                    p.getNomePatrocinador(),
                    p.getNif(),
                    String.format("%.2f", p.getValor()),
                    p.getDataInicio().format(fmt),
                    p.getDataFim().format(fmt),
                    p.getTipo(),
                    jogosStr
            });
        }
    }

    private void atualizarFinanceiro() {
        lblReceitaBilheteira.setText(String.format("%.2f €", financeiroControlador.getReceitaBilheteira()));
        lblReceitaPatrocinios.setText(String.format("%.2f €", financeiroControlador.getReceitaPatrocinios()));
        lblReceitaTotal.setText(String.format("%.2f €", financeiroControlador.getReceitaTotal()));
    }

    public void atualizar() {
        carregarJogos();
        atualizarTabelaBilhetes();
        atualizarTabelaPatrocinios();
        atualizarFinanceiro();
    }
}