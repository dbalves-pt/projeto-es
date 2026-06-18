package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EventoControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.EventoJogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static pt.ipleiria.estg.dei.ei.esoft.vista.FormularioEquipa.*;

/**
 * Vista UC10 (Registar Evento de Jogo), UC11 (Terminar Jogo) e
 * UC12 (Corrigir Evento de Jogo).
 *
 * Layout:
 *   • Cabeçalho: marcador + nome das equipas.
 *   • Tabela de eventos registados (Minuto, Tipo, Equipa, Jogador, [Editar]).
 *   • Botão 'Novo Evento' (apenas se jogo COMEÇADO — UC10).
 *   • Botão 'Terminar Jogo' (apenas se jogo COMEÇADO — UC11).
 *   • Nota de prazo de correcção (UC12).
 *
 * ⚠ Conforme os avisos do protótipo, este ecrã adiciona os controlos
 *   'Novo Evento', 'Registar Evento', 'Terminar Jogo' e os botões
 *   'Editar' por linha de evento, ausentes no protótipo original.
 */
public class PainelEstatisticasJogo extends JDialog {

    private final JogoControlador   jogoControlador;
    private final EventoControlador eventoControlador;
    private final Jogo              jogo;
    private final Runnable          aoAtualizar;

    private JLabel              lblMarcador;
    private DefaultTableModel   modeloEventos;
    private JTable              tabelaEventos;
    private JButton             btnNovoEvento;
    private JButton             btnTerminar;
    private List<EventoJogo>    eventosListados;

    public PainelEstatisticasJogo(Window owner,
                                   JogoControlador jogoControlador,
                                   EventoControlador eventoControlador,
                                   Jogo jogo,
                                   Runnable aoAtualizar) {
        super(owner, "Estatísticas do Jogo", ModalityType.APPLICATION_MODAL);
        this.jogoControlador   = jogoControlador;
        this.eventoControlador = eventoControlador;
        this.jogo              = jogo;
        this.aoAtualizar       = aoAtualizar;
        construirUI();
        atualizar();
        setSize(680, 560);
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Construção da UI
    // ══════════════════════════════════════════════════════════════════════════

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout(0, 0));
        raiz.setBackground(COR_FUNDO);
        setContentPane(raiz);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarPainelCentral(), BorderLayout.CENTER);
        raiz.add(criarRodape(), BorderLayout.SOUTH);
    }

    // ── Cabeçalho: marcador ───────────────────────────────────────────────────

    private JPanel criarCabecalho() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(0x1565C0));
        p.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel interno = new JPanel(new BorderLayout(20, 0));
        interno.setOpaque(false);

        JLabel lblCasa = new JLabel(jogo.getEquipaCasa().getNome(), SwingConstants.RIGHT);
        lblCasa.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCasa.setForeground(Color.WHITE);

        lblMarcador = new JLabel("0 — 0", SwingConstants.CENTER);
        lblMarcador.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblMarcador.setForeground(Color.WHITE);
        lblMarcador.setPreferredSize(new Dimension(120, 40));

        JLabel lblFora = new JLabel(jogo.getEquipaFora().getNome(), SwingConstants.LEFT);
        lblFora.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblFora.setForeground(Color.WHITE);

        interno.add(lblCasa,    BorderLayout.WEST);
        interno.add(lblMarcador, BorderLayout.CENTER);
        interno.add(lblFora,    BorderLayout.EAST);
        p.add(interno);
        return p;
    }

    // ── Centro: tabela de eventos ─────────────────────────────────────────────

    private JPanel criarPainelCentral() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COR_BRANCO);
        p.setBorder(new EmptyBorder(16, 24, 8, 24));

        JLabel lblTitulo = new JLabel("Eventos do Jogo");
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(lblTitulo, BorderLayout.NORTH);

        String[] colunas = {"Min.", "Tipo", "Equipa", "Jogador", "Editar"};
        modeloEventos = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 4; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 4 ? JButton.class : String.class;
            }
        };

        tabelaEventos = new JTable(modeloEventos);
        tabelaEventos.setRowHeight(28);
        tabelaEventos.setShowGrid(false);
        tabelaEventos.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabelaEventos.getTableHeader().setFont(FONTE_HEADER);
        tabelaEventos.setFont(FONTE_NORMAL);
        tabelaEventos.getColumnModel().getColumn(4).setMaxWidth(70);
        tabelaEventos.getColumnModel().getColumn(4).setMinWidth(70);
        tabelaEventos.getColumnModel().getColumn(0).setMaxWidth(50);

        // Renderizador e editor para o botão "Editar" na coluna 4
        tabelaEventos.getColumnModel().getColumn(4).setCellRenderer(new BotaoRenderer());
        tabelaEventos.getColumnModel().getColumn(4).setCellEditor(
                new BotaoEditor(new JCheckBox(), this::editarEventoDaLinha));

        // Seleção de linha também activa o botão
        tabelaEventos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tabelaEventos.columnAtPoint(e.getPoint());
                if (col == 4) {
                    int linha = tabelaEventos.rowAtPoint(e.getPoint());
                    editarEventoDaLinha(linha);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabelaEventos);
        scroll.setBorder(new LineBorder(COR_BORDA, 1));
        scroll.getViewport().setBackground(COR_BRANCO);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── Rodapé: botões de acção ───────────────────────────────────────────────

    private JPanel criarRodape() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        p.setBackground(COR_BRANCO);
        p.setBorder(new EmptyBorder(0, 12, 8, 12));

        btnNovoEvento = criarBotaoAcao("Novo Evento", new Color(0x1565C0));
        btnNovoEvento.addActionListener(e -> abrirFormularioNovoEvento());
        p.add(btnNovoEvento);

        btnTerminar = criarBotaoAcao("Terminar Jogo", new Color(0xC62828));
        btnTerminar.addActionListener(e -> aoTerminarJogo());
        p.add(btnTerminar);

        JButton btnFechar = criarBotaoAcao("Fechar", new Color(0x546E7A));
        btnFechar.addActionListener(e -> dispose());
        p.add(btnFechar);

        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Actualização
    // ══════════════════════════════════════════════════════════════════════════

    public void atualizar() {
        // Marcador
        lblMarcador.setText(jogo.getGolosCasa() + " — " + jogo.getGolosFora());

        // Tabela de eventos
        modeloEventos.setRowCount(0);
        eventosListados = eventoControlador.getEventos(jogo);
        for (EventoJogo ev : eventosListados) {
            boolean podeEditar = jogo.getEstado() == Jogo.Estado.TERMINADO
                    && eventoControlador.podeCorrigir(ev);
            modeloEventos.addRow(new Object[]{
                    ev.getMinuto() + "'",
                    ev.getTipo().toString(),
                    ev.getEquipa().getNome(),
                    ev.getJogador().getNomeCompleto(),
                    podeEditar ? "Editar" : (ev.isCorrigido() ? "✓" : "—")
            });
        }

        // Botões
        boolean emCurso = jogo.getEstado() == Jogo.Estado.COMECADO;
        btnNovoEvento.setEnabled(emCurso);
        btnTerminar.setEnabled(emCurso);

        repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC10 — Novo Evento
    // ══════════════════════════════════════════════════════════════════════════

    private void abrirFormularioNovoEvento() {
        FormularioEvento form = new FormularioEvento(this, eventoControlador, jogo, null, () -> {
            atualizar();
            if (aoAtualizar != null) aoAtualizar.run();
        });
        form.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC11 — Terminar Jogo
    // ══════════════════════════════════════════════════════════════════════════

    private void aoTerminarJogo() {
        // CA 3.2 — Inconsistência nos eventos: alerta
        if (jogoControlador.haInconsistenciaNosEventos(jogo)) {
            int r = JOptionPane.showConfirmDialog(this,
                    "Existem inconsistências entre os eventos registados e o marcador.\n"
                    + "Tem a certeza que pretende terminar o jogo?",
                    "Inconsistência Detectada", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }

        String resumo = "Resultado final: "
                + jogo.getEquipaCasa().getNome() + " " + jogo.getGolosCasa()
                + " — " + jogo.getGolosFora() + " " + jogo.getEquipaFora().getNome()
                + "\nEventos registados: " + jogo.getEventos().size()
                + "\n\nConfirma o fim do jogo?";

        int resposta = JOptionPane.showConfirmDialog(this, resumo,
                "Confirmar Fim de Jogo", JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) return;

        try {
            List<Equipa> apuradas = jogoControlador.terminarJogo(jogo);
            atualizar();
            if (aoAtualizar != null) aoAtualizar.run();

            // CA 8.1 — Empate técnico / apuramento
            if (!apuradas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Todos os jogos do " + jogo.getGrupo().getNome() + " terminaram!\n"
                        + "Equipas apuradas:\n"
                        + "  1.º: " + apuradas.get(0).getNome() + "\n"
                        + "  2.º: " + apuradas.get(1).getNome(),
                        "Fase de Grupos Concluída", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível terminar o jogo: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC12 — Editar Evento
    // ══════════════════════════════════════════════════════════════════════════

    private void editarEventoDaLinha(int linha) {
        if (linha < 0 || eventosListados == null || linha >= eventosListados.size()) return;
        EventoJogo evento = eventosListados.get(linha);

        if (!eventoControlador.podeCorrigir(evento)) {
            JOptionPane.showMessageDialog(this,
                    "O prazo de " + EventoControlador.PRAZO_CORRECAO_HORAS
                    + " horas para corrigir este evento já expirou.",
                    "Prazo Expirado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FormularioEvento form = new FormularioEvento(this, eventoControlador, jogo, evento, () -> {
            atualizar();
            if (aoAtualizar != null) aoAtualizar.run();
        });
        form.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Utilitários de UI
    // ══════════════════════════════════════════════════════════════════════════

    private JButton criarBotaoAcao(String texto, Color cor) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (getModel().isRollover() ? cor.darker() : cor) : COR_CINZENTO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONTE_NORMAL);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Renderizador de botão na tabela (coluna Editar) ───────────────────────

    private static class BotaoRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        BotaoRenderer() {
            setOpaque(true);
            setFont(new Font("SansSerif", Font.PLAIN, 11));
            setBorder(new EmptyBorder(3, 6, 3, 6));
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String txt = value == null ? "" : value.toString();
            setText(txt);
            boolean activo = "Editar".equals(txt);
            setBackground(activo ? new Color(0x1565C0) : new Color(0xEEEEEE));
            setForeground(activo ? Color.WHITE : new Color(0x757575));
            return this;
        }
    }

    // ── Editor de botão na tabela (coluna Editar) ─────────────────────────────

    private static class BotaoEditor extends DefaultCellEditor {
        private final java.util.function.IntConsumer acaoLinha;
        private int linhaAtual;
        BotaoEditor(JCheckBox checkBox, java.util.function.IntConsumer acaoLinha) {
            super(checkBox);
            this.acaoLinha = acaoLinha;
            setClickCountToStart(1);
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.linhaAtual = row;
            JButton btn = new JButton(value == null ? "" : value.toString());
            btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            btn.addActionListener(e -> {
                stopCellEditing();
                acaoLinha.accept(linhaAtual);
            });
            return btn;
        }
        @Override public Object getCellEditorValue() { return "Editar"; }
    }
}
