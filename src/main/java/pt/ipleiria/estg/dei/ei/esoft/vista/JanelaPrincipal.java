package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.JogadorControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class JanelaPrincipal extends JFrame {

    private final EquipaControlador equipaControlador;
    private final JogadorControlador jogadorControlador;
    private DefaultListModel<Equipa> listModel;
    private JList<Equipa> listaEquipas;

    // Cores baseadas no teu Figma
    private static final Color COR_FUNDO_AZUL = new Color(0x9FE2FC); // Azul claro do fundo
    private static final Color COR_BRANCO = Color.WHITE;
    private static final Color COR_CINZENTO_HEADER = new Color(0x9E9E9E);

    public JanelaPrincipal(EquipaControlador equipaControlador) {
        this.equipaControlador = equipaControlador;
        this.jogadorControlador = new JogadorControlador();
        construirUI();
    }

    private void construirUI() {
        setTitle("Gestão do Campeonato do Mundo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Fundo azul em toda a janela
        JPanel painelFundo = new JPanel(new GridBagLayout());
        painelFundo.setBackground(COR_FUNDO_AZUL);
        setContentPane(painelFundo);

        // Criar o cartão branco do meio (estilo Figma)
        JPanel cartaoBranco = criarCartaoArredondado();
        painelFundo.add(cartaoBranco);

        atualizarLista();
    }

    private JPanel criarCartaoArredondado() {
        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_BRANCO);
                // Desenha o fundo branco com cantos arredondados
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(400, 500));
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título do cartão
        JLabel lblTitulo = new JLabel("Criar Torneio");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(20));

        // --- Aqui mais tarde entrarão as caixas de texto do UC07 (Datas, Regras) ---

        // Painel da Lista de Equipas (O retângulo branco com cabeçalho cinzento)
        JPanel painelLista = new JPanel(new BorderLayout());
        painelLista.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        painelLista.setMaximumSize(new Dimension(350, 200));

        // Cabeçalho cinzento "Equipas"
        JLabel lblHeaderEquipas = new JLabel("  Equipas");
        lblHeaderEquipas.setOpaque(true);
        lblHeaderEquipas.setBackground(COR_CINZENTO_HEADER);
        lblHeaderEquipas.setForeground(Color.WHITE);
        lblHeaderEquipas.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblHeaderEquipas.setPreferredSize(new Dimension(350, 25));
        painelLista.add(lblHeaderEquipas, BorderLayout.NORTH);

        // A lista propriamente dita
        listModel = new DefaultListModel<>();
        listaEquipas = new JList<>(listModel);
        listaEquipas.setBorder(new EmptyBorder(5, 5, 5, 5));

        // ── ACÇÃO DE CLIQUE: O BLOCO NOVO ENTRA AQUI ──────────────────────────
        listaEquipas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaEquipas.getSelectedValue() != null) {
                Equipa equipaSelecionada = listaEquipas.getSelectedValue();

                // Abre o formulário em modo de Edição (passando a equipa selecionada)
                FormularioEquipa form = new FormularioEquipa(
                        this,
                        equipaControlador,
                        jogadorControlador,
                        equipaSelecionada,
                        this::atualizarLista
                );
                form.setVisible(true);

                // Limpa a seleção para poderes clicar nela outra vez mais tarde
                listaEquipas.clearSelection();
            }
        });
        // ──────────────────────────────────────────────────────────────────────

        painelLista.add(new JScrollPane(listaEquipas), BorderLayout.CENTER);

        // Botão disfarçado de link "Inserir Equipa..." no fundo da lista
        JButton btnInserir = new JButton("Inserir Equipa...");
        btnInserir.setHorizontalAlignment(SwingConstants.LEFT);
        btnInserir.setContentAreaFilled(false);
        btnInserir.setBorder(new EmptyBorder(5, 5, 5, 5));
        btnInserir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnInserir.addActionListener(e -> abrirFormularioInserir());
        painelLista.add(btnInserir, BorderLayout.SOUTH);

        painel.add(painelLista);

        // --- Botão final Gerar Torneio do UC07 ---
        JButton btnGerar = criarBotaoFigma("Gerar torneio");

        // ── ADICIONA ESTA LINHA AQUI ──
        btnGerar.addActionListener(e -> abrirDashboard());

        painel.add(btnGerar);
        return painel;
    }

    private JTextField criarCampoArredondado(String placeholder) {
        JTextField campo = new JTextField(placeholder);
        campo.setMaximumSize(new Dimension(350, 35));
        campo.setAlignmentX(Component.CENTER_ALIGNMENT);
        campo.setForeground(Color.GRAY);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return campo;
    }

    private JButton criarBotaoFigma(String texto) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(350, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
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
    // ══════════════════════════════════════════════════════════════════════════

    private void abrirDashboard() {
        // 1. Limpa o ecrã inicial (remove o fundo azul e o cartão)
        getContentPane().removeAll();
        setLayout(new BorderLayout()); // Muda o layout para ecrã inteiro

        // 2. Cria o Menu Superior de Abas (JTabbedPane)
        JTabbedPane menuTabs = new JTabbedPane();
        menuTabs.setBackground(COR_FUNDO_AZUL); // O azul claro do topo do teu Figma
        menuTabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        menuTabs.setOpaque(true);

        // 3. Cria as abas vazias por agora (vamos preenchê-las a seguir)
        menuTabs.addTab("Página Principal", new JPanel());

        // ── AQUI ESTÁ A CORREÇÃO: Chama o teu novo painel ──
        PainelEquipas painelEquipas = new PainelEquipas(equipaControlador, jogadorControlador);
        menuTabs.addTab("Equipas", painelEquipas);

        JPanel abaEstadios = new JPanel(); // Aqui vai entrar o UC06!
        abaEstadios.setBackground(Color.WHITE);
        menuTabs.addTab("Estádios", abaEstadios);

        menuTabs.addTab("Calendário", new JPanel());
        menuTabs.addTab("Jogadores", new JPanel());

        // 4. Força o programa a abrir automaticamente na aba "Equipas" (índice 1)
        menuTabs.setSelectedIndex(1);

        // 5. Adiciona a Dashboard à janela e manda o Java redesenhar o ecrã
        add(menuTabs, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}