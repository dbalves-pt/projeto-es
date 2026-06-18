package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EventoControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

import static pt.ipleiria.estg.dei.ei.esoft.vista.FormularioEquipa.*;

/**
 * Vista UC09 — Ecrã 'Detalhes do Jogo'.
 *
 * Protótipo: mostra dados do estádio, bancadas e lotação.
 * ⚠ O protótipo não apresentava o botão 'Iniciar Jogo' — este ecrã
 * adiciona-o conforme recomendado nos requisitos.
 *
 * Comportamento:
 *   • Jogo CALENDARIZADO → botão 'Iniciar Jogo' activo (com aviso se
 *     início muito antecipado — CA 5.2).
 *   • Jogo COMEÇADO ou TERMINADO → botão 'Iniciar Jogo' desactivado.
 *   • Botão 'Ver Estatísticas / Registar Eventos' disponível para jogos
 *     COMEÇADO ou TERMINADO, abrindo o PainelEstatisticasJogo.
 */
public class FormularioDetalhesJogo extends JDialog {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JogoControlador   jogoControlador;
    private final EventoControlador eventoControlador;
    private final Jogo              jogo;
    private final Runnable          aoAtualizar;

    public FormularioDetalhesJogo(Window owner,
                                   JogoControlador jogoControlador,
                                   EventoControlador eventoControlador,
                                   Jogo jogo,
                                   Runnable aoAtualizar) {
        super(owner, "Detalhes do Jogo", ModalityType.APPLICATION_MODAL);
        this.jogoControlador   = jogoControlador;
        this.eventoControlador = eventoControlador;
        this.jogo              = jogo;
        this.aoAtualizar       = aoAtualizar;
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
        raiz.add(criarPainelConteudo());
    }

    private JPanel criarPainelConteudo() {
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
        p.setPreferredSize(new Dimension(420, 400));
        p.setBorder(new EmptyBorder(22, 22, 22, 22));

        // ── Título ────────────────────────────────────────────────────────────
        JLabel titulo = new JLabel("Detalhes do Jogo");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(titulo);
        p.add(rigidH(18));

        // ── Confronto ─────────────────────────────────────────────────────────
        String confronto = jogo.getEquipaCasa().getNome() + " vs " + jogo.getEquipaFora().getNome();
        p.add(linhaInfo("Jogo:", confronto));
        p.add(rigidH(6));

        // ── Marcador (só visível se o jogo já começou) ────────────────────────
        if (jogo.getEstado() != Jogo.Estado.CALENDARIZADO) {
            String marcador = jogo.getGolosCasa() + " — " + jogo.getGolosFora();
            JLabel lblMarcador = new JLabel(marcador);
            lblMarcador.setFont(new Font("SansSerif", Font.BOLD, 28));
            lblMarcador.setForeground(COR_TEXTO);
            lblMarcador.setAlignmentX(LEFT_ALIGNMENT);
            p.add(lblMarcador);
            p.add(rigidH(8));
        }

        // ── Data e Hora ───────────────────────────────────────────────────────
        p.add(linhaInfo("Data / Hora:", jogo.getDataHora().format(FORMATO)));
        p.add(rigidH(6));

        // ── Estádio ───────────────────────────────────────────────────────────
        p.add(linhaInfo("Estádio:", jogo.getEstadio().getNome()
                + " — " + jogo.getEstadio().getCidade()));
        p.add(rigidH(6));

        // ── Fase ──────────────────────────────────────────────────────────────
        String fase = jogo.getGrupo() != null
                ? "Fase de Grupos — " + jogo.getGrupo().getNome()
                : jogo.getFase().toString();
        p.add(linhaInfo("Fase:", fase));
        p.add(rigidH(6));

        // ── Estado ───────────────────────────────────────────────────────────
        String descEstado = switch (jogo.getEstado()) {
            case CALENDARIZADO -> "Calendarizado";
            case COMECADO      -> "Em curso";
            case TERMINADO     -> "Terminado";
        };
        p.add(linhaInfo("Estado:", descEstado));
        p.add(rigidH(18));

        // ── Separador ────────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sep);
        p.add(rigidH(18));

        // ── Botão Iniciar Jogo (UC09) ─────────────────────────────────────────
        boolean podeIniciar = jogo.getEstado() == Jogo.Estado.CALENDARIZADO;
        JButton btnIniciar = criarBotao("Iniciar Jogo", new Color(0x1565C0), Color.WHITE);
        btnIniciar.setEnabled(podeIniciar);
        btnIniciar.addActionListener(e -> aoIniciarJogo());
        btnIniciar.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btnIniciar);
        p.add(rigidH(8));

        // ── Botão Ver Estatísticas / Registar Eventos (UC10/UC11/UC12) ────────
        boolean podeVerEstat = jogo.getEstado() != Jogo.Estado.CALENDARIZADO;
        String txtEstat = jogo.getEstado() == Jogo.Estado.COMECADO
                ? "Registar Eventos / Terminar Jogo"
                : "Ver Estatísticas / Corrigir Eventos";
        JButton btnEstat = criarBotao(txtEstat, new Color(0x2E7D32), Color.WHITE);
        btnEstat.setEnabled(podeVerEstat);
        btnEstat.addActionListener(e -> aoAbrirEstatisticas());
        btnEstat.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btnEstat);

        return p;
    }

    // ── Acções ────────────────────────────────────────────────────────────────

    private void aoIniciarJogo() {
        // CA 5.2 — Início muito antecipado: pede confirmação
        if (jogoControlador.isInicioMuitoAntecipado(jogo)) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "O jogo está agendado para " + jogo.getDataHora().format(FORMATO)
                    + ".\nTem a certeza que quer iniciar o jogo com tanta antecedência?",
                    "Início Antecipado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (resposta != JOptionPane.YES_OPTION) return;
        }

        try {
            jogoControlador.iniciarJogo(jogo);
            if (aoAtualizar != null) aoAtualizar.run();
            dispose();
            // Abre automaticamente o ecrã de estatísticas
            PainelEstatisticasJogo painel = new PainelEstatisticasJogo(
                    (Window) getParent(), jogoControlador, eventoControlador, jogo, aoAtualizar);
            painel.setVisible(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    "Não é possível iniciar o jogo: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aoAbrirEstatisticas() {
        dispose();
        PainelEstatisticasJogo painel = new PainelEstatisticasJogo(
                (Window) getParent(), jogoControlador, eventoControlador, jogo, aoAtualizar);
        painel.setVisible(true);
    }

    // ── Utilitários de construção ─────────────────────────────────────────────

    private JPanel linhaInfo(String rotulo, String valor) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        linha.setOpaque(false);
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblRotulo = new JLabel(rotulo);
        lblRotulo.setFont(FONTE_HEADER);
        lblRotulo.setForeground(COR_TEXTO);
        linha.add(lblRotulo);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(FONTE_NORMAL);
        lblValor.setForeground(new Color(0x424242));
        linha.add(lblValor);
        return linha;
    }

    private JButton criarBotao(String texto, Color fundo, Color texto2) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled()
                        ? (getModel().isRollover() ? fundo.darker() : fundo)
                        : COR_CINZENTO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONTE_NORMAL);
        btn.setForeground(texto2);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static Box.Filler rigidH(int h) {
        return (Box.Filler) Box.createRigidArea(new Dimension(0, h));
    }
}
