package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EstadioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Estadio;
import pt.ipleiria.estg.dei.ei.esoft.modelo.PaisCidade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FormularioEstadio extends JDialog {

    static final Color COR_FUNDO       = new Color(0xAED6F1);
    static final Color COR_BRANCO      = Color.WHITE;
    static final Color COR_BORDA       = new Color(0xCCCCCC);
    static final Color COR_TEXTO       = new Color(0x212121);
    static final Color COR_PLACEHOLDER = new Color(0x9E9E9E);
    static final Color COR_ERRO        = new Color(0xE53935);
    static final Color COR_FOCO        = new Color(0x1976D2);

    private JTextField        campNome;
    private JTextField        campLotacao;
    private JComboBox<String> comboPais;
    private JComboBox<String> comboCidade;

    private JLabel lblErroNome;
    private JLabel lblErroLotacao;
    private JLabel lblErroPais;
    private JLabel lblErroCidade;

    private final EstadioControlador ctrlEstadio;
    private final Runnable           aoAtualizar;

    public FormularioEstadio(Window owner, EstadioControlador ctrlEstadio, Runnable aoAtualizar) {
        super(owner, "Inserir Estádio", ModalityType.APPLICATION_MODAL);
        this.ctrlEstadio = ctrlEstadio;
        this.aoAtualizar = aoAtualizar;

        construirUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new GridBagLayout());
        raiz.setBackground(COR_FUNDO);
        raiz.setBorder(new EmptyBorder(24, 24, 24, 24));
        setContentPane(raiz);

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(320, 520));
        p.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel titulo = new JLabel("Inserir Estádio");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(titulo);
        p.add(Box.createVerticalStrut(15));

        // ── Nome ──────────────────────────────────────────────────────────────
        campNome = criarTextField("Nome do Estádio...");
        p.add(campNome);
        lblErroNome = criarLabelErro();
        p.add(lblErroNome);
        p.add(Box.createVerticalStrut(8));

        // ── Dropdown País ─────────────────────────────────────────────────────
        String[] paises = PaisCidade.getPaises();
        String[] opcoesPais = new String[paises.length + 1];
        opcoesPais[0] = "País...";
        System.arraycopy(paises, 0, opcoesPais, 1, paises.length);

        comboPais = new JComboBox<>(opcoesPais);
        estilizarCombo(comboPais);
        p.add(comboPais);
        lblErroPais = criarLabelErro();
        p.add(lblErroPais);
        p.add(Box.createVerticalStrut(8));

        // ── Dropdown Cidade (DEPENDENTE do País) ─────────────────────────────
        comboCidade = new JComboBox<>(new String[]{"Selecione primeiro o país..."});
        estilizarCombo(comboCidade);
        comboCidade.setEnabled(false);   // só liga quando há país seleccionado
        p.add(comboCidade);
        lblErroCidade = criarLabelErro();
        p.add(lblErroCidade);
        p.add(Box.createVerticalStrut(8));

        // Listener: ao mudar o país, repopula as cidades
        comboPais.addActionListener(e -> atualizarCidadesParaPaisSelecionado());

        // ── Lotação ───────────────────────────────────────────────────────────
        campLotacao = criarTextField("Lotação Máxima...");
        p.add(campLotacao);
        lblErroLotacao = criarLabelErro();
        p.add(lblErroLotacao);
        p.add(Box.createVerticalStrut(15));

        // ── Secção Bancadas (Design Figma) ──────────────────────────────────
        p.add(criarSecaoBancadas());
        p.add(Box.createVerticalStrut(20));

        // ── Botão Concluído ───────────────────────────────────────────────────
        JButton btnConcluido = criarBotao("Concluído");
        btnConcluido.addActionListener(this::guardar);
        p.add(btnConcluido);

        raiz.add(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Dropdown Cidade dependente do País
    // ══════════════════════════════════════════════════════════════════════════

    private void atualizarCidadesParaPaisSelecionado() {
        String paisSelecionado = comboPais.getSelectedIndex() == 0
                ? null
                : (String) comboPais.getSelectedItem();

        comboCidade.removeAllItems();

        if (paisSelecionado == null) {
            comboCidade.addItem("Selecione primeiro o país...");
            comboCidade.setEnabled(false);
            return;
        }

        String[] cidades = PaisCidade.getCidades(paisSelecionado);

        if (cidades.length == 0) {
            // País sem cidades pré-definidas no sistema — permite escrita manual
            comboCidade.setEditable(true);
            comboCidade.addItem("Escreva a cidade...");
            comboCidade.setEnabled(true);
            return;
        }

        comboCidade.setEditable(false);
        comboCidade.addItem("Cidade...");
        for (String cidade : cidades) {
            comboCidade.addItem(cidade);
        }
        comboCidade.setEnabled(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Secção Bancadas — agora com botão "Editar Preço..."
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarSecaoBancadas() {
        JPanel secao = new JPanel();
        secao.setLayout(new BoxLayout(secao, BoxLayout.Y_AXIS));
        secao.setAlignmentX(LEFT_ALIGNMENT);
        secao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x9E9E9E));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        header.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblBancadas = new JLabel("  Bancadas");
        lblBancadas.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblBancadas.setForeground(COR_TEXTO);
        header.add(lblBancadas, BorderLayout.WEST);
        secao.add(header);

        JPanel areaLista = new JPanel();
        areaLista.setLayout(new BoxLayout(areaLista, BoxLayout.Y_AXIS));
        areaLista.setBackground(new Color(0xF5F5F5));
        areaLista.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblInserirBancada = new JLabel("  Inserir Bancada...");
        lblInserirBancada.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblInserirBancada.setForeground(COR_PLACEHOLDER);
        lblInserirBancada.setBorder(new EmptyBorder(5, 0, 5, 0));
        lblInserirBancada.setAlignmentX(LEFT_ALIGNMENT);
        lblInserirBancada.setToolTipText(
                "Crie primeiro o estádio na base de dados para poder inserir bancadas.");
        areaLista.add(lblInserirBancada);

        // ── NOVO: Botão "Editar Preço..." ──────────────────────────────────────
        JButton btnEditarPreco = criarBotaoSecundario("Editar Preço...");
        btnEditarPreco.setAlignmentX(LEFT_ALIGNMENT);
        btnEditarPreco.setToolTipText(
                "Disponível após a bancada ser criada — abre o ecrã 'Alterar Preço' (UC14).");
        // A ligação real ao ecrã "Alterar Preço" (UC14) é feita quando há uma
        // bancada seleccionada — deixamos o botão pronto para o controlador
        // correspondente ser ligado nessa iteração.
        areaLista.add(Box.createVerticalStrut(6));
        areaLista.add(btnEditarPreco);

        secao.add(areaLista);
        return secao;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Guardar — com validação visual específica por campo
    // ══════════════════════════════════════════════════════════════════════════

    private void guardar(ActionEvent e) {
        limparErros();

        String nome    = textoReal(campNome, "Nome do Estádio...");
        String lotacao = textoReal(campLotacao, "Lotação Máxima...");
        String pais    = comboPais.getSelectedIndex() == 0
                ? "" : (String) comboPais.getSelectedItem();
        String cidade  = (!comboCidade.isEnabled() || comboCidade.getSelectedIndex() <= 0)
                ? "" : (String) comboCidade.getSelectedItem();

        try {
            ctrlEstadio.adicionarEstadio(nome, cidade, pais, lotacao);
            if (aoAtualizar != null) aoAtualizar.run();
            dispose();

        } catch (IllegalArgumentException ex) {
            tratarErro(ex.getMessage());
        }
    }

    /**
     * Traduz o código de erro do controlador para feedback visual específico:
     * pinta o campo certo a vermelho e mostra a mensagem junto a ele,
     * em vez de um alerta genérico.
     */
    private void tratarErro(String codigo) {
        switch (codigo) {
            case "CAMPOS_VAZIOS" -> {
                if (textoReal(campNome, "Nome do Estádio...").isBlank())
                    marcarErro(campNome, lblErroNome, "O nome é obrigatório.");
                if (comboPais.getSelectedIndex() == 0)
                    marcarErroCombo(comboPais, lblErroPais, "Selecione um país.");
                if (!comboCidade.isEnabled() || comboCidade.getSelectedIndex() <= 0)
                    marcarErroCombo(comboCidade, lblErroCidade, "Selecione/indique a cidade.");
                if (textoReal(campLotacao, "Lotação Máxima...").isBlank())
                    marcarErro(campLotacao, lblErroLotacao, "A lotação é obrigatória.");
            }
            case "NOME_DUPLICADO" ->
                    marcarErro(campNome, lblErroNome, "Já existe um estádio com este nome.");
            case "LOTACAO_INVALIDA" ->
                // CA 4.1 (no contexto do estádio): valor inválido/insuficiente —
                // pinta exactamente a caixa da lotação, não um alerta genérico.
                    marcarErro(campLotacao, lblErroLotacao,
                            "Introduza um valor numérico positivo.");
            default ->
                    JOptionPane.showMessageDialog(this, codigo, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void marcarErro(JTextField campo, JLabel lblErro, String mensagem) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_ERRO, 2, true), new EmptyBorder(6, 10, 6, 10)));
        lblErro.setText(mensagem);
    }

    private void marcarErroCombo(JComboBox<?> combo, JLabel lblErro, String mensagem) {
        combo.setBorder(new LineBorder(COR_ERRO, 2, true));
        lblErro.setText(mensagem);
    }

    private void limparErros() {
        campNome.setBorder(bordaCampo(COR_BORDA, 1));
        campLotacao.setBorder(bordaCampo(COR_BORDA, 1));
        comboPais.setBorder(null);
        comboCidade.setBorder(null);
        lblErroNome.setText(" ");
        lblErroLotacao.setText(" ");
        lblErroPais.setText(" ");
        lblErroCidade.setText(" ");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Utilitários de construção visual
    // ══════════════════════════════════════════════════════════════════════════

    private JTextField criarTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setForeground(COR_PLACEHOLDER);
        tf.setBorder(bordaCampo(COR_BORDA, 1));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(COR_TEXTO); }
                tf.setBorder(bordaCampo(COR_FOCO, 1));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isBlank()) { tf.setText(placeholder); tf.setForeground(COR_PLACEHOLDER); }
                tf.setBorder(bordaCampo(COR_BORDA, 1));
            }
        });
        return tf;
    }

    private void estilizarCombo(JComboBox<String> cb) {
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBackground(COR_BRANCO);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JLabel criarLabelErro() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(COR_ERRO);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton criarBotao(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xF0F0F0) : COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(COR_TEXTO);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_BORDA, 1, true), new EmptyBorder(8, 16, 8, 16)));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Botão secundário (mais discreto) — usado para "Editar Preço...". */
    private JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xEDEDED) : new Color(0xF5F5F5));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(new Color(0x1565C0));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xBBDEFB), 1, true), new EmptyBorder(6, 12, 6, 12)));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static javax.swing.border.Border bordaCampo(Color cor, int espessura) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(cor, espessura, true), new EmptyBorder(6, 10, 6, 10));
    }

    private String textoReal(JTextField tf, String placeholder) {
        return tf.getText().trim().equals(placeholder) ? "" : tf.getText().trim();
    }
}