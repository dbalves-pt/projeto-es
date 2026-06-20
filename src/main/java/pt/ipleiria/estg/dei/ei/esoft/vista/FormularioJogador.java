package pt.ipleiria.estg.dei.ei.esoft.vista;


import pt.ipleiria.estg.dei.ei.esoft.controlador.JogadorControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static pt.ipleiria.estg.dei.ei.esoft.vista.FormularioEquipa.*;

/**
 * Vista UC03 — Formulário "Inserir/Editar Jogador"
 *
 * Design Figma (imagem enviada):
 *   • Fundo azul claro (#AED6F1) igual ao formulário de equipa
 *   • Painel branco com cantos arredondados
 *   • Título "Inserir/Editar Jogador" a negrito
 *   • Campo  "Nome Completo…"
 *   • Campo  "Equipa (Escolhida no menu anterior)" — bloqueado/cinzento
 *   • Combo  "Posição ▼"
 *   • Campo  "Data de Nascimento…"
 *   • Campo  "Número de Camisola…"
 *   • Combo  "Estado ▼"
 *   • Espaço vazio (área de lista — não usa no UC03)
 *   • Botão  "Concluído"
 *
 * Modos:
 *   • jogadorParaEditar == null  → INSERIR
 *   • jogadorParaEditar != null  → EDITAR (pré-preenchido)
 */
public class FormularioJogador extends JDialog {

    // ── Componentes ───────────────────────────────────────────────────────────
    private JTextField        campNome;
    private JTextField        campEquipa;
    private JComboBox<String> comboPosicao;
    private JTextField        campData;
    private JTextField        campNumero;
    private JComboBox<String> comboEstado;

    private JLabel lblErroNome;
    private JLabel lblErroPosicao;
    private JLabel lblErroData;
    private JLabel lblErroNumero;
    private JLabel lblErroEstado;

    // ── Dependências ──────────────────────────────────────────────────────────
    private final JogadorControlador ctrl;
    private final Equipa             equipa;
    private final Jogador            jogadorParaEditar;   // null = inserção
    private final Runnable           aoAtualizar;

    // ══════════════════════════════════════════════════════════════════════════
    //  Construtores
    // ══════════════════════════════════════════════════════════════════════════

    /** UC03 — Inserir novo jogador. */
    public FormularioJogador(Window owner,
                             JogadorControlador ctrl,
                             Equipa equipa,
                             Runnable aoAtualizar) {
        this(owner, ctrl, equipa, null, aoAtualizar);
    }

    /** UC03 — Editar jogador existente. */
    public FormularioJogador(Window owner,
                             JogadorControlador ctrl,
                             Equipa equipa,
                             Jogador jogadorParaEditar,
                             Runnable aoAtualizar) {
        super(owner, "Inserir/Editar Jogador", ModalityType.APPLICATION_MODAL);
        this.ctrl              = ctrl;
        this.equipa            = equipa;
        this.jogadorParaEditar = jogadorParaEditar;
        this.aoAtualizar       = aoAtualizar;
        construirUI();
        if (jogadorParaEditar != null) preencherFormulario();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Construção da UI — réplica exacta do Figma
    // ══════════════════════════════════════════════════════════════════════════

    private void construirUI() {
        JPanel raiz = new JPanel(new GridBagLayout());
        raiz.setBackground(COR_FUNDO);
        raiz.setBorder(new EmptyBorder(24, 24, 24, 24));
        setContentPane(raiz);
        raiz.add(criarPainelBranco());
    }

    private JPanel criarPainelBranco() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(320, 480));
        p.setBorder(new EmptyBorder(22, 22, 22, 22));

        // ── Título ────────────────────────────────────────────────────────────
        JLabel titulo = new JLabel("Inserir/Editar Jogador");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(titulo);
        p.add(rigidH(18));

        // ── Nome Completo ─────────────────────────────────────────────────────
        campNome = criarTextField("Nome Completo...");
        p.add(campNome);
        lblErroNome = labelErro();
        p.add(lblErroNome);
        p.add(rigidH(8));

        // ── Equipa (bloqueado — contexto) ─────────────────────────────────────
        campEquipa = new JTextField(equipa != null ? equipa.getNome()
                : "Equipa (Escolhida no menu anterior)");
        campEquipa.setFont(FONTE_NORMAL);
        campEquipa.setEnabled(false);
        campEquipa.setDisabledTextColor(new Color(0x555555));
        campEquipa.setBackground(new Color(0xEEEEEE));
        campEquipa.setBorder(bordaCampo(COR_BORDA, 1));
        campEquipa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        campEquipa.setAlignmentX(LEFT_ALIGNMENT);
        p.add(campEquipa);
        p.add(rigidH(8));

        // ── Posição (dropdown) ────────────────────────────────────────────────
        List<String> posicoes = ctrl.getDescricoesPosicao();
        String[] opcoesPosicao = prepararOpcoes("Posição", posicoes);
        comboPosicao = criarCombo(opcoesPosicao);
        p.add(comboPosicao);
        lblErroPosicao = labelErro();
        p.add(lblErroPosicao);
        p.add(rigidH(8));

        // ── Data de Nascimento ────────────────────────────────────────────────
        campData = criarTextField("Data de Nascimento...");
        // CHAMA O MÉTODO DIRETAMENTE DA OUTRA CLASSE:
        JanelaPrincipal.aplicarMascaraData(campData);

        p.add(campData);
        lblErroData = labelErro();
        p.add(lblErroData);
        p.add(rigidH(8));

        // ── Número de Camisola ────────────────────────────────────────────────
        campNumero = criarTextField("Número de Camisola...");
        p.add(campNumero);
        lblErroNumero = labelErro();
        p.add(lblErroNumero);
        p.add(rigidH(8));

        // ── Estado (dropdown) ─────────────────────────────────────────────────
        List<String> estados = ctrl.getDescricoesEstado();
        String[] opcoesEstado = prepararOpcoes("Estado", estados);
        comboEstado = criarCombo(opcoesEstado);
        p.add(comboEstado);
        lblErroEstado = labelErro();
        p.add(lblErroEstado);
        p.add(rigidH(18));

        // ── Botão Concluído ───────────────────────────────────────────────────
        p.add(criarBotaoConcluido());

        return p;
    }

    // ── Pré-preenchimento ─────────────────────────────────────────────────────

    private void preencherFormulario() {
        campNome.setText(jogadorParaEditar.getNomeCompleto());
        campNome.setForeground(COR_TEXTO);

        selecionarCombo(comboPosicao, jogadorParaEditar.getPosicao().toString());
        campData.setText(jogadorParaEditar.getDataNascimentoFormatada());
        campData.setForeground(COR_TEXTO);
        campNumero.setText(String.valueOf(jogadorParaEditar.getNumeroCamisola()));
        campNumero.setForeground(COR_TEXTO);
        selecionarCombo(comboEstado, jogadorParaEditar.getEstado().toString());

        // ── NOVO: SE O TORNEIO JÁ COMEÇOU, TRANCA OS CAMPOS VISUALMENTE ──
        if (pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.getInstancia().getEstado() == pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.Estado.EM_CURSO) {
            campNome.setEnabled(false);
            comboPosicao.setEnabled(false);
            campData.setEnabled(false);
            campNumero.setEnabled(false);

            // Opcional: Adiciona uma dica no título para o utilizador saber o que se passa
            setTitle("Modo Médico — Apenas Alteração de Estado");
        }
    }

    private void selecionarCombo(JComboBox<String> cb, String valor) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).equalsIgnoreCase(valor)) {
                cb.setSelectedIndex(i);
                return;
            }
        }
    }

    // ── Botão Concluído ───────────────────────────────────────────────────────

    private JButton criarBotaoConcluido() {
        JButton btn = new JButton("Concluído") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(0xF0F0F0) : COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONTE_NORMAL);
        btn.setForeground(COR_TEXTO);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_BORDA, 1, true),
                new EmptyBorder(8, 16, 8, 16)));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this::aoConcluir);
        return btn;
    }

    private void aoConcluir(ActionEvent e) {
        limparErros();

        String nome    = textoReal(campNome,   "Nome Completo...");
        String posicao = comboPosicao.getSelectedIndex() == 0 ? "" : (String) comboPosicao.getSelectedItem();
        String data    = textoReal(campData,   "Data de Nascimento...");
        String numero  = textoReal(campNumero, "Número de Camisola...");
        String estado  = comboEstado.getSelectedIndex()  == 0 ? "" : (String) comboEstado.getSelectedItem();

        try {
            if (jogadorParaEditar == null) {
                ctrl.adicionarJogador(equipa, nome, posicao, data, numero, estado);
            } else {
                ctrl.editarJogador(jogadorParaEditar, nome, posicao, data, numero, estado);
            }
            if (aoAtualizar != null) aoAtualizar.run();
            dispose();

        } catch (IllegalArgumentException ex) {
            tratarErro(ex.getMessage());
        }
    }

    // ── Tratamento de erros visuais ────────────────────────────────────────────

    private void tratarErro(String codigo) {
        switch (codigo) {
            case "CAMPO_NOME_VAZIO"        -> { destacarErro(campNome);    lblErroNome.setText("Nome obrigatório."); }
            case "CAMPO_POSICAO_VAZIO", "POSICAO_INVALIDA"
                    -> { destacarComboErro(comboPosicao); lblErroPosicao.setText("Selecione a posição."); }
            case "CAMPO_DATA_VAZIO", "DATA_INVALIDA"
                    -> { destacarErro(campData);    lblErroData.setText("Use o formato dd/MM/yyyy."); }
            case "CAMPO_NUMERO_VAZIO", "NUMERO_INVALIDO"
                    -> { destacarErro(campNumero);  lblErroNumero.setText("Número inválido (1–99)."); }
            case "NUMERO_FORA_INTERVALO"   -> { destacarErro(campNumero);  lblErroNumero.setText("Número deve estar entre 1 e 99."); }
            case "NUMERO_CAMISOLA_DUPLICADO" -> { destacarErro(campNumero); lblErroNumero.setText("Este número já existe na equipa."); }
            case "CAMPO_ESTADO_VAZIO", "ESTADO_INVALIDO"
                    -> { destacarComboErro(comboEstado); lblErroEstado.setText("Selecione o estado."); }
            case "IDADE_INVALIDA" -> {
                destacarErro(campData); // Assume-se que a variável seja campData ou semelhante
                lblErroData.setText("A idade deve estar entre 15 e 50 anos.");
            }
            case "LIMITE_JOGADORES_APTO_EXCEDIDO" -> {
                JOptionPane.showMessageDialog(this,
                        "Não é possível registar/alterar para APTO. A equipa já atingiu o limite máximo de 23 jogadores aptos.",
                        "Aviso de Inscrição", JOptionPane.WARNING_MESSAGE);
            }
            default -> JOptionPane.showMessageDialog(this, codigo, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void destacarErro(JTextField tf) {
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_ERRO, 2, true),
                new EmptyBorder(6, 10, 6, 10)));
    }

    private void destacarComboErro(JComboBox<?> cb) {
        cb.setBorder(new LineBorder(COR_ERRO, 2, true));
    }

    private void limparErros() {
        campNome.setBorder(bordaCampo(COR_BORDA, 1));
        comboPosicao.setBorder(null);
        campData.setBorder(bordaCampo(COR_BORDA, 1));
        campNumero.setBorder(bordaCampo(COR_BORDA, 1));
        comboEstado.setBorder(null);
        lblErroNome.setText(" ");
        lblErroPosicao.setText(" ");
        lblErroData.setText(" ");
        lblErroNumero.setText(" ");
        lblErroEstado.setText(" ");
    }

    // ── Utilitários de construção ──────────────────────────────────────────────

    private JTextField criarTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONTE_NORMAL);
        tf.setForeground(COR_PLACEHOLDER);
        tf.setText(placeholder);
        tf.setBorder(bordaCampo(COR_BORDA, 1));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(LEFT_ALIGNMENT);

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText(""); tf.setForeground(COR_TEXTO);
                }
                tf.setBorder(bordaCampo(COR_FOCO, 1));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isBlank()) {
                    tf.setText(placeholder); tf.setForeground(COR_PLACEHOLDER);
                }
                tf.setBorder(bordaCampo(COR_BORDA, 1));
            }
        });
        return tf;
    }

    private JComboBox<String> criarCombo(String[] opcoes) {
        JComboBox<String> cb = new JComboBox<>(opcoes);
        cb.setFont(FONTE_NORMAL);
        cb.setBackground(COR_BRANCO);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setAlignmentX(LEFT_ALIGNMENT);
        return cb;
    }

    private JLabel labelErro() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(FONTE_ERRO);
        lbl.setForeground(COR_ERRO);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private String[] prepararOpcoes(String primeiro, List<String> lista) {
        String[] arr = new String[lista.size() + 1];
        arr[0] = primeiro;
        for (int i = 0; i < lista.size(); i++) arr[i + 1] = lista.get(i);
        return arr;
    }

    private static javax.swing.border.Border bordaCampo(Color cor, int esp) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(cor, esp, true),
                new EmptyBorder(6, 10, 6, 10));
    }

    private static Box.Filler rigidH(int h) {
        return (Box.Filler) Box.createRigidArea(new Dimension(0, h));
    }

    private String textoReal(JTextField tf, String placeholder) {
        String t = tf.getText().trim();
        return t.equals(placeholder) ? "" : t;
    }
}