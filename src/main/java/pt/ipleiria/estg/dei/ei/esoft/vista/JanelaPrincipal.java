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
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        this.tabsDashboard = tabs;

        // Separador "Página Principal" — botões UC08 (Validar/Iniciar Torneio)
        tabs.addTab("Página Principal", criarPainelPaginaPrincipal(tabs));

        // Separador "Equipas" — já existia
        tabs.addTab("Equipas", new PainelEquipas(equipaControlador, jogadorControlador));

        // Separador "Estádios" — já existia
        tabs.addTab("Estádios", new PainelEstadios(estadioControlador));

        // Separador "Calendário" — novo (UC09-UC12)
        painelCalendario = new PainelCalendario(torneioControlador, jogoControlador, eventoControlador);
        tabs.addTab("Calendário", painelCalendario);

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

    private JPanel painelColunasDashboard; // Variável global para podermos atualizar

    private JPanel criarPainelPaginaPrincipal(JTabbedPane tabs) {
        JPanel pMain = new JPanel(new BorderLayout());
        pMain.setBackground(COR_BRANCO);
        pMain.setBorder(new EmptyBorder(10, 20, 20, 20));

        // ── BARRA SUPERIOR COM BOTÃO DE ATUALIZAR ──
        JPanel pTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pTopo.setBackground(COR_BRANCO);
        JButton btnAtualizar = new JButton("↻ Atualizar Dashboard");
        btnAtualizar.setBackground(new Color(0x3498DB));
        btnAtualizar.setForeground(COR_BRANCO);
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> recarregarDadosDashboard(tabs));
        pTopo.add(btnAtualizar);
        pMain.add(pTopo, BorderLayout.NORTH);

        // ── ÁREA DAS 3 COLUNAS ──
        painelColunasDashboard = new JPanel(new GridBagLayout());
        painelColunasDashboard.setBackground(COR_BRANCO);
        pMain.add(painelColunasDashboard, BorderLayout.CENTER);

        // Carrega os dados pela primeira vez
        recarregarDadosDashboard(tabs);

        return pMain;
    }

    private void recarregarDadosDashboard(JTabbedPane tabs) {
        painelColunasDashboard.removeAll(); // Limpa as colunas antigas

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Coluna 1: Esquerda
        gbc.gridx = 0; gbc.weightx = 0.25; gbc.insets = new Insets(0, 0, 0, 15);
        painelColunasDashboard.add(criarColunaEsquerdaAutomatica(tabs), gbc);

        // Coluna 2: Centro
        gbc.gridx = 1; gbc.weightx = 0.50; gbc.insets = new Insets(0, 0, 0, 15);
        painelColunasDashboard.add(criarColunaCentro(), gbc);

        // Coluna 3: Direita
        gbc.gridx = 2; gbc.weightx = 0.25; gbc.insets = new Insets(0, 0, 0, 0);
        painelColunasDashboard.add(criarColunaDireitaAutomatica(), gbc);

        // Força a interface a desenhar as novidades
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

        // Vai buscar a lista de patrocínios ao controlador e cria as linhas automaticamente
        java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio> listaPatrocinios = patrocinioControlador.getPatrocinios();
        if (listaPatrocinios.isEmpty()) {
            pPatrocinios.add(criarLinhaBranca("Sem patrocínios registados."));
        } else {
            for (pt.ipleiria.estg.dei.ei.esoft.modelo.Patrocinio p : listaPatrocinios) {
                pPatrocinios.add(criarLinhaBranca(p.getNomePatrocinador() + ": " + p.getValor() + "€"));
            }
        }

        pPatrocinios.add(Box.createVerticalStrut(5));
        JButton btnInserirPat = new JButton("Inserir Patrocínio...");
        btnInserirPat.setBackground(new Color(0xDFCA38));
        btnInserirPat.setOpaque(true); btnInserirPat.setBorderPainted(false);
        // (Aqui podes ligar a ação de abrir o formulário de patrocínios)
        pPatrocinios.add(btnInserirPat);
        painel.add(pPatrocinios);
        painel.add(Box.createVerticalStrut(20));

        // 2. Bloco Bilheteira Dinâmico
        JPanel pBilheteira = criarCaixaCinza("Bilheteira");
        pBilheteira.add(criarLinhaBrancaTotal("TOTAL: " + financeiroControlador.getReceitaBilheteira() + "€"));

        // Simulação dinâmica: Se tiveres um método de jogos com bilhetes vendidos, usarias aqui.
        pBilheteira.add(criarLinhaBranca("(Receitas extraídas dos jogos)"));
        painel.add(pBilheteira);

        painel.add(Box.createVerticalGlue()); // Empurra o botão Gerar para o fundo

        // 3. Botão Gerar / Validar Calendário
        JButton btnGerarCal = new JButton("Gerar / Validar Calendário");
        btnGerarCal.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnGerarCal.setBackground(new Color(0xD3D3D3));
        btnGerarCal.setOpaque(true); btnGerarCal.setBorderPainted(false);
        btnGerarCal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnGerarCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnGerarCal.addActionListener(e -> {
            try {
                java.util.List<String> conflitos = torneioControlador.validarCalendario();
                if (conflitos.isEmpty()) {
                    torneioControlador.confirmarValidacao();
                    torneioControlador.iniciarTorneio();
                    JOptionPane.showMessageDialog(this, "Calendário validado e torneio iniciado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    tabs.setSelectedIndex(3); // Salta para Calendário após iniciar
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
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel canvasBranco = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(); int h = getHeight();
                int x1 = 20, x2 = w / 3, x3 = (w / 3) * 2, x4 = w - 40;

                // 1. DESENHAR AS LINHAS DA BRACKET
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));

                int[] yPos = {h/8, (h/8)*3, (h/8)*5, (h/8)*7};
                for (int y : yPos) {
                    g2.drawLine(x1, y - 20, x2, y - 20); // Linha cima
                    g2.drawLine(x1, y + 20, x2, y + 20); // Linha baixo
                    g2.drawLine(x2, y - 20, x2, y + 20); // Fecho vertical
                }
                g2.drawLine(x2, yPos[0], x3, yPos[0]);
                g2.drawLine(x2, yPos[1], x3, yPos[1]);
                g2.drawLine(x3, yPos[0], x3, yPos[1]); // Fecho Meia 1

                g2.drawLine(x2, yPos[2], x3, yPos[2]);
                g2.drawLine(x2, yPos[3], x3, yPos[3]);
                g2.drawLine(x3, yPos[2], x3, yPos[3]); // Fecho Meia 2

                int meio1 = (yPos[0] + yPos[1]) / 2;
                int meio2 = (yPos[2] + yPos[3]) / 2;
                g2.drawLine(x3, meio1, x4, meio1);
                g2.drawLine(x3, meio2, x4, meio2);
                g2.drawLine(x4, meio1, x4, meio2); // Fecho Final
                g2.drawLine(x4, h/2, w - 10, h/2); // Linha Vencedor

                // 2. ESCREVER OS NOMES DAS EQUIPAS DINAMICAMENTE
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.setColor(new Color(0x333333)); // Cinza escuro para o texto

                // Vai buscar todos os jogos ao controlador
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> todosJogos = jogoControlador.getJogos();

                // --- QUARTOS DE FINAL ---
                int[] yQuartosTexto = {yPos[0]-25, yPos[0]+15, yPos[1]-25, yPos[1]+15, yPos[2]-25, yPos[2]+15, yPos[3]-25, yPos[3]+15};
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> jogosQuartos = todosJogos.stream()
                        .filter(j -> j.getFase() != null && j.getFase().name().equals("QUARTOS"))
                        .toList();

                for (int i = 0; i < Math.min(jogosQuartos.size(), 4); i++) {
                    g2.drawString(jogosQuartos.get(i).getEquipaCasa().getNome(), x1 + 5, yQuartosTexto[i*2]);
                    g2.drawString(jogosQuartos.get(i).getEquipaFora().getNome(), x1 + 5, yQuartosTexto[(i*2) + 1]);
                }

                // --- MEIAS FINAIS ---
                int[] yMeiasTexto = {yPos[0]-5, yPos[1]-5, yPos[2]-5, yPos[3]-5};
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> jogosMeias = todosJogos.stream()
                        .filter(j -> j.getFase() != null && j.getFase().name().equals("MEIAS"))
                        .toList();

                for (int i = 0; i < Math.min(jogosMeias.size(), 2); i++) {
                    g2.drawString(jogosMeias.get(i).getEquipaCasa().getNome(), x2 + 5, yMeiasTexto[i*2]);
                    g2.drawString(jogosMeias.get(i).getEquipaFora().getNome(), x2 + 5, yMeiasTexto[(i*2) + 1]);
                }

                // --- FINAL ---
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo> jogosFinal = todosJogos.stream()
                        .filter(j -> j.getFase() != null && j.getFase().name().equals("FINAL"))
                        .toList();

                if (!jogosFinal.isEmpty()) {
                    g2.drawString(jogosFinal.get(0).getEquipaCasa().getNome(), x3 + 5, meio1 - 5);
                    g2.drawString(jogosFinal.get(0).getEquipaFora().getNome(), x3 + 5, meio2 - 5);

                    // --- VENCEDOR ---
                    String vencedor = jogoControlador.obterVencedorTorneio();
                    if (vencedor != null && !vencedor.contains("TBD") && !vencedor.contains("Torneio")) {
                        g2.setColor(new Color(0x27AE60)); // Fica a verde se houver campeão!
                        g2.drawString("🏆 " + vencedor, x4 + 5, (h/2) - 5);
                    }
                }
            }
        };
        canvasBranco.setBackground(COR_BRANCO);
        painel.add(canvasBranco, BorderLayout.CENTER);
        return painel;
    }


    private JPanel criarColunaDireitaAutomatica() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);

        // AGORA É DINÂMICO! Puxa os dados dos controladores
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

        return painel;
    }

    // (MANTÉM OS MÉTODOS criarCaixaCinza, criarCartaoEstatistica, criarLinha... AQUI POR BAIXO)

    // ── COMPONENTES VISUAIS (DESIGN SYSTEM) ───────────────────────────────────

    private JPanel criarCaixaCinza(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(0xE0E0E0)); // Cinza fundo
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCCCCCC), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitulo);
        p.add(Box.createVerticalStrut(10));
        return p;
    }

    private JPanel criarCartaoEstatistica(String tituloL, String tituloR, String valorL, String valorR) {
        JPanel cartao = new JPanel(new BorderLayout());
        cartao.setBackground(new Color(0xE0E0E0));
        cartao.setBorder(new EmptyBorder(5, 5, 5, 5));
        cartao.setMaximumSize(new Dimension(300, 80));

        // Metade superior amarela
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0xDFCA38));
        top.setBorder(new EmptyBorder(5, 10, 5, 10));
        JLabel lblTopL = new JLabel("<html><b>" + tituloL.replace(" ", "<br>") + "</b></html>");
        JLabel lblTopR = new JLabel("<html><b>" + tituloR.replace(" ", "<br>") + "</b></html>");
        top.add(lblTopL, BorderLayout.WEST);
        top.add(lblTopR, BorderLayout.EAST);

        // Metade inferior branca
        JPanel bot = new JPanel(new BorderLayout());
        bot.setBackground(COR_BRANCO);
        bot.setBorder(new EmptyBorder(5, 10, 5, 10));
        bot.add(new JLabel(valorL), BorderLayout.WEST);
        bot.add(new JLabel(valorR), BorderLayout.EAST);

        cartao.add(top, BorderLayout.NORTH);
        cartao.add(bot, BorderLayout.CENTER);
        return cartao;
    }

    private JLabel criarLinhaDestaqueAmarela(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(0xDFCA38));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setBorder(new EmptyBorder(5, 10, 5, 10));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return lbl;
    }

    private JLabel criarLinhaBranca(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setOpaque(true);
        lbl.setBackground(COR_BRANCO);
        lbl.setBorder(new EmptyBorder(5, 10, 5, 10));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return lbl;
    }

    private JLabel criarLinhaBrancaTotal(String texto) {
        JLabel lbl = criarLinhaBranca(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        return lbl;
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