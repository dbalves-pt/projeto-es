package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.JogadorControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PainelEquipas extends JPanel {

    private final EquipaControlador equipaControlador;
    private final JogadorControlador jogadorControlador;

    private DefaultListModel<Equipa> listModel;
    private JList<Equipa> listaEquipas;
    // Variáveis para a Coluna do Meio
    private JPanel painelGruposContainer;
    private JLabel lblNomeEquipaMeio;
    private DefaultListModel<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador> listModelJogadores;
    private JList<pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador> listaJogadoresMeio;
    private Equipa equipaSelecionadaAtual = null;

    // Cores do Figma
    private static final Color COR_FUNDO_CINZA = new Color(0xE0E0E0);
    private static final Color COR_BRANCO = Color.WHITE;

    public PainelEquipas(EquipaControlador equipaControlador, JogadorControlador jogadorControlador) {
        this.equipaControlador = equipaControlador;
        this.jogadorControlador = jogadorControlador;
        construirUI();

        // ── NOVO: O "Ouvinte" que acorda o painel ──
        // Sempre que o utilizador clicar na aba "Equipas" e ela ficar visível,
        // força a atualização da coluna da direita (Grupos).
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                atualizarTabelasGrupos();
            }
        });
    }

    private void construirUI() {
        setBackground(COR_BRANCO);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // A Magia das 3 Colunas: 1 linha, 3 colunas, 30px de espaço entre elas
        setLayout(new GridLayout(1, 3, 30, 0));

        add(criarColunaEsquerda());
        add(criarColunaMeio());
        add(criarColunaDireita());

        atualizarLista();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  COLUNA 1: Lista de Equipas
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarColunaEsquerda() {
        JPanel painel = criarCartaoCinza();

        JLabel lblTitulo = new JLabel("Equipas");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(15));

        listModel = new DefaultListModel<>();
        listaEquipas = new JList<>(listModel);
        listaEquipas.setFixedCellHeight(30);

        // ── NOVO COMPORTAMENTO DOS CLIQUES ──
        // 1. Clique Simples: Atualiza o painel do meio
        listaEquipas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaEquipas.getSelectedValue() != null) {
                atualizarDetalhesEquipa(listaEquipas.getSelectedValue());
            }
        });

        // 2. Duplo Clique: Abre a janela de Edição (FormularioEquipa)
        listaEquipas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    // ── VERIFICAÇÃO DE TORNEIO EM CURSO ──
                    if (equipaControlador.isEdicaoBloqueada()) {
                        JOptionPane.showMessageDialog(PainelEquipas.this,
                                "O calendário já foi gerado. Não é possível editar os dados das equipas!",
                                "Ação Bloqueada", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Equipa eq = listaEquipas.getSelectedValue();
                    if (eq != null) {
                        FormularioEquipa form = new FormularioEquipa(
                                SwingUtilities.getWindowAncestor(PainelEquipas.this),
                                equipaControlador, jogadorControlador, eq,
                                PainelEquipas.this::atualizarListasAposEdicao);
                        form.setVisible(true);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(listaEquipas);
        scroll.setBorder(null);
        painel.add(scroll);

        JButton btnInserir = new JButton("Inserir equipa...");
        btnInserir.setHorizontalAlignment(SwingConstants.LEFT);
        btnInserir.setContentAreaFilled(false);
        btnInserir.setBorder(new EmptyBorder(10, 5, 10, 5));
        btnInserir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnInserir.addActionListener(e -> {
            // ── VERIFICAÇÃO DE TORNEIO EM CURSO ──
            if (equipaControlador.isEdicaoBloqueada()) {
                JOptionPane.showMessageDialog(this,
                        "O calendário já foi gerado e o torneio iniciado. Não é possível inserir novas equipas!",
                        "Ação Bloqueada", JOptionPane.ERROR_MESSAGE);
                return;
            }

            FormularioEquipa form = new FormularioEquipa(
                    SwingUtilities.getWindowAncestor(this), equipaControlador, jogadorControlador, this::atualizarListasAposEdicao);
            form.setVisible(true);
        });
        painel.add(btnInserir);

        return painel;
    }
    // ══════════════════════════════════════════════════════════════════════════
    //  COLUNA 2: Detalhes da Equipa (A preencher no futuro)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarColunaMeio() {
            JPanel painel = criarCartaoCinza();

            lblNomeEquipaMeio = new JLabel("Selecione uma equipa...");
            lblNomeEquipaMeio.setFont(new Font("SansSerif", Font.BOLD, 18));
            lblNomeEquipaMeio.setAlignmentX(Component.LEFT_ALIGNMENT);
            painel.add(lblNomeEquipaMeio);
            painel.add(Box.createVerticalStrut(15));

            JLabel lblJogadores = new JLabel("Jogadores");
            lblJogadores.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblJogadores.setAlignmentX(Component.LEFT_ALIGNMENT);
            painel.add(lblJogadores);
            painel.add(Box.createVerticalStrut(5));

            // ── NOVA LISTA DE JOGADORES SELECIONÁVEL ──
            listModelJogadores = new DefaultListModel<>();
            listaJogadoresMeio = new JList<>(listModelJogadores);
            listaJogadoresMeio.setFixedCellHeight(25);
            // Formatar o aspeto de cada jogador na lista
        // ── Formatar o aspeto de cada jogador na lista (COM ESTADO) ──
        listaJogadoresMeio.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador) {
                    pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador j = (pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador) value;

                    // Define o texto: Nome + (Estado)
                    String estadoTexto = j.getEstado().toString(); // Assumindo que o enum tem .toString() que devolve "Apto"/"Inapto"
                    label.setText(String.format("  %s (%s)", j.getNomeCompleto(), estadoTexto));

                    // Colore a vermelho se estiver Inapto, preto caso contrário
                    if (j.getEstado() == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador.Estado.INAPTO) {
                        label.setForeground(Color.RED);
                        label.setFont(label.getFont().deriveFont(Font.BOLD)); // Destaque visual
                    } else {
                        label.setForeground(Color.BLACK);
                        label.setFont(label.getFont().deriveFont(Font.PLAIN));
                    }
                }
                label.setBorder(new EmptyBorder(0, 5, 0, 5));
                return label;
            }
        });

            JScrollPane scrollJogadores = new JScrollPane(listaJogadoresMeio);
            scrollJogadores.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            scrollJogadores.setAlignmentX(Component.LEFT_ALIGNMENT);
            painel.add(scrollJogadores);

            // ── NOVO: Duplo Clique para Editar Jogador ──
            // ── Duplo Clique para Editar Jogador (Atualizado) ──
            listaJogadoresMeio.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador j = listaJogadoresMeio.getSelectedValue();
                        if (j != null && equipaSelecionadaAtual != null) {
                            pt.ipleiria.estg.dei.ei.esoft.vista.FormularioJogador form = new pt.ipleiria.estg.dei.ei.esoft.vista.FormularioJogador(
                                    SwingUtilities.getWindowAncestor(PainelEquipas.this),
                                    jogadorControlador, equipaSelecionadaAtual, j, PainelEquipas.this::atualizarListasAposEdicao
                            );
                            form.setVisible(true);
                        }
                    }
                }
            });
            // ─────────────────────────────────────────────

            painel.add(Box.createVerticalStrut(10));

            // ── BOTÕES DOS JOGADORES ──
            // ── BOTÕES DOS JOGADORES ──
            JButton btnInserirJog = new JButton("Inserir jogador...");
            btnInserirJog.setContentAreaFilled(false);
            btnInserirJog.setBorder(new EmptyBorder(5, 0, 5, 0));
            btnInserirJog.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnInserirJog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnInserirJog.addActionListener(e -> {

                // ── NOVA VERIFICAÇÃO: BLOQUEIA APENAS SE A BOLA JÁ ROLOU NO 1º JOGO ──
                if (jogadorControlador.isMercadoFechado()) {
                    JOptionPane.showMessageDialog(this,
                            "O primeiro jogo já começou! A inscrição de novos jogadores está encerrada.",
                            "Mercado Fechado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (equipaSelecionadaAtual != null) {
                    pt.ipleiria.estg.dei.ei.esoft.vista.FormularioJogador form = new pt.ipleiria.estg.dei.ei.esoft.vista.FormularioJogador(
                            SwingUtilities.getWindowAncestor(this), jogadorControlador, equipaSelecionadaAtual, null, this::atualizarListasAposEdicao);
                    form.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Selecione uma equipa primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            });
            painel.add(btnInserirJog);

            JButton btnEliminarJog = new JButton("Eliminar jogador...");
            btnEliminarJog.setContentAreaFilled(false);
            btnEliminarJog.setBorder(new EmptyBorder(0, 0, 15, 0)); // Espaço em baixo para separar da Eliminar Equipa
            btnEliminarJog.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnEliminarJog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnEliminarJog.addActionListener(e -> {

                // ── NOVA VERIFICAÇÃO: BLOQUEIA APENAS SE A BOLA JÁ ROLOU NO 1º JOGO ──
                if (jogadorControlador.isMercadoFechado()) {
                    JOptionPane.showMessageDialog(this,
                            "Não é possível eliminar jogadores depois do primeiro jogo arrancar!",
                            "Mercado Fechado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador j = listaJogadoresMeio.getSelectedValue();
                if (j != null && equipaSelecionadaAtual != null) {
                    int confirmacao = JOptionPane.showConfirmDialog(this,
                            "Tem a certeza que deseja eliminar o jogador " + j.getNomeCompleto() + "?",
                            "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (confirmacao == JOptionPane.YES_OPTION) {
                        // 1. Elimina no controlador
                        jogadorControlador.eliminarJogador(equipaSelecionadaAtual, j);

                        // 2. Atualiza os dados no modelo da lista direita imediatamente
                        listModelJogadores.removeElement(j);

                        // 3. Atualiza a lista da esquerda para refletir alterações se necessário
                        atualizarLista();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Selecione um jogador na lista branca primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            });
            painel.add(btnEliminarJog);

            // Espaço elástico para empurrar o "Eliminar Equipa" para o fundo
            painel.add(Box.createVerticalGlue());
            return painel;
        }


    // ══════════════════════════════════════════════════════════════════════════
    //  COLUNA 3: Visão Geral dos Grupos (Design Estático Provisório)
    // ══════════════════════════════════════════════════════════════════════════
    // ══════════════════════════════════════════════════════════════════════════
    //  COLUNA 3: Visão Geral dos Grupos (Dinâmica)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel criarColunaDireita() {
        painelGruposContainer = criarCartaoCinza();
        atualizarTabelasGrupos(); // Chama o método que desenha as tabelas reais
        return painelGruposContainer;
    }

    /**
     * Limpa o painel da direita e desenha as tabelas de classificação
     * baseadas nos dados reais do sistema em tempo real.
     */
    /**
     * Limpa o painel da direita e desenha as tabelas de classificação
     * baseadas nos dados reais do sistema em tempo real.
     */
    /**
     * Limpa o painel da direita e desenha as tabelas de classificação
     * baseadas nos dados reais do sistema em tempo real.
     */
    public void atualizarTabelasGrupos() {
        painelGruposContainer.removeAll(); // Limpa as tabelas antigas

        JLabel lblTitulo = new JLabel("Grupos");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelGruposContainer.add(lblTitulo);
        painelGruposContainer.add(Box.createVerticalStrut(15));

        pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio torneio = pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.getInstancia();

        // Se o torneio ainda não começou, mostra apenas um aviso limpo
        if (torneio.getEstado() == pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.Estado.INICIAL ||
                torneio.getEstado() == pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.Estado.CONFIGURADO) {

            JLabel lblAviso = new JLabel("O calendário ainda não foi gerado.");
            lblAviso.setFont(new Font("SansSerif", Font.ITALIC, 13));
            lblAviso.setForeground(Color.GRAY);
            painelGruposContainer.add(lblAviso);

        } else {
            // ── O CALENDÁRIO EXISTE: VAMOS BUSCAR A MESMA LÓGICA DO PAINEL CALENDÁRIO ──
            String[] colunas = {"P", "Equipa", "GD", "GM", "PTS"};

            // Instanciamos os controladores apenas para ir ler a base de dados
            pt.ipleiria.estg.dei.ei.esoft.controlador.TorneioControlador tc = new pt.ipleiria.estg.dei.ei.esoft.controlador.TorneioControlador();
            pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador jc = new pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador();

            java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo> grupos = tc.getGrupos();

            for (pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo grupo : grupos) {
                // Título do Grupo
                JLabel lblGrupo = new JLabel("Grupo " + grupo.getNome());
                lblGrupo.setFont(new Font("SansSerif", Font.BOLD, 14));
                lblGrupo.setAlignmentX(Component.LEFT_ALIGNMENT);
                painelGruposContainer.add(lblGrupo);
                painelGruposContainer.add(Box.createVerticalStrut(5));

                // Vai buscar a classificação oficial que já usas no calendário
                java.util.List<pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo.LinhaClassificacao> classificacao = jc.getClassificacao(grupo);

                // Verifica se já há jogos terminados para saber se mete "–" ou os golos a sério
                boolean grupoTemJogosTerminados = jc.getJogosPorGrupo(grupo).stream()
                        .anyMatch(j -> j.getEstado() == pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Estado.TERMINADO);

                Object[][] dados = new Object[classificacao.size()][5];
                int pos = 0;

                for (pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo.LinhaClassificacao linha : classificacao) {
                    dados[pos][0] = String.valueOf(pos + 1); // Posição
                    dados[pos][1] = linha.getEquipa().getNome();

                    if (grupoTemJogosTerminados) {
                        dados[pos][2] = linha.getDiferencaGolos();
                        dados[pos][3] = linha.getGolosFormatado();
                        dados[pos][4] = String.valueOf(linha.getPontos());
                    } else {
                        dados[pos][2] = "–";
                        dados[pos][3] = "–";
                        dados[pos][4] = "0";
                    }
                    pos++;
                }

                // Cria a tabela visual e adiciona
                painelGruposContainer.add(criarTabelaGrupo(dados, colunas));
                painelGruposContainer.add(Box.createVerticalStrut(15));
            }
        }

        painelGruposContainer.add(Box.createVerticalGlue()); // Empurra para cima
        painelGruposContainer.revalidate();
        painelGruposContainer.repaint();
    }


    // ── Utilitário para desenhar o fundo cinzento arredondado ──────────────
    private JPanel criarCartaoCinza() {
        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_FUNDO_CINZA);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return painel;
    }

    private void atualizarLista() {
        listModel.clear();
        for (Equipa eq : equipaControlador.getEquipas()) {
            listModel.addElement(eq);
        }
    }

    // ── Lógica de Atualização da Interface ──

    private void atualizarDetalhesEquipa(Equipa eq) {
        equipaSelecionadaAtual = eq;
        listModelJogadores.clear();

        if (eq == null) {
            lblNomeEquipaMeio.setText("Selecione uma equipa...");
        } else {
            lblNomeEquipaMeio.setText(eq.getNome());

            // Preenche a lista branca com os jogadores da equipa
            for (pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador j : eq.getJogadores()) {
                listModelJogadores.addElement(j);
            }
        }
    }

    private void atualizarListasAposEdicao() {
        // 1. Guarda a equipa que estava selecionada antes de limpar tudo
        Equipa equipaGuardada = listaEquipas.getSelectedValue();

        atualizarLista(); // Atualiza a lista da esquerda (isto limpa a seleção)

        // 2. Se havia uma equipa selecionada, volta a procurá-la e a selecioná-la
        if (equipaGuardada != null) {
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).getNome().equals(equipaGuardada.getNome())) {
                    listaEquipas.setSelectedIndex(i); // Isto vai disparar a atualização do meio automaticamente!
                    break;
                }
            }
        } else {
            atualizarDetalhesEquipa(null);
        }
    }

    // ── Utilitário para desenhar as tabelas dos grupos (Estilo Figma) ─────────
    private JPanel criarTabelaGrupo(Object[][] dados, String[] colunas) {
        JTable tabela = new JTable(dados, colunas);
        tabela.setEnabled(false); // Impede que o utilizador edite as células
        tabela.setRowHeight(25);
        tabela.setShowGrid(false); // Remove as linhas de grelha
        tabela.setBackground(Color.WHITE);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Estilizar cabeçalho para ficar cinza como no Figma
        tabela.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabela.getTableHeader().setForeground(Color.BLACK);
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabela.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Ajustar largura das colunas
        tabela.getColumnModel().getColumn(0).setPreferredWidth(25);  // P
        tabela.getColumnModel().getColumn(1).setPreferredWidth(120); // Equipa (Mais larga)
        tabela.getColumnModel().getColumn(2).setPreferredWidth(35);  // GD
        tabela.getColumnModel().getColumn(3).setPreferredWidth(40);  // GM
        tabela.getColumnModel().getColumn(4).setPreferredWidth(35);  // PTS

        // Colocar a tabela dentro de um painel arredondado branco
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setPreferredSize(new Dimension(300, 125)); // Altura exata para 4 equipas
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        scroll.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5, true));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel painelTabela = new JPanel(new BorderLayout());
        painelTabela.setOpaque(false);
        painelTabela.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelTabela.add(scroll, BorderLayout.CENTER);

        return painelTabela;
    }
}