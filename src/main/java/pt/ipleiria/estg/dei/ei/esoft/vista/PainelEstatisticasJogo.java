package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EventoControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.EventoJogo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static pt.ipleiria.estg.dei.ei.esoft.vista.FormularioEquipa.*;

public class PainelEstatisticasJogo extends JDialog {

    private final JogoControlador   jogoControlador;
    private final EventoControlador eventoControlador;
    private final Jogo              jogo;
    private final Runnable          aoAtualizar;

    private JLabel              lblMarcador;

    // Gestão de Cartões (Páginas)
    private CardLayout          cardLayout;
    private JPanel              painelCartoes;

    // Tabela Página 1 (Eventos)
    private DefaultTableModel   modeloEventos;
    private JTable              tabelaEventos;
    private List<EventoJogo>    eventosListados;

    // Tabela Página 2 (Resumo)
    private DefaultTableModel   modeloResumo;
    private JTable              tabelaResumo;

    // Botões
    private JButton             btnNovoEvento;
    private JButton             btnTerminar;
    private JButton             btnMudarVista;
    private boolean             vistaEventos = true;

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

        setSize(720, 580);
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

        // O Centro agora usa o CardLayout para ter 2 páginas
        cardLayout = new CardLayout();
        painelCartoes = new JPanel(cardLayout);
        painelCartoes.setBackground(COR_BRANCO);

        painelCartoes.add(criarPainelEventos(), "EVENTOS");
        painelCartoes.add(criarPainelResumo(), "RESUMO");

        raiz.add(painelCartoes, BorderLayout.CENTER);
        raiz.add(criarRodape(), BorderLayout.SOUTH);
    }

    private JPanel criarCabecalho() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(0x1565C0));
        p.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel interno = new JPanel(new BorderLayout(20, 0));
        interno.setOpaque(false);

        JLabel lblCasa = new JLabel(jogo.getEquipaCasa().getNome(), SwingConstants.RIGHT);
        lblCasa.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblCasa.setForeground(Color.WHITE);

        lblMarcador = new JLabel("0 — 0", SwingConstants.CENTER);
        lblMarcador.setFont(new Font("SansSerif", Font.BOLD, 36));
        lblMarcador.setForeground(Color.WHITE);
        lblMarcador.setPreferredSize(new Dimension(120, 40));

        JLabel lblFora = new JLabel(jogo.getEquipaFora().getNome(), SwingConstants.LEFT);
        lblFora.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblFora.setForeground(Color.WHITE);

        interno.add(lblCasa,    BorderLayout.WEST);
        interno.add(lblMarcador, BorderLayout.CENTER);
        interno.add(lblFora,    BorderLayout.EAST);
        p.add(interno);
        return p;
    }

    // ── PÁGINA 1: Tabela de Eventos ───────────────────────────────────────────
    private JPanel criarPainelEventos() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xB3E5FC)); // Azul clarinho do teu design
        p.setBorder(new EmptyBorder(16, 24, 8, 24));

        JLabel lblTitulo = new JLabel("Eventos do Jogo");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(lblTitulo, BorderLayout.NORTH);

        // NOVA COLUNA "REMOVER"
        String[] colunas = {"Min.", "Tipo", "Equipa", "Jogador", "Editar", "Remover"};
        modeloEventos = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 4 || col == 5; }
        };

        tabelaEventos = new JTable(modeloEventos);
        tabelaEventos.setRowHeight(28);
        tabelaEventos.setShowGrid(false);
        tabelaEventos.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabelaEventos.getTableHeader().setFont(FONTE_HEADER);
        tabelaEventos.setFont(FONTE_NORMAL);

        tabelaEventos.getColumnModel().getColumn(4).setMaxWidth(70);
        tabelaEventos.getColumnModel().getColumn(5).setMaxWidth(80);
        tabelaEventos.getColumnModel().getColumn(0).setMaxWidth(50);

        tabelaEventos.getColumnModel().getColumn(4).setCellRenderer(new BotaoRenderer(new Color(0x1565C0)));
        tabelaEventos.getColumnModel().getColumn(4).setCellEditor(new BotaoEditor(new JCheckBox(), this::editarEventoDaLinha, "Editar"));

        tabelaEventos.getColumnModel().getColumn(5).setCellRenderer(new BotaoRenderer(new Color(0xC62828))); // Vermelho para remover
        tabelaEventos.getColumnModel().getColumn(5).setCellEditor(new BotaoEditor(new JCheckBox(), this::removerEventoDaLinha, "Remover"));

        JScrollPane scroll = new JScrollPane(tabelaEventos);
        scroll.setBorder(new LineBorder(Color.WHITE, 10, true)); // Fundo branco arredondado na lista
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── PÁGINA 2: Resumo de Estatísticas ──────────────────────────────────────
    private JPanel criarPainelResumo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xB3E5FC));
        p.setBorder(new EmptyBorder(16, 24, 8, 24));

        JLabel lblTitulo = new JLabel("Estatísticas do Jogo");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(lblTitulo, BorderLayout.NORTH);

        String eqA = jogo.getEquipaCasa().getNome();
        String eqB = jogo.getEquipaFora().getNome();

        modeloResumo = new DefaultTableModel(new String[]{eqA, "—", eqB}, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                // Bloqueia a coluna do meio (nomes das estatísticas)
                if (col == 1) return false;

                // Só permite editar Posse(0), Remates(2) e Passes(6), se o botão "Novo Evento" estiver ativo
                return btnNovoEvento.isEnabled() && (row == 0 || row == 2 || row == 6);
            }

            @Override public void setValueAt(Object aValue, int row, int col) {
                try {
                    // Remove símbolos (como o "%") para apanhar só o número digitado
                    int valor = Integer.parseInt(aValue.toString().replaceAll("[^0-9]", ""));

                    if (row == 0) { // Lógica da Posse de Bola
                        if (valor > 100) valor = 100;
                        if (valor < 0) valor = 0;

                        if (col == 0) {
                            jogo.setPosseBolaEquipa1(valor);
                            jogo.setPosseBolaEquipa2(100 - valor); // A outra equipa fica com o resto
                        } else {
                            jogo.setPosseBolaEquipa2(valor);
                            jogo.setPosseBolaEquipa1(100 - valor); // A outra equipa fica com o resto
                        }
                    } else if (row == 2) { // Remates
                        if (col == 0) jogo.setRematesEquipa1(valor);
                        else jogo.setRematesEquipa2(valor);
                    } else if (row == 6) { // Passes
                        if (col == 0) jogo.setPassesEquipa1(valor);
                        else jogo.setPassesEquipa2(valor);
                    }

                    // Força a tabela a atualizar-se visualmente após inserires o valor
                    SwingUtilities.invokeLater(() -> atualizarResumoEstatisticas());

                } catch (Exception ignored) { }
            }
        };

        tabelaResumo = new JTable(modeloResumo);
        tabelaResumo.setRowHeight(35);
        tabelaResumo.setShowGrid(false);
        tabelaResumo.setFont(new Font("SansSerif", Font.BOLD, 14));

        tabelaResumo.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(new Color(0x9E9E9E));
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));
                return lbl;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<3; i++) tabelaResumo.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(tabelaResumo);
        scroll.setBorder(new LineBorder(Color.WHITE, 10, true));
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── Rodapé ────────────────────────────────────────────────────────────────
    private JPanel criarRodape() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xB3E5FC));
        p.setBorder(new EmptyBorder(0, 24, 16, 24));

        JPanel pBotoesEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pBotoesEsquerda.setOpaque(false);

        btnNovoEvento = criarBotaoAcao("Novo Evento", new Color(0x1565C0));
        btnNovoEvento.addActionListener(e -> abrirFormularioNovoEvento());
        pBotoesEsquerda.add(btnNovoEvento);

        btnTerminar = criarBotaoAcao("Terminar Jogo", new Color(0xC62828));
        btnTerminar.addActionListener(e -> aoTerminarJogo());
        pBotoesEsquerda.add(btnTerminar);

        JPanel pBotoesDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBotoesDireita.setOpaque(false);

        // Botão para navegar entre as vistas
        btnMudarVista = criarBotaoAcao("Ver Resumo ►", new Color(0x00695C));
        btnMudarVista.addActionListener(e -> alternarVista());
        pBotoesDireita.add(btnMudarVista);

        JButton btnFechar = criarBotaoAcao("Fechar", Color.WHITE);
        btnFechar.setForeground(Color.BLACK); // O botão de fechar branco do teu design
        btnFechar.addActionListener(e -> dispose());
        pBotoesDireita.add(btnFechar);

        p.add(pBotoesEsquerda, BorderLayout.WEST);
        p.add(pBotoesDireita, BorderLayout.EAST);

        return p;
    }

    private void alternarVista() {
        if (vistaEventos) {
            cardLayout.show(painelCartoes, "RESUMO");
            btnMudarVista.setText("◄ Voltar aos Eventos");
        } else {
            cardLayout.show(painelCartoes, "EVENTOS");
            btnMudarVista.setText("Ver Resumo ►");
        }
        vistaEventos = !vistaEventos;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Actualização de Dados
    // ══════════════════════════════════════════════════════════════════════════

    public void atualizar() {
        lblMarcador.setText(jogo.getGolosCasa() + " — " + jogo.getGolosFora());

        boolean terminado = jogo.getEstado() == Jogo.Estado.TERMINADO;
        boolean emCurso = jogo.getEstado() == Jogo.Estado.COMECADO;

        // Regra de inserção: Em curso ou (Terminado e dentro das 24h)
        boolean podeModificarTerminado = terminado && jogo.getHoraFim() != null
                && ChronoUnit.HOURS.between(jogo.getHoraFim(), LocalDateTime.now()) < EventoControlador.PRAZO_CORRECAO_HORAS;

        btnNovoEvento.setEnabled(emCurso || podeModificarTerminado);
        btnTerminar.setEnabled(emCurso);

        // Atualizar Tabela 1 (Eventos)
        modeloEventos.setRowCount(0);
        eventosListados = eventoControlador.getEventos(jogo);
        for (EventoJogo ev : eventosListados) {
            boolean podeEditar = emCurso || (terminado && eventoControlador.podeCorrigir(ev));

            modeloEventos.addRow(new Object[]{
                    ev.getMinuto() + "'",
                    ev.getTipo().toString(),
                    ev.getEquipa().getNome(),
                    ev.getJogador().getNomeCompleto(),
                    podeEditar ? "Editar" : "—",
                    podeEditar ? "X" : "—"
            });
        }

        // Atualizar Tabela 2 (Resumo Total)
        atualizarResumoEstatisticas();
        repaint();
    }

    private void atualizarResumoEstatisticas() {
        int[] golos = {0,0}, defesas = {0,0};
        int[] amarelo = {0,0}, vermelho = {0,0};

        Equipa casa = jogo.getEquipaCasa();

        for (EventoJogo ev : jogo.getEventos()) {
            int i = (ev.getEquipa() == casa) ? 0 : 1;
            switch(ev.getTipo()) {
                case GOLO -> golos[i]++;
                case DEFESA -> defesas[i]++;
                case CARTAO_AMARELO -> amarelo[i]++;
                case CARTAO_VERMELHO -> vermelho[i]++;
                default -> {}
            }
        }

        modeloResumo.setRowCount(0); // Limpa as linhas

        // Recria as 7 linhas misturando os dados do Controlador e os do Jogo
        modeloResumo.addRow(new Object[]{jogo.getPosseBolaEquipa1() + "%", "Posse de Bola", jogo.getPosseBolaEquipa2() + "%"});
        modeloResumo.addRow(new Object[]{golos[0], "Golos", golos[1]});
        modeloResumo.addRow(new Object[]{jogo.getRematesEquipa1(), "Remates", jogo.getRematesEquipa2()});
        modeloResumo.addRow(new Object[]{defesas[0], "Defesas (Baliza)", defesas[1]});
        modeloResumo.addRow(new Object[]{amarelo[0], "Cartões Amarelos", amarelo[1]});
        modeloResumo.addRow(new Object[]{vermelho[0], "Cartões Vermelhos", vermelho[1]});
        modeloResumo.addRow(new Object[]{jogo.getPassesEquipa1(), "Passes", jogo.getPassesEquipa2()});
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Acções de Controlador
    // ══════════════════════════════════════════════════════════════════════════

    private void abrirFormularioNovoEvento() {
        FormularioEvento form = new FormularioEvento(this, eventoControlador, jogo, null, () -> {
            atualizar();
            if (aoAtualizar != null) aoAtualizar.run();
        });
        form.setVisible(true);
    }

    private void aoTerminarJogo() {
        if (jogoControlador.haInconsistenciaNosEventos(jogo)) {
            int r = JOptionPane.showConfirmDialog(this, "Existem inconsistências. Terminar na mesma?", "Aviso", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
        }

        int resposta = JOptionPane.showConfirmDialog(this, "Confirma o fim do jogo?", "Terminar", JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) return;

        try {
            jogoControlador.terminarJogo(jogo);
            atualizar();
            if (aoAtualizar != null) aoAtualizar.run();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarEventoDaLinha(int linha) {
        if (linha < 0 || eventosListados == null || linha >= eventosListados.size()) return;
        EventoJogo evento = eventosListados.get(linha);

        if (jogo.getEstado() == Jogo.Estado.TERMINADO && !eventoControlador.podeCorrigir(evento)) {
            JOptionPane.showMessageDialog(this, "O prazo de edição expirou.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FormularioEvento form = new FormularioEvento(this, eventoControlador, jogo, evento, () -> {
            atualizar();
            if (aoAtualizar != null) aoAtualizar.run();
        });
        form.setVisible(true);
    }

    private void removerEventoDaLinha(int linha) {
        if (linha < 0 || eventosListados == null || linha >= eventosListados.size()) return;
        EventoJogo evento = eventosListados.get(linha);

        if (jogo.getEstado() == Jogo.Estado.TERMINADO && !eventoControlador.podeCorrigir(evento)) {
            JOptionPane.showMessageDialog(this, "O prazo para remover expirou.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int r = JOptionPane.showConfirmDialog(this, "Tem a certeza que deseja remover este evento (" + evento.getTipo() + ")?", "Remover Evento", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            // Chamamos o método que criaste no passo anterior!
            eventoControlador.removerEvento(jogo, evento);
            atualizar();
            if (aoAtualizar != null) aoAtualizar.run();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Utilitários e Renderizadores
    // ══════════════════════════════════════════════════════════════════════════

    private JButton criarBotaoAcao(String texto, Color cor) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? cor : Color.GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONTE_NORMAL);
        btn.setForeground(cor == Color.WHITE ? Color.BLACK : Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class BotaoRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        private final Color corFundo;
        BotaoRenderer(Color cor) {
            this.corFundo = cor;
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER); // Centrar o texto
            setFont(new Font("SansSerif", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String txt = value == null ? "" : value.toString();
            setText(txt);

            boolean activo = !"—".equals(txt);
            // Se estiver ativo, usa a cor (azul ou vermelho). Se não, fica cinza claro.
            setBackground(activo ? corFundo : new Color(0xEEEEEE));
            setForeground(activo ? Color.WHITE : new Color(0x757575));
            return this;
        }
    }

    private static class BotaoEditor extends DefaultCellEditor {
        private final java.util.function.IntConsumer acaoLinha;
        private final String label;
        private int linhaAtual;
        BotaoEditor(JCheckBox checkBox, java.util.function.IntConsumer acaoLinha, String label) {
            super(checkBox);
            this.acaoLinha = acaoLinha;
            this.label = label;
            setClickCountToStart(1);
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.linhaAtual = row;
            JButton btn = new JButton(value == null ? "" : value.toString());
            btn.setFont(new Font("SansSerif", Font.BOLD, 11));
            btn.addActionListener(e -> {
                stopCellEditing();
                acaoLinha.accept(linhaAtual);
            });
            return btn;
        }
        @Override public Object getCellEditorValue() { return label; }
    }
}