package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EstadioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Bancada;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Estadio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Vista — Formulário "Inserir Bancada".
 *
 * Aqui é que se materializa a validação CA 4.1 (Lotação Excedida):
 * o campo "Lugares" é pintado a vermelho com mensagem específica,
 * em vez de um JOptionPane genérico — exactamente como pedido.
 */
public class FormularioBancada extends JDialog {

    static final Color COR_FUNDO       = new Color(0xAED6F1);
    static final Color COR_BRANCO      = Color.WHITE;
    static final Color COR_BORDA       = new Color(0xCCCCCC);
    static final Color COR_TEXTO       = new Color(0x212121);
    static final Color COR_PLACEHOLDER = new Color(0x9E9E9E);
    static final Color COR_ERRO        = new Color(0xE53935);
    static final Color COR_FOCO        = new Color(0x1976D2);

    private JTextField campNome;
    private JTextField campPreco;
    private JComboBox<String> comboCategoria;
    private JTextField campFilas;
    private JTextField campLugares;

    private JLabel lblErroNome;
    private JLabel lblErroPreco;
    private JLabel lblErroCategoria;
    private JLabel lblErroFilas;
    private JLabel lblErroLugares;   // ← aqui aparece "Lotação Excedida"

    private final EstadioControlador ctrlEstadio;
    private final Estadio            estadio;
    private final Runnable           aoAtualizar;
    private final Bancada bancadaEdit;

    public FormularioBancada(Window owner, EstadioControlador ctrlEstadio,
                             Estadio estadio, Bancada bancadaEdit, Runnable aoAtualizar) {

        // O título da janela muda automaticamente consoante estamos a inserir ou editar
        super(owner, bancadaEdit == null ? "Inserir Bancada" : "Editar Bancada", ModalityType.APPLICATION_MODAL);

        this.ctrlEstadio = ctrlEstadio;
        this.estadio     = estadio;
        this.bancadaEdit = bancadaEdit;
        this.aoAtualizar = aoAtualizar;

        construirUI();

        // ── PRÉ-PREENCHIMENTO DE DADOS (MODO EDIÇÃO) ──
        if (this.bancadaEdit != null) {
            campNome.setText(this.bancadaEdit.getNome());
            campNome.setForeground(COR_TEXTO);

            // Converte o double para String (Ex: 20.0)
            campPreco.setText(String.valueOf(this.bancadaEdit.getPreco()));
            campPreco.setForeground(COR_TEXTO);

            // Seleciona a categoria correta na Dropdown
            comboCategoria.setSelectedItem(this.bancadaEdit.getCategoria());

            // Converte os inteiros para String
            campFilas.setText(String.valueOf(this.bancadaEdit.getFilas()));
            campFilas.setForeground(COR_TEXTO);

            campLugares.setText(String.valueOf(this.bancadaEdit.getLugares()));
            campLugares.setForeground(COR_TEXTO);
        }

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
        p.setPreferredSize(new Dimension(320, 460));
        p.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel titulo = new JLabel(bancadaEdit == null ? "Inserir Bancada" : "Editar Bancada");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(titulo);
        p.add(Box.createVerticalStrut(15));

        // Indicação da lotação disponível — ajuda o utilizador a evitar o erro 4.1
        int restante = estadio.getLotacaoMaxima() - estadio.getLotacaoAtual();
        JLabel lblInfo = new JLabel("Lugares disponíveis no estádio: " + restante);
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblInfo.setForeground(new Color(0x616161));
        lblInfo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lblInfo);
        p.add(Box.createVerticalStrut(10));

        campNome = criarTextField("Nome da Bancada...");
        p.add(campNome);
        lblErroNome = criarLabelErro();
        p.add(lblErroNome);
        p.add(Box.createVerticalStrut(8));

        campPreco = criarTextField("Preço (€)...");
        p.add(campPreco);
        lblErroPreco = criarLabelErro();
        p.add(lblErroPreco);
        p.add(Box.createVerticalStrut(8));

        // ── Dropdown Categoria ──
        String[] opcoesCategoria = {"Categoria...", "VIP", "STANDARD"};
        comboCategoria = new JComboBox<>(opcoesCategoria);
        comboCategoria.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboCategoria.setBackground(COR_BRANCO);
        comboCategoria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboCategoria.setAlignmentX(LEFT_ALIGNMENT);
        p.add(comboCategoria);

        lblErroCategoria = criarLabelErro();
        p.add(lblErroCategoria);
        p.add(Box.createVerticalStrut(8));

        campFilas = criarTextField("Número de Filas...");
        p.add(campFilas);
        lblErroFilas = criarLabelErro();
        p.add(lblErroFilas);
        p.add(Box.createVerticalStrut(8));

        campLugares = criarTextField("Número de Lugares...");
        p.add(campLugares);
        lblErroLugares = criarLabelErro();
        p.add(lblErroLugares);
        p.add(Box.createVerticalStrut(15));

        JButton btnConcluido = criarBotao("Concluído");
        btnConcluido.addActionListener(this::guardar);
        p.add(btnConcluido);

        raiz.add(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Guardar — com validação visual específica (CA 4.1 destacado)
    // ══════════════════════════════════════════════════════════════════════════

    private void guardar(ActionEvent e) {
        limparErros();

        String nome      = textoReal(campNome,      "Nome da Bancada...");
        String preco      = textoReal(campPreco,      "Preço (€)...");
        // Se for o índice 0 ("Categoria..."), envia vazio para o controlador disparar erro
        String categoria = comboCategoria.getSelectedIndex() == 0 ? "" : (String) comboCategoria.getSelectedItem();
        String filas      = textoReal(campFilas,      "Número de Filas...");
        String lugares    = textoReal(campLugares,    "Número de Lugares...");

        try {
            if (bancadaEdit == null) {
                // Se a bancadaEdit for null, significa que clicámos no botão "Inserir"
                ctrlEstadio.adicionarBancada(estadio, nome, preco, categoria, filas, lugares);
            } else {
                // Se existir uma bancadaEdit, significa que fizemos duplo clique para "Editar"
                ctrlEstadio.editarBancada(estadio, bancadaEdit, nome, preco, categoria, filas, lugares);
            }

            if (aoAtualizar != null) aoAtualizar.run();
            dispose();

        } catch (IllegalArgumentException ex) {
            tratarErro(ex.getMessage());
        }
    }

    private void tratarErro(String codigo) {
        switch (codigo) {
            case "CAMPOS_VAZIOS" -> {
                if (textoReal(campNome, "Nome da Bancada...").isBlank())
                    marcarErro(campNome, lblErroNome, "Campo obrigatório.");
                if (textoReal(campPreco, "Preço (€)...").isBlank())
                    marcarErro(campPreco, lblErroPreco, "Campo obrigatório.");
                if (comboCategoria.getSelectedIndex() == 0) {
                    comboCategoria.setBorder(new LineBorder(COR_ERRO, 2, true));
                    lblErroCategoria.setText("Campo obrigatório.");
                }
                if (textoReal(campFilas, "Número de Filas...").isBlank())
                    marcarErro(campFilas, lblErroFilas, "Campo obrigatório.");
                if (textoReal(campLugares, "Número de Lugares...").isBlank())
                    marcarErro(campLugares, lblErroLugares, "Campo obrigatório.");
            }
            case "NOME_BANCADA_DUPLICADO" ->
                    marcarErro(campNome, lblErroNome,
                            "Já existe uma bancada com este nome neste estádio.");
            case "VALOR_PRECO_INVALIDO" ->
                    marcarErro(campPreco, lblErroPreco, "Introduza um preço numérico positivo.");
            case "VALOR_FILAS_INVALIDO" ->
                    marcarErro(campFilas, lblErroFilas, "Introduza um número de filas positivo.");
            case "VALOR_LUGARES_INVALIDO" ->
                    marcarErro(campLugares, lblErroLugares, "Introduza um número de lugares positivo.");
            case "LOTACAO_EXCEDIDA" -> {
                // ── CA 4.1 — exactamente o que foi pedido: pintar a caixa
                //     de "Lugares" a vermelho, com a origem do erro explícita.
                int restante = estadio.getLotacaoMaxima() - estadio.getLotacaoAtual();
                marcarErro(campLugares, lblErroLugares,
                        "Lotação excedida — restam apenas " + restante + " lugares neste estádio.");
            }
            default ->
                    JOptionPane.showMessageDialog(this, codigo, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void marcarErro(JTextField campo, JLabel lblErro, String mensagem) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_ERRO, 2, true), new EmptyBorder(6, 10, 6, 10)));
        lblErro.setText(mensagem);
    }

    private void limparErros() {
        // Retirei o campCategoria daqui de dentro
        for (JTextField tf : new JTextField[]{campNome, campPreco, campFilas, campLugares}) {
            tf.setBorder(bordaCampo(COR_BORDA, 1));
        }
        // Limpa a borda da Dropdown
        comboCategoria.setBorder(null);

        lblErroNome.setText(" ");
        lblErroPreco.setText(" ");
        lblErroCategoria.setText(" ");
        lblErroFilas.setText(" ");
        lblErroLugares.setText(" ");
    }

    // ── Utilitários visuais ───────────────────────────────────────────────────

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

    private static javax.swing.border.Border bordaCampo(Color cor, int espessura) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(cor, espessura, true), new EmptyBorder(6, 10, 6, 10));
    }

    private String textoReal(JTextField tf, String placeholder) {
        return tf.getText().trim().equals(placeholder) ? "" : tf.getText().trim();
    }
}