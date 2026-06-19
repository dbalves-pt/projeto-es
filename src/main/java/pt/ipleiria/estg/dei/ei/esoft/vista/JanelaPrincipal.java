package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.*;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Janela de arranque — ecrã 'Criar Torneio' (UC07).
 * Após gerar o torneio, abre a dashboard com os separadores principais.
 * Após validar e iniciar o torneio (UC08), o separador 'Calendário'
 * fica totalmente funcional para UC09-UC12.
 */
public class JanelaPrincipal extends JFrame {

    // ── Controladores ─────────────────────────────────────────────────────────
    private final EquipaControlador    equipaControlador;
    private final JogadorControlador   jogadorControlador;
    private final EstadioControlador   estadioControlador;
    private final TorneioControlador   torneioControlador;
    private final JogoControlador      jogoControlador;
    private final EventoControlador    eventoControlador;
    private final BilheteControlador bilheteControlador;
    private final PatrocinioControlador patrocinioControlador;
    private final FinanceiroControlador financeiroControlador;



    // ── Referência ao painel de estatísticas do torneio (para atualizar ao mudar de aba) ──
    private PainelEstatisticasTorneio painelEstatisticas;
    private int indiceJogadores;

    // ── Componentes do ecrã de criação ────────────────────────────────────────
    private DefaultListModel<Equipa> listModel;
    private JList<Equipa>            listaEquipas;
    private JTextField               campDataInicio;
    private JTextField               campDataFim;
    private JTextField               campDiasDescanso;
    private JLabel                   lblErroDataInicio;
    private JLabel                   lblErroDataFim;
    private JLabel                   lblErroDias;

    // ── Referência ao painel do calendário (para refrescar após acções) ────────
    private PainelCalendario painelCalendario;

    private static final Color COR_FUNDO_AZUL      = new Color(0x9FE2FC);
    private static final Color COR_FUNDO_EXTERIOR  = new Color(0xF5F5F5);
    private static final Color COR_BRANCO          = Color.WHITE;
    private static final Color COR_CINZENTO_HEADER = new Color(0x9E9E9E);

    public JanelaPrincipal(EquipaControlador equipaControlador) {
        this.equipaControlador  = equipaControlador;
        this.jogadorControlador = new JogadorControlador();
        this.estadioControlador = new EstadioControlador();
        this.torneioControlador = new TorneioControlador();
        this.jogoControlador    = new JogoControlador();
        this.eventoControlador  = new EventoControlador();
        this.bilheteControlador = new BilheteControlador();
        this.patrocinioControlador = new PatrocinioControlador();
        this.financeiroControlador = new FinanceiroControlador(bilheteControlador, patrocinioControlador);
        construirUI();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Ecrã inicial — Criar Torneio (UC07)
    // ══════════════════════════════════════════════════════════════════════════

    private void construirUI() {
        setTitle("Gestão do Campeonato do Mundo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        JPanel painelFundo = new JPanel(new GridBagLayout());
        painelFundo.setBackground(COR_FUNDO_EXTERIOR);
        setContentPane(painelFundo);
        painelFundo.add(criarCartaoCriarTorneio());

        atualizarLista();
    }

    private JPanel criarCartaoCriarTorneio() {
        JPanel painel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_FUNDO_AZUL);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new Color(0x3498DB));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(400, 580));
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título
        JPanel painelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelTitulo.setOpaque(false);
        painelTitulo.setMaximumSize(new Dimension(380, 30));
        JLabel lblTitulo = new JLabel("Criar Torneio");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        painelTitulo.add(lblTitulo);
        painel.add(painelTitulo);
        painel.add(Box.createVerticalStrut(15));

        // Data de Início
        lblErroDataInicio = criarLabelErro();
        campDataInicio    = criarCampoData("Data de Início (dd/MM/yyyy)...", lblErroDataInicio);
        painel.add(campDataInicio);
        painel.add(lblErroDataInicio);
        painel.add(Box.createVerticalStrut(4));

        // Data de Fim
        lblErroDataFim = criarLabelErro();
        campDataFim    = criarCampoData("Data de Fim (dd/MM/yyyy)...", lblErroDataFim);
        painel.add(campDataFim);
        painel.add(lblErroDataFim);
        painel.add(Box.createVerticalStrut(4));

        // Dias de Descanso
        lblErroDias       = criarLabelErro();
        campDiasDescanso  = criarCampoData("Dias de descanso entre jogos (ex: 2)...", lblErroDias);
        painel.add(campDiasDescanso);
        painel.add(lblErroDias);
        painel.add(Box.createVerticalStrut(10));

        // Lista de Equipas
        JPanel painelLista = new JPanel(new BorderLayout());
        painelLista.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        painelLista.setMaximumSize(new Dimension(380, 200));

        JLabel lblHeaderEquipas = new JLabel("  Equipas");
        lblHeaderEquipas.setOpaque(true);
        lblHeaderEquipas.setBackground(COR_CINZENTO_HEADER);
        lblHeaderEquipas.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblHeaderEquipas.setPreferredSize(new Dimension(380, 25));
        painelLista.add(lblHeaderEquipas, BorderLayout.NORTH);

        listModel    = new DefaultListModel<>();
        listaEquipas = new JList<>(listModel);
        listaEquipas.setBorder(new EmptyBorder(5, 5, 5, 5));
        listaEquipas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaEquipas.getSelectedValue() != null) {
                FormularioEquipa form = new FormularioEquipa(this, equipaControlador,
                        jogadorControlador, listaEquipas.getSelectedValue(), this::atualizarLista);
                form.setVisible(true);
                listaEquipas.clearSelection();
            }
        });
        painelLista.add(new JScrollPane(listaEquipas), BorderLayout.CENTER);

        JButton btnInserir = new JButton("Inserir Equipa...");
        btnInserir.setContentAreaFilled(false);
        btnInserir.setBorder(new EmptyBorder(5, 5, 5, 5));
        btnInserir.setHorizontalAlignment(SwingConstants.LEFT);
        btnInserir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnInserir.addActionListener(e -> {
            FormularioEquipa form = new FormularioEquipa(this, equipaControlador,
                    jogadorControlador, this::atualizarLista);
            form.setVisible(true);
        });
        painelLista.add(btnInserir, BorderLayout.SOUTH);
        painel.add(painelLista);
        painel.add(Box.createVerticalStrut(15));

        // Botão Gerar Torneio
        JButton btnGerar = criarBotaoFigma("Gerar torneio");
        btnGerar.setMaximumSize(new Dimension(380, 38));
        btnGerar.addActionListener(e -> validarEGerarTorneio());
        painel.add(btnGerar);

        return painel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Lógica UC07 — Gerar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    private void validarEGerarTorneio() {
        String strInicio = campDataInicio.getText().trim();
        String strFim    = campDataFim.getText().trim();
        String strDias   = campDiasDescanso.getText().trim();

        final String PLACEHOLDER_INICIO = "Data de Início (dd/MM/yyyy)...";
        final String PLACEHOLDER_FIM    = "Data de Fim (dd/MM/yyyy)...";
        final String PLACEHOLDER_DIAS   = "Dias de descanso entre jogos (ex: 2)...";

        if (strInicio.equals(PLACEHOLDER_INICIO)) strInicio = "";
        if (strFim.equals(PLACEHOLDER_FIM))       strFim    = "";
        if (strDias.equals(PLACEHOLDER_DIAS))      strDias   = "";

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataInicio, dataFim;
        int diasDescanso;

        try {
            dataInicio = LocalDate.parse(strInicio, fmt);
        } catch (DateTimeParseException ex) {
            lblErroDataInicio.setText("Formato inválido. Ex: 01/06/2026");
            return;
        }
        try {
            dataFim = LocalDate.parse(strFim, fmt);
        } catch (DateTimeParseException ex) {
            lblErroDataFim.setText("Formato inválido. Ex: 30/06/2026");
            return;
        }
        try {
            diasDescanso = Integer.parseInt(strDias.isBlank() ? "1" : strDias);
            if (diasDescanso < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            lblErroDias.setText("Introduza um número positivo.");
            return;
        }

        lblErroDataInicio.setText(" ");
        lblErroDataFim.setText(" ");
        lblErroDias.setText(" ");

        try {
            torneioControlador.configurarTorneio(dataInicio, dataFim, diasDescanso);
            abrirDashboard();
        } catch (IllegalArgumentException ex) {
            String msg = switch (ex.getMessage()) {
                case "DATAS_INVALIDAS"      -> "A data de fim não pode ser anterior à de início.";
                case "EQUIPAS_INCOMPATIVEIS"-> "O número de equipas tem de ser múltiplo de 4 (grupos de 4).";
                default -> {
                    if (ex.getMessage().startsWith("EQUIPAS_SEM_JOGADORES"))
                        yield "Equipas sem jogadores APTOS:\n"
                                + ex.getMessage().replace("EQUIPAS_SEM_JOGADORES: ", "");
                    yield ex.getMessage();
                }
            };
            JOptionPane.showMessageDialog(this, msg, "Erro ao Gerar Torneio", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            if ("SEM_ESTADIOS".equals(ex.getMessage())) {
                JOptionPane.showMessageDialog(this,
                        "Adicione pelo menos um estádio antes de gerar o torneio.",
                        "Sem Estádios", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Dashboard principal após gerar o torneio
    // ══════════════════════════════════════════════════════════════════════════

    private void abrirDashboard() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(COR_FUNDO_AZUL);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Separador "Página Principal" — botões UC08 (Validar/Iniciar Torneio)
        tabs.addTab("Página Principal", criarPainelPaginaPrincipal(tabs));

        // Separador "Equipas" — já existia
        tabs.addTab("Equipas", new PainelEquipas(equipaControlador, jogadorControlador));

        // Separador "Estádios" — já existia
        tabs.addTab("Estádios", new PainelEstadios(estadioControlador));

        // Separador "Calendário" — novo (UC09-UC12)
        painelCalendario = new PainelCalendario(torneioControlador, jogoControlador, eventoControlador);
        tabs.addTab("Calendário", painelCalendario);

        painelEstatisticas = new PainelEstatisticasTorneio();
        // Separador "Jogadores" — stub (estatísticas UC17, fora do âmbito UC09-12)
        tabs.addTab("Jogadores", new PainelEstatisticasTorneio());

        tabs.addChangeListener(e -> {
            PainelEstatisticasTorneio painelEstatisticas;
            painelEstatisticas = new PainelEstatisticasTorneio();
            if (tabs.getSelectedIndex() == indiceJogadores && painelEstatisticas != null) {
                painelEstatisticas.atualizar();
            }
        });

        PainelBilhetesPatrocinios painelBilhetes = new PainelBilhetesPatrocinios(
                bilheteControlador, patrocinioControlador, financeiroControlador, jogoControlador);
        tabs.addTab("Receita", painelBilhetes);

        tabs.setSelectedIndex(3); // abre no Calendário após gerar

        add(tabs, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Painel "Página Principal" com UC08 (Validar/Iniciar Torneio)
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarPainelPaginaPrincipal(JTabbedPane tabs) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(COR_FUNDO_EXTERIOR);

        JPanel cartao = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_FUNDO_AZUL);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new Color(0x3498DB));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));
        cartao.setOpaque(false);
        cartao.setPreferredSize(new Dimension(400, 300));
        cartao.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel titulo = new JLabel("Estado do Torneio");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartao.add(titulo);
        cartao.add(Box.createVerticalStrut(16));

        JLabel lblEstado = new JLabel("Estado: " + formatarEstadoTorneio());
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartao.add(lblEstado);
        cartao.add(Box.createVerticalStrut(20));

        // 1. CRIAR OS DOIS BOTÕES PRIMEIRO
        JButton btnValidar = criarBotaoFigma("Validar Calendário");
        btnValidar.setMaximumSize(new Dimension(360, 38));
        btnValidar.setEnabled(torneioControlador.podeValidar());

        JButton btnIniciar = criarBotaoFigma("Iniciar Torneio");
        btnIniciar.setMaximumSize(new Dimension(360, 38));
        btnIniciar.setEnabled(torneioControlador.podeIniciar());

        // 2. AÇÃO DO BOTÃO VALIDAR
        btnValidar.addActionListener(e -> {
            try {
                java.util.List<String> conflitos = torneioControlador.validarCalendario();
                if (conflitos.isEmpty()) {
                    torneioControlador.confirmarValidacao();
                    lblEstado.setText("Estado: " + formatarEstadoTorneio());
                    btnValidar.setEnabled(false);

                    // --> Ativa o botão de Iniciar <--
                    btnIniciar.setEnabled(true);

                    JOptionPane.showMessageDialog(p,
                            "Calendário validado sem conflitos!\nPode agora iniciar o torneio.",
                            "Validação Concluída", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    StringBuilder sb = new StringBuilder("Conflitos encontrados:\n");
                    conflitos.forEach(c -> sb.append("• ").append(c).append("\n"));
                    JOptionPane.showMessageDialog(p, sb.toString(),
                            "Conflitos no Calendário", JOptionPane.WARNING_MESSAGE);
                }
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(p, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 3. AÇÃO DO BOTÃO INICIAR
        btnIniciar.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(p,
                    "Tem a certeza que quer iniciar o torneio?\nEsta acção não pode ser revertida.",
                    "Iniciar Torneio", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                torneioControlador.iniciarTorneio();
                lblEstado.setText("Estado: " + formatarEstadoTorneio());
                btnIniciar.setEnabled(false);
                tabs.setSelectedIndex(3); // navega para Calendário
                if (painelCalendario != null) painelCalendario.atualizar();
            }
        });

        // 4. ADICIONAR TUDO AO CARTÃO (VISUAL)
        cartao.add(btnValidar);
        cartao.add(Box.createVerticalStrut(10));
        cartao.add(btnIniciar);

        p.add(cartao);
        return p;
    }

    private String formatarEstadoTorneio() {
        return switch (Torneio.getInstancia().getEstado()) {
            case INICIAL     -> "INICIAL";
            case CONFIGURADO -> "CONFIGURADO (aguarda validação)";
            case VALIDADO    -> "VALIDADO (pronto para iniciar)";
            case EM_CURSO    -> "EM CURSO";
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Utilitários de UI
    // ══════════════════════════════════════════════════════════════════════════

    private void atualizarLista() {
        listModel.clear();
        for (Equipa eq : equipaControlador.getEquipas()) listModel.addElement(eq);
    }

    private JLabel criarLabelErro() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(Color.RED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JTextField criarCampoData(String placeholder, JLabel lblErro) {
        JTextField campo = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Color.GRAY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        campo.setMaximumSize(new Dimension(380, 34));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setBackground(COR_BRANCO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_BRANCO, 1, true), new EmptyBorder(5, 10, 5, 10)));
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                lblErro.setText(" ");
            }
        });
        return campo;
    }

    private JButton criarBotaoFigma(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? COR_BRANCO : new Color(0xDDDDDD));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setForeground(Color.BLACK);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_BRANCO, 1, true), new EmptyBorder(5, 10, 5, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void atualizarEstatisticas() {
        if (painelEstatisticas != null) {
            painelEstatisticas.atualizar();
        }
    }
}
