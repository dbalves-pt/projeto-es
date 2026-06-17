package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EstadioControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.JogadorControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class JanelaPrincipal extends JFrame {

    private final EquipaControlador equipaControlador;
    private final JogadorControlador jogadorControlador;
    private DefaultListModel<Equipa> listModel;
    private JList<Equipa> listaEquipas;
    private final EstadioControlador estadioControlador;
    // ── Variáveis para podermos ler os dados antes de gerar ──
    private JTextField campDataInicio;
    private JTextField campDataFim;
    private JLabel lblErroDataInicio; // NOVO
    private JLabel lblErroDataFim;

    // Cores baseadas no teu novo Figma
    private static final Color COR_FUNDO_AZUL = new Color(0x9FE2FC); // Azul claro do cartão
    private static final Color COR_FUNDO_EXTERIOR = new Color(0xF5F5F5); // Fundo da janela (cinza claro/branco)
    private static final Color COR_BRANCO = Color.WHITE;
    private static final Color COR_CINZENTO_HEADER = new Color(0x9E9E9E);

    public JanelaPrincipal(EquipaControlador equipaControlador) {
        this.equipaControlador = equipaControlador;
        this.jogadorControlador = new JogadorControlador();
        this.estadioControlador = new EstadioControlador();
        construirUI();
    }

    private void construirUI() {
        setTitle("Gestão do Campeonato do Mundo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Fundo exterior da janela
        JPanel painelFundo = new JPanel(new GridBagLayout());
        painelFundo.setBackground(COR_FUNDO_EXTERIOR);
        setContentPane(painelFundo);

        // Criar o cartão azul do meio (estilo do novo Figma)
        JPanel cartao = criarCartaoArredondado();
        painelFundo.add(cartao);

        atualizarLista();
    }

    private JPanel criarCartaoArredondado() {
        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Desenha o fundo AZUL do cartão
                g2.setColor(COR_FUNDO_AZUL);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                // Desenha uma borda azul um pouco mais escura
                g2.setColor(new Color(0x3498DB));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(380, 520));
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título do cartão (Alinhado à esquerda)
        JPanel painelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelTitulo.setOpaque(false);
        painelTitulo.setMaximumSize(new Dimension(350, 30));
        JLabel lblTitulo = new JLabel("Criar Torneio");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        painelTitulo.add(lblTitulo);
        painel.add(painelTitulo);
        painel.add(Box.createVerticalStrut(15));

        // ── CAIXAS DE DATA (Com Validação Inline) ──
        lblErroDataInicio = criarLabelErro();
        campDataInicio = criarCampoData("Inserir Data de Início...(ex: 10/02/2000)", lblErroDataInicio);
        painel.add(campDataInicio);
        painel.add(lblErroDataInicio);
        painel.add(Box.createVerticalStrut(2)); // Espaço mais curto porque o erro ocupa espaço

        lblErroDataFim = criarLabelErro();
        campDataFim = criarCampoData("Inserir Data de Fim...(ex: 10/02/2000)", lblErroDataFim);
        painel.add(campDataFim);
        painel.add(lblErroDataFim);
        painel.add(Box.createVerticalStrut(10));

        // ── BOTÃO REGRAS (Novo) ──
        JButton btnRegras = criarBotaoFigma("Regras");
        btnRegras.setMaximumSize(new Dimension(140, 32)); // Botão mais pequeno
        painel.add(btnRegras);
        painel.add(Box.createVerticalStrut(15));

        // ── Painel da Lista de Equipas ──
        JPanel painelLista = new JPanel(new BorderLayout());
        painelLista.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        painelLista.setMaximumSize(new Dimension(350, 200));

        // Cabeçalho cinzento "Equipas"
        JLabel lblHeaderEquipas = new JLabel("  Equipas");
        lblHeaderEquipas.setOpaque(true);
        lblHeaderEquipas.setBackground(COR_CINZENTO_HEADER);
        lblHeaderEquipas.setForeground(Color.BLACK);
        lblHeaderEquipas.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblHeaderEquipas.setPreferredSize(new Dimension(350, 25));
        painelLista.add(lblHeaderEquipas, BorderLayout.NORTH);

        // A lista propriamente dita
        listModel = new DefaultListModel<>();
        listaEquipas = new JList<>(listModel);
        listaEquipas.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Ação de clique na Equipa (Abre edição)
        listaEquipas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaEquipas.getSelectedValue() != null) {
                Equipa equipaSelecionada = listaEquipas.getSelectedValue();
                FormularioEquipa form = new FormularioEquipa(
                        this, equipaControlador, jogadorControlador,
                        equipaSelecionada, this::atualizarLista
                );
                form.setVisible(true);
                listaEquipas.clearSelection();
            }
        });

        painelLista.add(new JScrollPane(listaEquipas), BorderLayout.CENTER);

        // Botão disfarçado de link "Inserir Equipa..."
        JButton btnInserir = new JButton("Inserir Equipa...");
        btnInserir.setHorizontalAlignment(SwingConstants.LEFT);
        btnInserir.setContentAreaFilled(false);
        btnInserir.setBorder(new EmptyBorder(5, 5, 5, 5));
        btnInserir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnInserir.addActionListener(e -> abrirFormularioInserir());
        painelLista.add(btnInserir, BorderLayout.SOUTH);

        painel.add(painelLista);
        painel.add(Box.createVerticalStrut(15));

        // ── Botão final Gerar Torneio ──
        JButton btnGerar = criarBotaoFigma("Gerar torneio");
        btnGerar.setMaximumSize(new Dimension(350, 35));

        // Em vez de abrir a dashboard logo, chama a nossa validação!
        btnGerar.addActionListener(e -> validarEGerarTorneio());
        painel.add(btnGerar);

        return painel;
    }

    // Cria o texto vermelho pequenino para os erros
    private JLabel criarLabelErro() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(Color.RED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JTextField criarCampoData(String placeholder, JLabel lblErro) {
        // Criamos o JTextField com desenho personalizado para o "Texto Fantasma"
        JTextField campo = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Se o campo estiver vazio, desenha o exemplo lá dentro
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));

                    // Alinha o texto perfeitamente com as bordas internas
                    int x = getInsets().left;
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                    g2.drawString(placeholder, x, y);
                    g2.dispose();
                }
            }
        };

        campo.setMaximumSize(new Dimension(350, 30));
        campo.setAlignmentX(Component.CENTER_ALIGNMENT);
        campo.setBackground(COR_BRANCO);
        campo.setForeground(Color.BLACK); // Texto digitado pelo utilizador será preto

        javax.swing.border.Border bordaNormal = BorderFactory.createCompoundBorder(
                new LineBorder(COR_BRANCO, 1, true), new EmptyBorder(5, 10, 5, 10));
        javax.swing.border.Border bordaErro = BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 2, true), new EmptyBorder(4, 9, 4, 9));

        campo.setBorder(bordaNormal);

        // Listener apenas para validar o formato quando o utilizador muda de campo
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                campo.setBorder(bordaNormal);
                lblErro.setText(" ");
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                String texto = campo.getText().trim();
                if (!texto.isEmpty()) {
                    try {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        java.time.LocalDate.parse(texto, formatter);
                    } catch (Exception ex) {
                        campo.setBorder(bordaErro);
                        lblErro.setText("Formato inválido. Ex: 12/06/2026");
                    }
                }
            }
        });
        return campo;
    }



    // Método para criar caixas de texto com fundo branco (visto que o cartão agora é azul)
    private JTextField criarCampoArredondado(String placeholder) {
        JTextField campo = new JTextField(placeholder);
        campo.setMaximumSize(new Dimension(350, 30));
        campo.setAlignmentX(Component.CENTER_ALIGNMENT);
        campo.setBackground(COR_BRANCO);
        campo.setForeground(Color.GRAY);
        // Usa CompoundBorder para dar padding interno
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_BRANCO, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // Efeito de Placeholder
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campo.getText().equals(placeholder)) { campo.setText(""); campo.setForeground(Color.BLACK); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campo.getText().isBlank()) { campo.setText(placeholder); campo.setForeground(Color.GRAY); }
            }
        });
        return campo;
    }

    // Método para criar botões brancos arredondados
    private JButton criarBotaoFigma(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BRANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setForeground(Color.BLACK);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COR_BRANCO, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void abrirFormularioInserir() {
        FormularioEquipa form = new FormularioEquipa(this, equipaControlador, jogadorControlador, this::atualizarLista);
        form.setVisible(true);
    }

    private void atualizarLista() {
        listModel.clear();
        for (Equipa eq : equipaControlador.getEquipas()) {
            listModel.addElement(eq);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TRANSIÇÃO PARA A DASHBOARD PRINCIPAL
    //  VALIDAÇÕES DE CRIAÇÃO DO TORNEIO
    // ══════════════════════════════════════════════════════════════════════════


    private void validarEGerarTorneio() {
        String strInicio = campDataInicio.getText().trim();
        String strFim = campDataFim.getText().trim();
        boolean temErro = false;

        // 1. Verifica se os campos estão vazios (ou com o texto de placeholder)
        if (strInicio.equals("Inserir Data de Início...") || strInicio.isBlank() ||
                strFim.equals("Inserir Data de Fim...") || strFim.isBlank()) {

            JOptionPane.showMessageDialog(this,
                    "Por favor, preencha as datas de início e fim do torneio.",
                    "Campos Obrigatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.border.Border bordaErro = BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 2, true), new EmptyBorder(4, 9, 4, 9));

        if (strInicio.isBlank() || !lblErroDataInicio.getText().equals(" ")) {
            campDataInicio.setBorder(bordaErro);
            lblErroDataInicio.setText("A data de início é obrigatória ou inválida.");
            temErro = true;
        }

        // 2. Valida o formato e a cronologia das datas
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate dataInicio = LocalDate.parse(strInicio, formatter);
            LocalDate dataFim = LocalDate.parse(strFim, formatter);

            if (dataFim.isBefore(dataInicio)) {
                JOptionPane.showMessageDialog(this,
                        "A data de fim não pode ser anterior à data de início!",
                        "Datas Inválidas", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Verifica se o utilizador já inseriu equipas (Opcional, mas recomendado)
            if (equipaControlador.getEquipas().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Tem de adicionar pelo menos uma equipa antes de gerar o torneio.",
                        "Sem Equipas", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Se chegou aqui, passou em todas as validações!
            // Aqui poderás guardar as datas no teu modelo Torneio.
            // ... Torneio.getInstancia().setDataInicio(dataInicio); ...

            abrirDashboard();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Formato de data inválido. Use o formato DD/MM/AAAA (ex: 12/06/2026).",
                    "Formato Inválido", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void abrirDashboard() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JTabbedPane menuTabs = new JTabbedPane();
        menuTabs.setBackground(COR_FUNDO_AZUL);
        menuTabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        menuTabs.setOpaque(true);

        menuTabs.addTab("Página Principal", new JPanel());

        PainelEquipas painelEquipas = new PainelEquipas(equipaControlador, jogadorControlador);
        menuTabs.addTab("Equipas", painelEquipas);

        PainelEstadios painelEstadios = new PainelEstadios(estadioControlador);
        menuTabs.addTab("Estádios", painelEstadios);

        menuTabs.addTab("Calendário", new JPanel());
        menuTabs.addTab("Jogadores", new JPanel());

        menuTabs.setSelectedIndex(1);

        add(menuTabs, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}