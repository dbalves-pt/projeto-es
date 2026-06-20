package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EventoControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.TorneioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Grupo;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista do separador 'Calendário' — suporta UC09 (Registar Início de Jogo,
 * acedido a partir da tabela de jogos) e UC18 (Consultar Classificação da
 * Fase de Grupos, no painel esquerdo).
 *
 * Layout (réplica do protótipo):
 *   • Painel esquerdo 'Grupos' — lista os grupos do torneio, cada um com a
 *     respetiva tabela de classificação (P, Equipa, GD, GM, PTS).
 *   • Painel direito 'Jogos' — tabela de jogos (ID, Jogo, Data, Estádio,
 *     Estado); duplo clique abre o ecrã 'Detalhes do Jogo' (UC09/UC11).
 */
public class PainelCalendario extends JPanel {

    private static final Color COR_FUNDO_CINZA = new Color(0xE0E0E0);
    private static final Color COR_BRANCO      = Color.WHITE;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TorneioControlador torneioControlador;
    private final JogoControlador    jogoControlador;
    private final EventoControlador  eventoControlador;

    private JPanel painelGrupos;       // reconstruído a cada atualização
    private JTable tabelaJogos;
    private DefaultTableModel modeloTabelaJogos;
    private List<Jogo> jogosListados;  // mantém correspondência linha -> Jogo

    private JButton btnEliminatorias;  // NOVO BOTÃO

    public PainelCalendario(TorneioControlador torneioControlador,
                             JogoControlador jogoControlador,
                             EventoControlador eventoControlador) {
        this.torneioControlador = torneioControlador;
        this.jogoControlador = jogoControlador;
        this.eventoControlador = eventoControlador;
        construirUI();
        atualizar();
    }

    private void construirUI() {
        setBackground(COR_BRANCO);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.35; gbc.insets = new Insets(0, 0, 0, 20);
        add(criarColunaGrupos(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.65; gbc.insets = new Insets(0, 20, 0, 0);
        add(criarColunaJogos(), gbc);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAINEL ESQUERDO — Grupos / Classificação (UC18)
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarColunaGrupos() {
        JPanel exterior = criarCartaoCinza();

        JLabel titulo = new JLabel("Grupos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        exterior.add(titulo);
        exterior.add(Box.createVerticalStrut(15));

        painelGrupos = new JPanel();
        painelGrupos.setLayout(new BoxLayout(painelGrupos, BoxLayout.Y_AXIS));
        painelGrupos.setOpaque(false);
        painelGrupos.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scroll = new JScrollPane(painelGrupos);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        exterior.add(scroll);

        return exterior;
    }

    private void reconstruirPainelGrupos() {
        painelGrupos.removeAll();

        List<Grupo> grupos = torneioControlador.getGrupos();
        if (grupos.isEmpty()) {
            JLabel msg = new JLabel("Os grupos ainda não foram gerados.");
            msg.setFont(new Font("SansSerif", Font.ITALIC, 12));
            msg.setAlignmentX(Component.LEFT_ALIGNMENT);
            painelGrupos.add(msg);
        } else {
            for (Grupo grupo : grupos) {
                painelGrupos.add(criarTabelaGrupo(grupo));
                painelGrupos.add(Box.createVerticalStrut(15));
            }
        }
        painelGrupos.revalidate();
        painelGrupos.repaint();
    }

    private JPanel criarTabelaGrupo(Grupo grupo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nomeGrupo = new JLabel(grupo.getNome());
        nomeGrupo.setFont(new Font("SansSerif", Font.BOLD, 14));
        nomeGrupo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(nomeGrupo);
        painel.add(Box.createVerticalStrut(5));

        String[] colunas = {"P", "Equipa", "GD", "GM", "PTS"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        boolean grupoTemJogosTerminados = jogoControlador.getJogosPorGrupo(grupo).stream()
                .anyMatch(j -> j.getEstado() == Jogo.Estado.TERMINADO);

        List<Grupo.LinhaClassificacao> classificacao = jogoControlador.getClassificacao(grupo);
        int posicao = 1;
        for (Grupo.LinhaClassificacao linha : classificacao) {
            if (grupoTemJogosTerminados) {
                modelo.addRow(new Object[]{
                        posicao, linha.getEquipa().getNome(),
                        linha.getDiferencaGolos(), linha.getGolosFormatado(), linha.getPontos()
                });
            } else {
                modelo.addRow(new Object[]{posicao, linha.getEquipa().getNome(), "–", "–", 0});
            }
            posicao++;
        }

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(22);
        tabela.setShowGrid(false);
        tabela.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabela.setPreferredScrollableViewportSize(
                new Dimension(280, tabela.getRowHeight() * Math.max(classificacao.size(), 1)));

        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        scrollTabela.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(scrollTabela);

        if (!grupoTemJogosTerminados) {
            JLabel msg = new JLabel("Ainda não foram disputados jogos neste grupo.");
            msg.setFont(new Font("SansSerif", Font.ITALIC, 11));
            msg.setAlignmentX(Component.LEFT_ALIGNMENT);
            painel.add(msg);
        }

        return painel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAINEL DIREITO — Tabela de Jogos (UC09)
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarColunaJogos() {
        JPanel painel = criarCartaoCinza();

        JLabel titulo = new JLabel("Jogos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(15));

        String[] colunas = {"ID", "Jogo", "Data", "Estádio", "Estado"};
        modeloTabelaJogos = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tabelaJogos = new JTable(modeloTabelaJogos);
        tabelaJogos.setRowHeight(26);
        tabelaJogos.setShowGrid(false);
        tabelaJogos.getTableHeader().setBackground(new Color(0xBDBDBD));
        tabelaJogos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        tabelaJogos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) abrirDetalhesDoJogoSelecionado();
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabelaJogos);
        scrollTabela.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        scrollTabela.getViewport().setBackground(COR_BRANCO);
        scrollTabela.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(scrollTabela);
        painel.add(Box.createVerticalStrut(10));

        // ── NOVO: Painel de rodapé com a dica à esquerda e o Botão à direita ──
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dica = new JLabel("Duplo clique num jogo para ver os detalhes.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        rodape.add(dica, BorderLayout.WEST);

        btnEliminatorias = new JButton("Gerar Fase Eliminatória");
        btnEliminatorias.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnEliminatorias.setBackground(new Color(0x3498DB)); // Azul ativo
        btnEliminatorias.setForeground(Color.WHITE);
        btnEliminatorias.setFocusPainted(false);
        btnEliminatorias.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnEliminatorias.addActionListener(e -> {
            try {
                torneioControlador.gerarProximaFase();
                atualizar(); // Recarrega o calendário para mostrar os novos jogos

                // Força a aba Principal a atualizar o Dashboard (Bracket)
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JanelaPrincipal) {
                    ((JanelaPrincipal) window).atualizarEstatisticas();
                }

                JOptionPane.showMessageDialog(this,
                        "Fase Eliminatória gerada com sucesso!\nVerifique a nova grelha na Página Principal.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        rodape.add(btnEliminatorias, BorderLayout.EAST);
        painel.add(rodape);

        return painel;
    }

    private void abrirDetalhesDoJogoSelecionado() {
        int linha = tabelaJogos.getSelectedRow();
        if (linha == -1 || jogosListados == null || linha >= jogosListados.size()) return;

        Jogo jogo = jogosListados.get(linha);

        // Callback que atualiza o calendário E as estatísticas
        Runnable aoAtualizarTudo = () -> {
            PainelCalendario.this.atualizar();  // atualiza a tabela de jogos e grupos

            // Atualiza também a aba Jogadores se a janela principal estiver acessível
            Window window = SwingUtilities.getWindowAncestor(PainelCalendario.this);
            if (window instanceof JanelaPrincipal) {
                ((JanelaPrincipal) window).atualizarEstatisticas();
            }
        };

        FormularioDetalhesJogo form = new FormularioDetalhesJogo(
                SwingUtilities.getWindowAncestor(this),
                jogoControlador,
                eventoControlador,
                jogo,
                aoAtualizarTudo
        );
        form.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Actualização geral
    // ══════════════════════════════════════════════════════════════════════════

    public void atualizar() {
        reconstruirPainelGrupos();
        atualizarTabelaJogos();
    }

    private void atualizarTabelaJogos() {
        modeloTabelaJogos.setRowCount(0);
        jogosListados = jogoControlador.getJogos();

        int id = 1;
        pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase faseMaisAvancada = pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase.GRUPOS;

        for (Jogo jogo : jogosListados) {
            String descricaoJogo = jogo.getEquipaCasa().getNome() + " x " + jogo.getEquipaFora().getNome();
            modeloTabelaJogos.addRow(new Object[]{
                    id++,
                    descricaoJogo,
                    jogo.getDataHora().format(FORMATO_DATA),
                    jogo.getEstadio().getNome(),
                    formatarEstado(jogo)
            });

            // Encontrar a fase mais avançada existente no calendário
            if (jogo.getFase() == Jogo.Fase.FINAL) faseMaisAvancada = Jogo.Fase.FINAL;
            else if (jogo.getFase() == Jogo.Fase.MEIAS && faseMaisAvancada != Jogo.Fase.FINAL)
                faseMaisAvancada = Jogo.Fase.MEIAS;
            else if (jogo.getFase() == Jogo.Fase.QUARTOS && faseMaisAvancada != Jogo.Fase.FINAL && faseMaisAvancada != Jogo.Fase.MEIAS)
                faseMaisAvancada = Jogo.Fase.QUARTOS;
        }

        // Verificar se todos os jogos dessa "fase mais avançada" estão terminados
        pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Fase fVerificar = faseMaisAvancada;
        boolean todosDestaFaseTerminados = jogosListados.stream()
                .filter(j -> j.getFase() == fVerificar)
                .allMatch(j -> j.getEstado() == Jogo.Estado.TERMINADO);

        // Controlar o estado e o texto do Botão dinamicamente
        if (btnEliminatorias != null) {
            boolean torneioAcabou = (faseMaisAvancada == Jogo.Fase.FINAL && todosDestaFaseTerminados);
            boolean podeGerar = todosDestaFaseTerminados && jogosListados.size() > 0;

            btnEliminatorias.setEnabled(podeGerar);

            if (torneioAcabou) {
                btnEliminatorias.setText("Ver Vencedor \uD83C\uDFC6");
                btnEliminatorias.setBackground(new Color(0x4CAF50)); // Verde vitória
            } else {
                btnEliminatorias.setText("Gerar Próxima Fase");
                btnEliminatorias.setBackground(podeGerar ? new Color(0x3498DB) : Color.GRAY); // Azul se ativo, Cinza se bloqueado
            }
        }
    }

    private String formatarEstado(Jogo jogo) {
        return switch (jogo.getEstado()) {
            case CALENDARIZADO -> "Calendarizado";
            case COMECADO -> "Em curso";
            case TERMINADO -> jogo.getGolosCasa() + " - " + jogo.getGolosFora() + " (Terminado)";
        };
    }

    private JPanel criarCartaoCinza() {
        JPanel painel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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
}
