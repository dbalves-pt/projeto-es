package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EventoControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.EventoJogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static pt.ipleiria.estg.dei.ei.esoft.vista.FormularioEquipa.*;

/**
 * Formulário partilhado por UC10 (Registar Evento de Jogo) e
 * UC12 (Corrigir Evento de Jogo).
 *
 * Modos:
 *   • eventoParaEditar == null  → INSERIR (UC10)
 *   • eventoParaEditar != null  → EDITAR  (UC12) — pré-preenchido
 *
 * Campos: Tipo de Evento, Equipa, Jogador, Minuto.
 */
public class FormularioEvento extends JDialog {

    private JComboBox<String> comboTipo;
    private JComboBox<String> comboEquipa;
    private JComboBox<String> comboJogador;
    private JComboBox<String> comboAssistencia;
    private JTextField        campMinuto;

    private JLabel lblErroTipo;
    private JLabel lblErroEquipa;
    private JLabel lblErroJogador;
    private JLabel lblErroMinuto;

    private final EventoControlador eventoControlador;
    private final Jogo              jogo;
    private final EventoJogo        eventoParaEditar;   // null = inserção
    private final Runnable          aoAtualizar;

    // Listas paralelas para mapear índice de combo -> objeto real
    private final List<Equipa>  equipasDisponiveis  = new ArrayList<>();
    private final List<Jogador> jogadoresDisponiveis = new ArrayList<>();

    public FormularioEvento(Window owner,
                             EventoControlador eventoControlador,
                             Jogo jogo,
                             EventoJogo eventoParaEditar,
                             Runnable aoAtualizar) {
        super(owner,
              eventoParaEditar == null ? "Registar Evento" : "Editar Evento",
              ModalityType.APPLICATION_MODAL);
        this.eventoControlador = eventoControlador;
        this.jogo              = jogo;
        this.eventoParaEditar  = eventoParaEditar;
        this.aoAtualizar       = aoAtualizar;
        construirUI();
        if (eventoParaEditar != null) preencherFormulario();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Construção da UI
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(340, 420));
        p.setBorder(new EmptyBorder(22, 22, 22, 22));

        // ── Título ────────────────────────────────────────────────────────────
        String tituloTexto = eventoParaEditar == null ? "Registar Evento" : "Editar Evento";
        JLabel titulo = new JLabel(tituloTexto);
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(titulo);
        p.add(rigidH(18));

        // ── Tipo de Evento ────────────────────────────────────────────────────
        List<String> tipos = eventoControlador.getDescricoesTipo();
        comboTipo = criarCombo(prepararOpcoes("Tipo de Evento", tipos));
        comboTipo.addActionListener(e -> {
            boolean isGolo = "Golo".equals(comboTipo.getSelectedItem());
            comboAssistencia.setVisible(isGolo);
        });
        p.add(comboTipo);
        lblErroTipo = labelErro();
        p.add(lblErroTipo);
        p.add(rigidH(8));

        // ── Equipa ────────────────────────────────────────────────────────────
        equipasDisponiveis.clear();
        equipasDisponiveis.add(jogo.getEquipaCasa());
        equipasDisponiveis.add(jogo.getEquipaFora());
        String[] opcoesEquipa = new String[equipasDisponiveis.size() + 1];
        opcoesEquipa[0] = "Equipa";
        for (int i = 0; i < equipasDisponiveis.size(); i++)
            opcoesEquipa[i + 1] = equipasDisponiveis.get(i).getNome();

        comboEquipa = criarCombo(opcoesEquipa);
        comboEquipa.addActionListener(e -> atualizarJogadores());
        p.add(comboEquipa);
        lblErroEquipa = labelErro();
        p.add(lblErroEquipa);
        p.add(rigidH(8));

        // ── Jogador (actualizado consoante a equipa) ──────────────────────────
        comboJogador = criarCombo(new String[]{"Jogador"});
        p.add(comboJogador);
        lblErroJogador = labelErro();
        p.add(lblErroJogador);
        p.add(rigidH(8));

        // ── Assistência (Invisível por defeito) ───────────────────────────────
        comboAssistencia = criarCombo(new String[]{"Assistência (Opcional)"});
        comboAssistencia.setVisible(false);
        p.add(comboAssistencia);
        p.add(rigidH(8));

        // ── Minuto ────────────────────────────────────────────────────────────
        campMinuto = criarTextField("Minuto (0–130)...");
        p.add(campMinuto);
        lblErroMinuto = labelErro();
        p.add(lblErroMinuto);
        p.add(rigidH(18));

        // ── Botão Concluído ───────────────────────────────────────────────────
        p.add(criarBotaoConcluido());

        return p;
    }

    // ── Actualização da lista de jogadores ao mudar a equipa ──────────────────

    private void atualizarJogadores() {
        int idxEquipa = comboEquipa.getSelectedIndex();
        jogadoresDisponiveis.clear();

        comboJogador.removeAllItems();
        comboJogador.addItem("Jogador");

        comboAssistencia.removeAllItems();
        comboAssistencia.addItem("Assistência (Opcional)");

        if (idxEquipa > 0) {
            Equipa eq = equipasDisponiveis.get(idxEquipa - 1);
            for (Jogador j : eq.getJogadores()) {
                if (j.getEstado() == Jogador.Estado.APTO) {
                    jogadoresDisponiveis.add(j);
                    comboJogador.addItem(j.getNomeCompleto());
                    comboAssistencia.addItem(j.getNomeCompleto());
                }
            }
        }
    }

    // ── Pré-preenchimento (UC12 — Editar) ─────────────────────────────────────

    private void preencherFormulario() {
        // Tipo
        selecionarCombo(comboTipo, eventoParaEditar.getTipo().toString());

        // Equipa
        selecionarCombo(comboEquipa, eventoParaEditar.getEquipa().getNome());
        atualizarJogadores();

        // Jogador
        selecionarCombo(comboJogador, eventoParaEditar.getJogador().getNomeCompleto());

        // Minuto
        campMinuto.setText(String.valueOf(eventoParaEditar.getMinuto()));
        campMinuto.setForeground(COR_TEXTO);

        // Assistência (NOVO)
        if (eventoParaEditar.getTipo() == EventoJogo.Tipo.GOLO) {
            comboAssistencia.setVisible(true);
            if (eventoParaEditar.getAssistencia() != null) {
                selecionarCombo(comboAssistencia, eventoParaEditar.getAssistencia().getNomeCompleto());
            }
        }
    }

    // ── Botão Concluído ───────────────────────────────────────────────────────

    private JButton criarBotaoConcluido() {
        JButton btn = new JButton("Concluído") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xF0F0F0) : COR_BRANCO);
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
                new LineBorder(COR_BORDA, 1, true), new EmptyBorder(8, 16, 8, 16)));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> aoConcluir());
        return btn;
    }

    private void aoConcluir() {
        limparErros();

        String tipo    = comboTipo.getSelectedIndex()    == 0 ? "" : (String) comboTipo.getSelectedItem();
        int    idxEq   = comboEquipa.getSelectedIndex();
        Equipa equipa  = idxEq > 0 ? equipasDisponiveis.get(idxEq - 1) : null;
        int    idxJog  = comboJogador.getSelectedIndex();
        Jogador jogador = idxJog > 0 ? jogadoresDisponiveis.get(idxJog - 1) : null;
        String minuto  = textoReal(campMinuto, "Minuto (0–130)...");

        // NOVO: Recolher a assistência se a combobox estiver visível (ou seja, se for Golo)
        int idxAssist = comboAssistencia.getSelectedIndex();
        Jogador assistencia = (comboAssistencia.isVisible() && idxAssist > 0) ? jogadoresDisponiveis.get(idxAssist - 1) : null;

        try {
            if (eventoParaEditar == null) {
                // UC10 — Registar (Adicionamos a 'assistencia' no final)
                eventoControlador.registarEvento(jogo, tipo, equipa, jogador, minuto, assistencia);
            } else {
                // UC12 — Corrigir (Adicionamos a 'assistencia' no final)
                eventoControlador.corrigirEvento(jogo, eventoParaEditar, tipo, equipa, jogador, minuto, assistencia);
            }
            if (aoAtualizar != null) aoAtualizar.run();
            dispose();

        } catch (IllegalStateException ex) {
            String msg = switch (ex.getMessage()) {
                case "JOGO_NAO_COMECADO" -> "O jogo ainda não foi iniciado.";
                case "PRAZO_EXPIRADO"    -> "O prazo de correcção de "
                        + EventoControlador.PRAZO_CORRECAO_HORAS + "h já expirou.";
                default -> ex.getMessage();
            };
            JOptionPane.showMessageDialog(this, msg, "Operação Bloqueada", JOptionPane.WARNING_MESSAGE);

        } catch (IllegalArgumentException ex) {
            tratarErro(ex.getMessage());
        }
    }

    // ── Tratamento de erros visuais ────────────────────────────────────────────

    private void tratarErro(String codigo) {
        switch (codigo) {
            case "CAMPO_TIPO_VAZIO", "TIPO_INVALIDO" -> {
                destacarComboErro(comboTipo);
                lblErroTipo.setText("Selecione o tipo de evento.");
            }
            case "CAMPO_EQUIPA_VAZIA" -> {
                destacarComboErro(comboEquipa);
                lblErroEquipa.setText("Selecione a equipa.");
            }
            case "CAMPO_JOGADOR_VAZIO", "JOGADOR_NAO_PERTENCE_EQUIPA" -> {
                destacarComboErro(comboJogador);
                lblErroJogador.setText("Selecione um jogador válido da equipa.");
            }
            case "JOGADOR_INAPTO" -> {
                destacarComboErro(comboJogador);
                lblErroJogador.setText("Jogador está inapto.");
            }
            case "JOGADOR_JA_EXPULSO" -> {
                destacarComboErro(comboJogador);
                lblErroJogador.setText("Jogador já foi expulso neste jogo.");
            }
            case "CAMPO_MINUTO_VAZIO", "MINUTO_INVALIDO" -> {
                destacarErro(campMinuto);
                lblErroMinuto.setText("Minuto inválido (0–130).");
            }
            default -> JOptionPane.showMessageDialog(this, codigo, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void destacarErro(JTextField tf) {
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_ERRO, 2, true), new EmptyBorder(6, 10, 6, 10)));
    }

    private void destacarComboErro(JComboBox<?> cb) {
        cb.setBorder(new LineBorder(COR_ERRO, 2, true));
    }

    private void limparErros() {
        comboTipo.setBorder(null);
        comboEquipa.setBorder(null);
        comboJogador.setBorder(null);
        campMinuto.setBorder(bordaCampo(COR_BORDA, 1));
        lblErroTipo.setText(" ");
        lblErroEquipa.setText(" ");
        lblErroJogador.setText(" ");
        lblErroMinuto.setText(" ");
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

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
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(COR_TEXTO); }
                tf.setBorder(bordaCampo(COR_FOCO, 1));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isBlank()) { tf.setText(placeholder); tf.setForeground(COR_PLACEHOLDER); }
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

    private static String[] prepararOpcoes(String primeiro, List<String> lista) {
        String[] arr = new String[lista.size() + 1];
        arr[0] = primeiro;
        for (int i = 0; i < lista.size(); i++) arr[i + 1] = lista.get(i);
        return arr;
    }

    private void selecionarCombo(JComboBox<String> cb, String valor) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).equalsIgnoreCase(valor)) { cb.setSelectedIndex(i); return; }
        }
    }

    private static javax.swing.border.Border bordaCampo(Color cor, int esp) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(cor, esp, true), new EmptyBorder(6, 10, 6, 10));
    }

    private static Box.Filler rigidH(int h) {
        return (Box.Filler) Box.createRigidArea(new Dimension(0, h));
    }

    private String textoReal(JTextField tf, String placeholder) {
        String t = tf.getText().trim();
        return t.equals(placeholder) ? "" : t;
    }
}
