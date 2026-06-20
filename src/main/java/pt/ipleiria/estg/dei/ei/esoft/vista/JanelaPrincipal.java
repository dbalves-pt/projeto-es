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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

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
    private JLabel                   lblErroDataInicio;
    private JLabel                   lblErroDataFim;

    // ── Novas variáveis para guardar as regras ──
    private int diasDescansoConfigurados = -1;
    private int numeroTotalEquipasConfigurado = -1;
    // ── Referência ao painel do calendário (para refrescar após acções) ────────
    private PainelCalendario painelCalendario;

    // ── Referência ao JTabbedPane da dashboard (para refrescar a Página Principal) ──
    private JTabbedPane tabsDashboard;

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
    // ── Máscara Automática de Data (Atualizada) ─────────────────────────────
    public static void aplicarMascaraData(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr); // Redireciona tudo para o replace
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;

                // ── A SOLUÇÃO ESTÁ AQUI ──
                // Se for apagar texto (empty), se tiver letras (placeholder) ou se já trouxer barras (data do sistema), deixa passar!
                if (text.isEmpty() || text.matches(".*[a-zA-Z].*") || text.contains("/")) {
                    fb.replace(offset, length, text, attrs);
                    return;
                }

                // Se o utilizador estiver a digitar apenas números no teclado, aplica as barras
                if (text.matches("\\d+")) {
                    StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                    sb.replace(offset, offset + length, text);

                    String texto = sb.toString().replaceAll("/", "");
                    if (texto.length() <= 8) {
                        StringBuilder formatado = new StringBuilder();
                        for (int i = 0; i < texto.length(); i++) {
                            if (i == 2 || i == 4) formatado.append("/");
                            formatado.append(texto.charAt(i));
                        }
                        fb.replace(0, fb.getDocument().getLength(), formatado.toString(), attrs);
                    }
                }
            }
        });
    }

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

        // Título (Agora Centrado)
        JPanel painelTitulo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        painelTitulo.setOpaque(false);
        painelTitulo.setAlignmentX(Component.CENTER_ALIGNMENT); // ALINHAMENTO CORRIGIDO
        painelTitulo.setMaximumSize(new Dimension(380, 30));
        JLabel lblTitulo = new JLabel("Criar Torneio");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
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
        painel.add(Box.createVerticalStrut(10));

        // Botão para abrir as Regras
        JButton btnRegras = criarBotaoFigma("Definir Regras...");
        btnRegras.setMaximumSize(new Dimension(380, 38));
        btnRegras.addActionListener(e -> abrirDialogoRegras());
        painel.add(btnRegras);
        painel.add(Box.createVerticalStrut(15));

        // Lista de Equipas (Agora Centrada)
        JPanel painelLista = new JPanel(new BorderLayout());
        painelLista.setAlignmentX(Component.CENTER_ALIGNMENT); // ALINHAMENTO CORRIGIDO
        painelLista.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        painelLista.setMaximumSize(new Dimension(380, 200));

        JLabel lblHeaderEquipas = new JLabel("  Equipas");
        lblHeaderEquipas.setOpaque(true);
        lblHeaderEquipas.setBackground(COR_CINZENTO_HEADER);
        lblHeaderEquipas.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblHeaderEquipas.setPreferredSize(new Dimension(380, 25));
        painelLista.add(lblHeaderEquipas, BorderLayout.NORTH);

        listModel    = new DefaultListModel<>();
        listaEquipas = new JList<>(listModel);
        listaEquipas.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 1. Permite selecionar várias equipas ao mesmo tempo (segurando CTRL ou SHIFT)
        listaEquipas.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // 2. Troca o evento para só abrir a edição com DUPLO CLIQUE
        listaEquipas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // Deteta o duplo clique
                    Equipa selecionada = listaEquipas.getSelectedValue();
                    if (selecionada != null) {
                        FormularioEquipa form = new FormularioEquipa(JanelaPrincipal.this, equipaControlador,
                                jogadorControlador, selecionada, JanelaPrincipal.this::atualizarLista);
                        form.setVisible(true);
                        listaEquipas.clearSelection();
                    }
                }
            }
        });

        painelLista.add(new JScrollPane(listaEquipas), BorderLayout.CENTER);

        // Botão Inserir
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

        // Botão Remover (NOVO)
        // Botão Remover (NOVO - Atualizado para apagar múltiplas)
        JButton btnRemover = new JButton("Remover Equipa");
        btnRemover.setContentAreaFilled(false);
        btnRemover.setBorder(new EmptyBorder(5, 5, 5, 5));
        btnRemover.setHorizontalAlignment(SwingConstants.RIGHT);
        btnRemover.setForeground(new Color(200, 0, 0));
        btnRemover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRemover.addActionListener(e -> {
            // Vai buscar TODAS as equipas que estiverem selecionadas
            java.util.List<Equipa> selecionadas = listaEquipas.getSelectedValuesList();

            if (!selecionadas.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Tem a certeza que quer remover as " + selecionadas.size() + " equipa(s) selecionada(s)?",
                        "Remover Equipas", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    for (Equipa eq : selecionadas) {
                        equipaControlador.eliminarEquipa(eq);
                    }
                    atualizarLista();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione pelo menos uma equipa na lista primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Painel para colocar os dois botões lado a lado na base da lista
        JPanel painelBotoesLista = new JPanel(new GridLayout(1, 2));
        painelBotoesLista.setOpaque(false);
        painelBotoesLista.add(btnInserir);
        painelBotoesLista.add(btnRemover);

        painelLista.add(painelBotoesLista, BorderLayout.SOUTH);
        painel.add(painelLista);
        painel.add(Box.createVerticalStrut(15));

        // Botão Gerar Torneio
        JButton btnGerar = criarBotaoFigma("Gerar torneio");
        btnGerar.setMaximumSize(new Dimension(380, 38));
        btnGerar.addActionListener(e -> validarEGerarTorneio());
        painel.add(btnGerar);

        painel.add(Box.createVerticalGlue()); // Mantém os elementos empurrados para cima

        return painel;
    }
    // ══════════════════════════════════════════════════════════════════════════
    //  Lógica UC07 — Gerar Torneio
    // ══════════════════════════════════════════════════════════════════════════

    // ── NOVO: Formulário de Regras ──
    private void abrirDialogoRegras() {
        JDialog dialog = new JDialog(this, "Regras do Torneio", true);
        dialog.setSize(350, 400);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COR_FUNDO_AZUL);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Regras do Torneio");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(titulo);
        p.add(Box.createVerticalStrut(30));

        // Dias de Descanso
        JLabel lblDias = new JLabel("Dias de Descanso (mínimo 3 dias)");
        lblDias.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDias.setFont(new Font("SansSerif", Font.BOLD, 12));
        JTextField txtDias = new JTextField(diasDescansoConfigurados >= 0 ? String.valueOf(diasDescansoConfigurados) : "");
        txtDias.setMaximumSize(new Dimension(100, 30));
        txtDias.setHorizontalAlignment(JTextField.CENTER);
        txtDias.setBorder(new LineBorder(COR_BRANCO, 5, true));
        p.add(lblDias);
        p.add(Box.createVerticalStrut(10));
        p.add(txtDias);
        p.add(Box.createVerticalStrut(30));

        // Total de Equipas
        JLabel lblEquipas = new JLabel("Número total de equipas");
        lblEquipas.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEquipas.setFont(new Font("SansSerif", Font.BOLD, 12));
        JTextField txtEquipas = new JTextField(numeroTotalEquipasConfigurado >= 0 ? String.valueOf(numeroTotalEquipasConfigurado) : "");
        txtEquipas.setMaximumSize(new Dimension(100, 30));
        txtEquipas.setHorizontalAlignment(JTextField.CENTER);
        txtEquipas.setBorder(new LineBorder(COR_BRANCO, 5, true));
        p.add(lblEquipas);
        p.add(Box.createVerticalStrut(10));
        p.add(txtEquipas);

        p.add(Box.createVerticalGlue());

        // Botão Concluído
        JButton btnConcluido = criarBotaoFigma("Concluído");
        btnConcluido.setMaximumSize(new Dimension(300, 38));
        btnConcluido.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConcluido.addActionListener(e -> {
            try {
                int dias = Integer.parseInt(txtDias.getText().trim());
                int equipas = Integer.parseInt(txtEquipas.getText().trim());

                if (dias < 3) throw new IllegalArgumentException("Os dias de descanso têm de ser no mínimo 3.");
                if (equipas <= 0 || equipas % 4 != 0) throw new IllegalArgumentException("O número de equipas tem de ser múltiplo de 4.");

                diasDescansoConfigurados = dias;
                numeroTotalEquipasConfigurado = equipas;
                dialog.dispose(); // Fecha a janela se estiver tudo bem
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Introduza valores numéricos válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        p.add(btnConcluido);

        dialog.add(p);
        dialog.setVisible(true);
    }

    // ── ATUALIZADO: Lógica de Validação ──
    private void validarEGerarTorneio() {
        // Verifica se o utilizador abriu as regras e configurou os valores
        if (diasDescansoConfigurados < 0 || numeroTotalEquipasConfigurado < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, defina as regras do torneio (botão 'Definir Regras...').", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verifica se inseriram exatamente o número de equipas pedido nas regras
        if (listModel.size() != numeroTotalEquipasConfigurado) {
            JOptionPane.showMessageDialog(this, "Tem " + listModel.size() + " equipas, mas as regras exigem " + numeroTotalEquipasConfigurado + ".\nAdicione ou remova equipas.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String strInicio = campDataInicio.getText().trim();
        String strFim    = campDataFim.getText().trim();
        final String PLACEHOLDER_INICIO = "Data de Início (dd/MM/yyyy)...";
        final String PLACEHOLDER_FIM    = "Data de Fim (dd/MM/yyyy)...";

        if (strInicio.equals(PLACEHOLDER_INICIO)) strInicio = "";
        if (strFim.equals(PLACEHOLDER_FIM))       strFim    = "";

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataInicio, dataFim;

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

        lblErroDataInicio.setText(" ");
        lblErroDataFim.setText(" ");

        try {
            // Usa as variáveis guardadas pelo Formulario de Regras
            torneioControlador.configurarTorneio(dataInicio, dataFim, diasDescansoConfigurados);
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
        tabs.setOpaque(true);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 15));

        // ── MAGIA PARA DEIXAR O MENU IGUAL AO FIGMA ──
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(COR_FUNDO_AZUL); // Pinta a barra toda de azul
                g.fillRect(0, 0, tabs.getWidth(), calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight));
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {}

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(font);

                // --- ATUALIZADO: Tratamento visual para aba desativada ---
                if (!tabs.isEnabledAt(tabIndex)) {
                    g2.setColor(new Color(0x777777)); // Cinzento para indicar desativado
                } else {
                    g2.setColor(Color.BLACK); // Cor normal
                }
                // ----------------------------------------------------------

                // Centrar o texto
                int textX = textRect.x + (textRect.width - metrics.stringWidth(title)) / 2;
                int textY = textRect.y + metrics.getAscent() + (textRect.height - metrics.getHeight()) / 2;
                g2.drawString(title, textX, textY);

                // Se for a aba ativa e estiver ativada, desenha a linha preta grossa por baixo
                if (isSelected && tabs.isEnabledAt(tabIndex)) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.fillRoundRect(textX, textY + 6, metrics.stringWidth(title), 5, 5, 5);
                }
                g2.dispose();
            }
            @Override
            protected Insets getTabInsets(int tabPlacement, int tabIndex) {
                return new Insets(15, 30, 15, 30); // Espaçamento grande entre menus
            }
        });

        this.tabsDashboard = tabs;
        tabs.addTab("Página Principal", criarPainelPaginaPrincipal(tabs));

        // Separador "Equipas" — já existia
        tabs.addTab("Equipas", new PainelEquipas(equipaControlador, jogadorControlador));

        // Separador "Estádios" — já existia
        tabs.addTab("Estádios", new PainelEstadios(estadioControlador));

        // Separador "Calendário" — novo (UC09-UC12)
        painelCalendario = new PainelCalendario(torneioControlador, jogoControlador, eventoControlador);
        tabs.addTab("Calendário", painelCalendario);

        // --- NOVO: Bloqueia a aba Calendário inicialmente (índice 3) ---
        tabs.setEnabledAt(3, false);
        // ---------------------------------------------------------------

        // Separador "Jogadores" (UC17)
        painelEstatisticas = new PainelEstatisticasTorneio();
        tabs.addTab("Jogadores", painelEstatisticas);

        tabs.addChangeListener(e -> {
            // O separador "Jogadores" é o índice 4 (0=Página Principal, 1=Equipas, 2=Estádios, 3=Calendário, 4=Jogadores)
            if (tabs.getSelectedIndex() == 4 && painelEstatisticas != null) {
                painelEstatisticas.atualizar();
            }
        });
 
        PainelBilhetesPatrocinios painelBilhetes = new PainelBilhetesPatrocinios(
                bilheteControlador, patrocinioControlador, financeiroControlador, jogoControlador);
        tabs.addTab("Receita", painelBilhetes);

        tabs.setSelectedIndex(0); // abre no Dashboard após gerar

        add(tabs, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

// ══════════════════════════════════════════════════════════════════════════
    //  Painel "Página Principal" (Dashboard Dinâmico)
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel painelColunasDashboard;

    private JPanel criarPainelPaginaPrincipal(JTabbedPane tabs) {
        JPanel pMain = new JPanel(new BorderLayout());
        pMain.setBackground(COR_BRANCO);
        pMain.setBorder(new EmptyBorder(10, 20, 20, 20));

        // ── BARRA SUPERIOR COM BOTÃO DE ATUALIZAR ──
        JPanel pTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pTopo.setBackground(COR_BRANCO);

        JButton btnAtualizar = new JButton("↻ Atualizar Dashboard") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE0E0E0)); // Fundo cinzento claro, muito limpo
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // Cantos arredondados
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAtualizar.setOpaque(false);
        btnAtualizar.setContentAreaFilled(false);
        btnAtualizar.setBorderPainted(false);

        // Aplica o "preto" moderno nas letras
        btnAtualizar.setForeground(new Color(0x333333));

        btnAtualizar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnAtualizar.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnAtualizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> recarregarDadosDashboard(tabs));

        pTopo.add(btnAtualizar);
        pMain.add(pTopo, BorderLayout.NORTH);

        // ── ÁREA DAS 3 COLUNAS ──
        painelColunasDashboard = new JPanel(new GridBagLayout());
        painelColunasDashboard.setBackground(COR_BRANCO);
        pMain.add(painelColunasDashboard, BorderLayout.CENTER);

        recarregarDadosDashboard(tabs);

        return pMain;
    }

    private void recarregarDadosDashboard(JTabbedPane tabs) {
        painelColunasDashboard.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Coluna 1: Esquerda
        gbc.gridx = 0; gbc.weightx = 0.28; gbc.insets = new Insets(0, 0, 0, 15);
        painelColunasDashboard.add(criarColunaEsquerdaAutomatica(tabs), gbc);

        // Coluna 2: Centro
        gbc.gridx = 1; gbc.weightx = 0.44; gbc.insets = new Insets(0, 0, 0, 15);
        painelColunasDashboard.add(criarColunaCentro(), gbc);

        // Coluna 3: Direita
        gbc.gridx = 2; gbc.weightx = 0.28; gbc.insets = new Insets(0, 0, 0, 0);
        painelColunasDashboard.add(criarColunaDireitaAutomatica(), gbc);

        painelColunasDashboard.revalidate();
        painelColunasDashboard.repaint();
    }

    private JPanel criarColunaEsquerdaAutomatica(JTabbedPane tabs) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);

        // 1. Bloco Patrocínios Dinâmico
        JPanel pPatrocinios = criarCaixaCinza("Patrocínios");
        pPatrocinios.add(criarLinhaDestaqueAmarela("TOTAL: " + financeiroControlador.getReceitaPatrocinios() + "€"));
        pPatrocinios.add(Box.createVerticalStrut(3));

        java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio> listaPatrocinios = patrocinioControlador.getPatrocinios();
        if (listaPatrocinios.isEmpty()) {
            pPatrocinios.add(criarLinhaBranca("Sem patrocínios registados."));
            pPatrocinios.add(Box.createVerticalStrut(3));
        } else {
            for (pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio p : listaPatrocinios) {
                pPatrocinios.add(criarLinhaBranca(p.getNomePatrocinador() + ": " + p.getValor() + "€"));
                pPatrocinios.add(Box.createVerticalStrut(3));
            }
        }


        painel.add(pPatrocinios);
        painel.add(Box.createVerticalStrut(20));

        // 2. Bloco Bilheteira Dinâmico
        JPanel pBilheteira = criarCaixaCinza("Bilheteira");
        pBilheteira.add(criarLinhaBrancaTotal("TOTAL: " + financeiroControlador.getReceitaBilheteira() + "€"));
        pBilheteira.add(Box.createVerticalStrut(3));
        pBilheteira.add(criarLinhaBranca("Portugal x França: 5000€"));
        pBilheteira.add(Box.createVerticalStrut(3));
        pBilheteira.add(criarLinhaBranca("Alemanha x Brasil: 3500€"));
        pBilheteira.add(Box.createVerticalStrut(3));
        pBilheteira.add(criarLinhaBranca("(Jogos inseridos automaticamente)"));
        painel.add(pBilheteira);

        painel.add(Box.createVerticalGlue());

        // 3. Botão Gerar Calendário
        JButton btnGerarCal = criarBotaoCinza("Gerar Calendário");
        btnGerarCal.addActionListener(e -> {
            try {
                // (Se a tua lógica tiver um método de gerar, tipo torneioControlador.gerarCalendario(), devia estar aqui!)

                java.util.List<String> conflitos = torneioControlador.validarCalendario();
                if (conflitos.isEmpty()) {
                    torneioControlador.confirmarValidacao();
                    torneioControlador.iniciarTorneio();
                    JOptionPane.showMessageDialog(this, "Calendário validado e torneio iniciado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                    // --- NOVO: Desbloqueia a aba e salta para lá! ---
                    tabs.setEnabledAt(3, true);
                    // ------------------------------------------------

                    tabs.setSelectedIndex(3);
                } else {
                    StringBuilder erro = new StringBuilder("Conflitos no calendário:\n\n");
                    for (String c : conflitos) erro.append("• ").append(c).append("\n");
                    JOptionPane.showMessageDialog(this, erro.toString(), "Erro", JOptionPane.WARNING_MESSAGE);
                }
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        painel.add(btnGerarCal);

        return painel;
    }

    private JPanel criarColunaCentro() {
        JPanel painel = criarCaixaCinza("Eliminatória");
        painel.setLayout(new BorderLayout());

        JPanel canvasBranco = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Desenha o fundo branco arredondado do interior
                g2.setColor(COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                int w = getWidth(); int h = getHeight();
                int x1 = 20, x2 = w / 3, x3 = (w / 3) * 2, x4 = w - 20;

                g2.setColor(new Color(0x333333));
                g2.setStroke(new BasicStroke(2));

                int[] yPos = {h/8, (h/8)*3, (h/8)*5, (h/8)*7};
                for (int y : yPos) {
                    g2.drawLine(x1, y - 20, x2, y - 20);
                    g2.drawLine(x1, y + 20, x2, y + 20);
                    g2.drawLine(x2, y - 20, x2, y + 20);
                }
                g2.drawLine(x2, yPos[0], x3, yPos[0]);
                g2.drawLine(x2, yPos[1], x3, yPos[1]);
                g2.drawLine(x3, yPos[0], x3, yPos[1]);

                g2.drawLine(x2, yPos[2], x3, yPos[2]);
                g2.drawLine(x2, yPos[3], x3, yPos[3]);
                g2.drawLine(x3, yPos[2], x3, yPos[3]);

                int meio1 = (yPos[0] + yPos[1]) / 2;
                int meio2 = (yPos[2] + yPos[3]) / 2;
                g2.drawLine(x3, meio1, x4, meio1);
                g2.drawLine(x3, meio2, x4, meio2);
                g2.drawLine(x4, meio1, x4, meio2);
                g2.drawLine(x4, h/2, w, h/2);

                // --- DESENHO DINÂMICO DOS NOMES DAS EQUIPAS ---
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> todosJogos = jogoControlador.getJogos();

                int[] yQuartos = {yPos[0]-25, yPos[0]+15, yPos[1]-25, yPos[1]+15, yPos[2]-25, yPos[2]+15, yPos[3]-25, yPos[3]+15};
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> jogosQuartos = todosJogos.stream()
                        .filter(j -> j.getFase() != null && j.getFase().name().equals("QUARTOS")).toList();
                for (int i = 0; i < Math.min(jogosQuartos.size(), 4); i++) {
                    g2.drawString(jogosQuartos.get(i).getEquipaCasa().getNome(), x1 + 5, yQuartos[i*2]);
                    g2.drawString(jogosQuartos.get(i).getEquipaFora().getNome(), x1 + 5, yQuartos[(i*2) + 1]);
                }

                int[] yMeias = {yPos[0]-5, yPos[1]-5, yPos[2]-5, yPos[3]-5};
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> jogosMeias = todosJogos.stream()
                        .filter(j -> j.getFase() != null && j.getFase().name().equals("MEIAS")).toList();
                for (int i = 0; i < Math.min(jogosMeias.size(), 2); i++) {
                    g2.drawString(jogosMeias.get(i).getEquipaCasa().getNome(), x2 + 5, yMeias[i*2]);
                    g2.drawString(jogosMeias.get(i).getEquipaFora().getNome(), x2 + 5, yMeias[(i*2) + 1]);
                }

                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> jogosFinal = todosJogos.stream()
                        .filter(j -> j.getFase() != null && j.getFase().name().equals("FINAL")).toList();
                if (!jogosFinal.isEmpty()) {
                    g2.drawString(jogosFinal.get(0).getEquipaCasa().getNome(), x3 + 5, meio1 - 5);
                    g2.drawString(jogosFinal.get(0).getEquipaFora().getNome(), x3 + 5, meio2 - 5);
                }
                g2.dispose();
            }
        };
        canvasBranco.setOpaque(false);
        canvasBranco.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 15, 15, 15));
        wrapper.add(canvasBranco, BorderLayout.CENTER);

        painel.add(wrapper, BorderLayout.CENTER);
        return painel;
    }


    private JPanel criarColunaDireitaAutomatica() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);

        String vencedor = jogoControlador.obterVencedorTorneio();
        painel.add(criarCartaoEstatistica("Vencedor", "", vencedor, ""));
        painel.add(Box.createVerticalStrut(15));

        String marcNome = jogoControlador.obterNomeMelhorMarcador();
        String marcGolos = jogoControlador.obterGolosMelhorMarcador();
        painel.add(criarCartaoEstatistica("Melhor Marcador", "Golos", marcNome, marcGolos));
        painel.add(Box.createVerticalStrut(15));

        String assisNome = jogoControlador.obterNomeMaisAssistencias();
        String assisNum = jogoControlador.obterNumeroMaisAssistencias();
        painel.add(criarCartaoEstatistica("Jogador com mais assistências", "Assistências", assisNome, assisNum));
        painel.add(Box.createVerticalStrut(15));

        String defNome = jogoControlador.obterEquipaMelhorDefesa();
        String defGolos = jogoControlador.obterGolosSofridosMelhorDefesa();
        painel.add(criarCartaoEstatistica("Equipa com a melhor defesa", "Golos sofridos", defNome, defGolos));
        painel.add(Box.createVerticalStrut(15));

        String atqNome = jogoControlador.obterEquipaMelhorAtaque();
        String atqGolos = jogoControlador.obterGolosMarcadosMelhorAtaque();
        painel.add(criarCartaoEstatistica("Equipa com o melhor ataque", "Golos marcados", atqNome, atqGolos));

        painel.add(Box.createVerticalGlue()); // Evita que os cartões estiquem

        return painel;
    }

    // ── COMPONENTES VISUAIS REFEITOS (DESIGN SYSTEM) ───────────────────────────

    private JPanel criarCaixaCinza(String titulo) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(217, 217, 217)); // Cinza exato do Figma
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblTitulo);
        p.add(Box.createVerticalStrut(15));
        return p;
    }

    private JPanel criarCartaoEstatistica(String tituloL, String tituloR, String valorL, String valorR) {
        // Criar os painéis antes para sabermos a altura deles na hora de pintar
        JPanel top = new JPanel(new BorderLayout());
        JPanel bot = new JPanel(new BorderLayout());

        JPanel cartao = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fundo cinza exterior
                g2.setColor(new Color(217, 217, 217));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // O segredo para não sair dos cantos arredondados
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(6, 6, getWidth()-12, getHeight()-12, 10, 10));

                // Agora a altura amarela é inteligente! Acompanha o texto de cima.
                int alturaAmarelo = top.getHeight() > 0 ? top.getHeight() + 6 : getHeight() / 2;

                // Metade superior amarela
                g2.setColor(new Color(223, 202, 56));
                g2.fillRect(6, 6, getWidth()-12, alturaAmarelo);

                // Metade inferior branca
                g2.setColor(Color.WHITE);
                g2.fillRect(6, alturaAmarelo, getWidth()-12, getHeight() - alturaAmarelo);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        cartao.setOpaque(false);
        cartao.setLayout(new BorderLayout());

        // Aumentei um pouco a altura limite para o texto respirar melhor
        cartao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cartao.setPreferredSize(new Dimension(300, 110));

        top.setOpaque(false);
        top.setBorder(new EmptyBorder(12, 15, 5, 15));

        // Em vez de partir todos os espaços, usamos o CSS nativo do Java para organizar o texto!
        JLabel lblTopL = new JLabel("<html><div style='width: 120px;'><b>" + tituloL + "</b></div></html>");
        JLabel lblTopR = new JLabel("<html><b>" + tituloR + "</b></html>");
        top.add(lblTopL, BorderLayout.WEST);
        top.add(lblTopR, BorderLayout.EAST);

        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(5, 15, 12, 15));
        bot.add(new JLabel(valorL), BorderLayout.WEST);
        bot.add(new JLabel(valorR), BorderLayout.EAST);

        cartao.add(top, BorderLayout.NORTH);
        cartao.add(bot, BorderLayout.CENTER);
        return cartao;
    }

    private JLabel criarLinhaDestaqueAmarela(String texto) {
        JLabel lbl = new JLabel(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(223, 202, 56));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setOpaque(false);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setBorder(new EmptyBorder(8, 10, 8, 10));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton criarBotaoDestaqueAmarelo(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(223, 202, 56));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 10, 8, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel criarLinhaBranca(String texto) {
        JLabel lbl = new JLabel(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setOpaque(false);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setBorder(new EmptyBorder(8, 10, 8, 10));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel criarLinhaBrancaTotal(String texto) {
        JLabel lbl = criarLinhaBranca(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        return lbl;
    }

    private JButton criarBotaoCinza(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(217, 217, 217));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setBorder(new EmptyBorder(15, 15, 15, 15));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fundo branco com cantos arredondados (igual aos botões)
                g2.setColor(COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                super.paintComponent(g);

                // Se estiver vazio, desenha o placeholder cinzento
                if (getText().isEmpty()) {
                    g2.setColor(Color.GRAY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }
                g2.dispose();
            }
        };

        campo.setOpaque(false); // Remove as pontas brancas quadradas!

        // FORÇA O TAMANHO E O ALINHAMENTO EXATAMENTE IGUAL AOS BOTÕES:
        campo.setMaximumSize(new Dimension(380, 38));
        campo.setPreferredSize(new Dimension(380, 38));
        campo.setAlignmentX(Component.CENTER_ALIGNMENT); // <--- O SEGREDO ESTÁ AQUI

        // Margens internas para o texto não ficar colado à borda
        campo.setBorder(new EmptyBorder(5, 15, 5, 15));

        // Limpa a mensagem de erro quando clicamos na caixa
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                lblErro.setText(" ");
            }
        });
        aplicarMascaraData(campo);
        return campo;
    }


    private JButton criarBotaoFigma(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? COR_BRANCO : new Color(0xDDDDDD));
                // 15 para ter cantos mais redondinhos e modernos
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); // ALINHAMENTO CORRIGIDO PARA O CENTRO
        btn.setForeground(Color.BLACK);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false); // ISTO TIRA O FUNDO BRANCO ESTRANHO!
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_BRANCO, 1, true), new EmptyBorder(5, 10, 5, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Callback central de reatividade — invocado a partir de qualquer ecrã
     * filho (FormularioEvento, PainelEstatisticasJogo, FormularioDetalhesJogo,
     * PainelCalendario) sempre que um Golo, Cartão, Início ou Fim de Jogo é
     * registado. Atualiza a aba "Jogadores" e a Dashboard "Página Principal"
     * num único ponto, para não haver telas desatualizadas.
     */
    public void atualizarEstatisticas() {
        if (painelEstatisticas != null) {
            painelEstatisticas.atualizar();
        }
        if (tabsDashboard != null && painelColunasDashboard != null) {
            recarregarDadosDashboard(tabsDashboard);
        }
    }
}