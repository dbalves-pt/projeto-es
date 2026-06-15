package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Vista UC01 — Formulário "Inserir Equipa"
 *
 * Replica o mockup Figma:
 *   • Fundo azul claro (#AED6F1)
 *   • Painel branco arredondado no centro
 *   • Título "Inserir Equipa" a negrito
 *   • Campo "Nome…" (JTextField)
 *   • Dropdown "País ▼" (JComboBox)
 *   • Campo "Grupo (atribuído automaticamente)" — cinzento/desativado
 *   • Secção "Jogadores" (cabeçalho cinzento) + "Inserir jogador…" (UC03)
 *   • Botão "Concluído"
 *
 * Validações visuais (bordas vermelhas) conforme caminhos alternativos UC01.
 */
public class FormularioInserirEquipa extends JDialog {

    // ── Cores extraídas do Figma ───────────────────────────────────────────────
    private static final Color COR_FUNDO_AZUL      = new Color(0xAED6F1);
    private static final Color COR_BRANCO          = Color.WHITE;
    private static final Color COR_CINZENTO_HEADER = new Color(0xBDBDBD);
    private static final Color COR_CINZENTO_CAMPO  = new Color(0xEEEEEE);
    private static final Color COR_TEXTO_ESCURO    = new Color(0x212121);
    private static final Color COR_ERRO            = new Color(0xE53935);
    private static final Color COR_BOTAO_HOVER     = new Color(0xF5F5F5);

    private static final Font FONTE_TITULO  = new Font("SansSerif", Font.BOLD, 16);
    private static final Font FONTE_NORMAL  = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONTE_HEADER  = new Font("SansSerif", Font.BOLD, 12);

    // ── Componentes ───────────────────────────────────────────────────────────
    private JTextField    campNome;
    private JComboBox<String> comboPais;
    private JTextField    campGrupo;
    private JButton       btnConcluido;
    private JLabel        lblErroNome;
    private JLabel        lblErroPais;

    // ── Dependências ──────────────────────────────────────────────────────────
    private final EquipaControlador controlador;
    private final Runnable          aoAtualizar;   // callback para refrescar o PainelEquipas

    /**
     * @param owner        Janela pai
     * @param controlador  Instância do controlador UC01
     * @param aoAtualizar  Callback invocado após inserção com sucesso
     */
    public FormularioInserirEquipa(Window owner,
                                   EquipaControlador controlador,
                                   Runnable aoAtualizar) {
        super(owner, "Inserir Equipa", ModalityType.APPLICATION_MODAL);
        this.controlador = controlador;
        this.aoAtualizar = aoAtualizar;
        construirUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Construção da interface
    // ══════════════════════════════════════════════════════════════════════════

    private void construirUI() {
        // Fundo azul claro — toda a janela
        JPanel painelRaiz = new JPanel(new GridBagLayout());
        painelRaiz.setBackground(COR_FUNDO_AZUL);
        painelRaiz.setBorder(new EmptyBorder(24, 24, 24, 24));
        setContentPane(painelRaiz);

        // Painel branco arredondado
        JPanel painelBranco = criarPainelBranco();
        painelRaiz.add(painelBranco);
    }

    private JPanel criarPainelBranco() {
        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(310, 380));
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Título ────────────────────────────────────────────────────────────
        JLabel titulo = new JLabel("Inserir Equipa");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(COR_TEXTO_ESCURO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(16));

        // ── Campo Nome ────────────────────────────────────────────────────────
        campNome = criarTextField("Nome...");
        campNome.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(campNome);

        lblErroNome = criarLabelErro();
        painel.add(lblErroNome);
        painel.add(Box.createVerticalStrut(8));

        // ── Dropdown País ─────────────────────────────────────────────────────
        List<String> paises = controlador.getPaisesDisponiveis();
        String[] opcoesPais = new String[paises.size() + 1];
        opcoesPais[0] = "País";
        for (int i = 0; i < paises.size(); i++) {
            opcoesPais[i + 1] = paises.get(i);
        }
        comboPais = new JComboBox<>(opcoesPais);
        comboPais.setFont(FONTE_NORMAL);
        comboPais.setBackground(COR_BRANCO);
        comboPais.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboPais.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(comboPais);

        lblErroPais = criarLabelErro();
        painel.add(lblErroPais);
        painel.add(Box.createVerticalStrut(8));

        // ── Campo Grupo (desativado) ───────────────────────────────────────────
        campGrupo = new JTextField("Grupo (atribuído automaticamente)");
        campGrupo.setFont(FONTE_NORMAL);
        campGrupo.setEnabled(false);
        campGrupo.setBackground(COR_CINZENTO_CAMPO);
        campGrupo.setForeground(new Color(0x757575));
        campGrupo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        campGrupo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campGrupo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(campGrupo);
        painel.add(Box.createVerticalStrut(10));

        // ── Secção Jogadores ──────────────────────────────────────────────────
        painel.add(criarSecaoJogadores());
        painel.add(Box.createVerticalStrut(16));

        // ── Botão Concluído ───────────────────────────────────────────────────
        btnConcluido = criarBotaoConcluido();
        btnConcluido.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(btnConcluido);

        return painel;
    }

    // ── Utilitários de construção ──────────────────────────────────────────────

    private JTextField criarTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONTE_NORMAL);
        tf.setForeground(new Color(0x9E9E9E));
        tf.setText(placeholder);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Comportamento de placeholder
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(COR_TEXTO_ESCURO);
                }
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(0x1976D2), 1, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isBlank()) {
                    tf.setText(placeholder);
                    tf.setForeground(new Color(0x9E9E9E));
                }
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(0xCCCCCC), 1, true),
                        new EmptyBorder(6, 10, 6, 10)));
            }
        });
        return tf;
    }

    private JLabel criarLabelErro() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(COR_ERRO);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel criarSecaoJogadores() {
        JPanel secao = new JPanel();
        secao.setLayout(new BoxLayout(secao, BoxLayout.Y_AXIS));
        secao.setOpaque(false);
        secao.setAlignmentX(Component.LEFT_ALIGNMENT);
        secao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Cabeçalho cinzento "Jogadores"
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setBackground(COR_CINZENTO_HEADER);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblJogadores = new JLabel("  Jogadores");
        lblJogadores.setFont(FONTE_HEADER);
        lblJogadores.setForeground(COR_BRANCO);
        header.add(lblJogadores);
        secao.add(header);

        // Link "Inserir jogador…" (UC03 — aqui apenas apresentado)
        JLabel linkInserirJogador = new JLabel("  Inserir jogador...");
        linkInserirJogador.setFont(FONTE_NORMAL);
        linkInserirJogador.setForeground(new Color(0x757575));
        linkInserirJogador.setBackground(new Color(0xF5F5F5));
        linkInserirJogador.setOpaque(true);
        linkInserirJogador.setBorder(new EmptyBorder(6, 4, 6, 4));
        linkInserirJogador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        linkInserirJogador.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Nota: a lógica de clique será ligada no UC03
        secao.add(linkInserirJogador);

        return secao;
    }

    private JButton criarBotaoConcluido() {
        JButton btn = new JButton("Concluído") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? COR_BOTAO_HOVER : COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONTE_NORMAL);
        btn.setForeground(COR_TEXTO_ESCURO);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                new EmptyBorder(8, 16, 8, 16)));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this::aoConcluir);
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Lógica do botão Concluído (ligação ao Controlador)
    // ══════════════════════════════════════════════════════════════════════════

    private void aoConcluir(ActionEvent e) {
        limparErros();

        String nome = obterTextoReal(campNome, "Nome...");
        String pais = comboPais.getSelectedIndex() == 0
                ? ""
                : (String) comboPais.getSelectedItem();

        try {
            controlador.adicionarEquipa(nome, pais);
            // Sucesso — notifica o painel pai e fecha
            if (aoAtualizar != null) aoAtualizar.run();
            dispose();

        } catch (IllegalArgumentException ex) {
            tratarErroArgumento(ex.getMessage());
        } catch (IllegalStateException ex) {
            // CA "Grupos gerados" — situação anómala (botão devia estar bloqueado)
            JOptionPane.showMessageDialog(this,
                    "Não é possível adicionar equipas: os grupos já foram gerados.",
                    "Operação bloqueada", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── Gestão de erros visuais (bordas vermelhas) ────────────────────────────

    private void tratarErroArgumento(String codigo) {
        switch (codigo) {
            case "CAMPO_NOME_VAZIO" -> {
                marcarCampoComErro(campNome);
                lblErroNome.setText("O nome é obrigatório.");
            }
            case "CAMPO_PAIS_VAZIO" -> {
                marcarComboComErro(comboPais);
                lblErroPais.setText("Selecione um país.");
            }
            case "NOME_DUPLICADO" -> {
                marcarCampoComErro(campNome);
                lblErroNome.setText("Já existe uma equipa com este nome.");
            }
            case "PAIS_INVALIDO" -> {
                marcarComboComErro(comboPais);
                lblErroPais.setText("País inválido. Selecione um da lista.");
            }
            default -> JOptionPane.showMessageDialog(this, codigo,
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void marcarCampoComErro(JTextField campo) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_ERRO, 2, true),
                new EmptyBorder(6, 10, 6, 10)));
    }

    private void marcarComboComErro(JComboBox<?> combo) {
        combo.setBorder(new LineBorder(COR_ERRO, 2, true));
    }

    private void limparErros() {
        campNome.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        comboPais.setBorder(null);
        lblErroNome.setText(" ");
        lblErroPais.setText(" ");
    }

    /** Devolve o texto real do campo, ignorando o placeholder. */
    private String obterTextoReal(JTextField campo, String placeholder) {
        String txt = campo.getText().trim();
        return txt.equals(placeholder) ? "" : txt;
    }
}