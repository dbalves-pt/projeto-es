    package pt.ipleiria.estg.dei.ei.esoft.vista;

    import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
    import pt.ipleiria.estg.dei.ei.esoft.controlador.JogadorControlador;
    import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
    import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;

    import javax.swing.*;
    import javax.swing.border.EmptyBorder;
    import javax.swing.border.LineBorder;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.util.List;

    /**
     * Vista UC01 + UC02 — Formulário "Inserir Equipa" / "Editar Equipa"
     *
     * Design Figma:
     *   • Fundo azul claro (#AED6F1) com padding generoso
     *   • Painel branco com cantos arredondados
     *   • Título "Inserir Equipa" ou "Editar Equipa" a negrito
     *   • Campo Nome (JTextField com placeholder)
     *   • Dropdown País com seta (JComboBox)
     *   • Campo Grupo desativado (cinzento)
     *   • Secção Jogadores: header cinzento + lista de jogadores + link "Inserir jogador…"
     *   • Botão "Concluído" (branco, borda fina, arredondado)
     *
     * Modos de operação:
     *   • equipaParaEditar == null  → modo INSERIR (UC01)
     *   • equipaParaEditar != null  → modo EDITAR  (UC02), formulário pré-preenchido
     */
    public class FormularioEquipa extends JDialog {

        // ── Paleta Figma ──────────────────────────────────────────────────────────
        static final Color COR_FUNDO      = new Color(0xAED6F1);
        static final Color COR_BRANCO     = Color.WHITE;
        static final Color COR_CINZENTO   = new Color(0xBDBDBD);
        static final Color COR_CAMPO_DIS  = new Color(0xEEEEEE);
        static final Color COR_TEXTO      = new Color(0x212121);
        static final Color COR_PLACEHOLDER= new Color(0x9E9E9E);
        static final Color COR_ERRO       = new Color(0xE53935);
        static final Color COR_BORDA      = new Color(0xCCCCCC);
        static final Color COR_FOCO       = new Color(0x1976D2);

        static final Font FONTE_TITULO  = new Font("SansSerif", Font.BOLD,  16);
        static final Font FONTE_NORMAL  = new Font("SansSerif", Font.PLAIN, 13);
        static final Font FONTE_HEADER  = new Font("SansSerif", Font.BOLD,  12);
        static final Font FONTE_ERRO    = new Font("SansSerif", Font.PLAIN, 11);

        // ── Componentes ───────────────────────────────────────────────────────────
        private JTextField        campNome;
        private JComboBox<String> comboPais;
        private JTextField        campGrupo;
        private JPanel            painelListaJogadores;
        private JLabel            lblErroNome;
        private JLabel            lblErroPais;

        // ── Dependências ──────────────────────────────────────────────────────────
        private final EquipaControlador  ctrlEquipa;
        private final JogadorControlador ctrlJogador;
        private final Equipa             equipaParaEditar;   // null = inserção
        private final Runnable           aoAtualizar;

        // ══════════════════════════════════════════════════════════════════════════
        //  Construtores
        // ══════════════════════════════════════════════════════════════════════════

        /** UC01 — Inserir nova equipa. */
        public FormularioEquipa(Window owner,
                                EquipaControlador ctrlEquipa,
                                JogadorControlador ctrlJogador,
                                Runnable aoAtualizar) {
            this(owner, ctrlEquipa, ctrlJogador, null, aoAtualizar);
        }

        /** UC02 — Editar equipa existente (formulário pré-preenchido). */
        public FormularioEquipa(Window owner,
                                EquipaControlador ctrlEquipa,
                                JogadorControlador ctrlJogador,
                                Equipa equipaParaEditar,
                                Runnable aoAtualizar) {
            super(owner,
                    equipaParaEditar == null ? "Inserir Equipa" : "Editar Equipa",
                    ModalityType.APPLICATION_MODAL);
            this.ctrlEquipa       = ctrlEquipa;
            this.ctrlJogador      = ctrlJogador;
            this.equipaParaEditar = equipaParaEditar;
            this.aoAtualizar      = aoAtualizar;
            construirUI();
            if (equipaParaEditar != null) preencherFormulario();
            pack();
            setResizable(false);
            setLocationRelativeTo(owner);
        }

        // ══════════════════════════════════════════════════════════════════════════
        //  Construção da UI
        // ══════════════════════════════════════════════════════════════════════════

        private void construirUI() {
            // Camada raiz — fundo azul claro
            JPanel raiz = new JPanel(new GridBagLayout());
            raiz.setBackground(COR_FUNDO);
            raiz.setBorder(new EmptyBorder(24, 24, 24, 24));
            setContentPane(raiz);
            raiz.add(criarPainelBranco());
        }

        private JPanel criarPainelBranco() {
            // Painel branco com cantos arredondados (desenhado manualmente)
            JPanel p = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COR_BRANCO);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                    g2.dispose();
                }
            };
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(320, 420));
            p.setBorder(new EmptyBorder(22, 22, 22, 22));

            // ── Título ────────────────────────────────────────────────────────────
            boolean modoEdicao = (equipaParaEditar != null);
            JLabel titulo = new JLabel(modoEdicao ? "Editar Equipa" : "Inserir Equipa");
            titulo.setFont(FONTE_TITULO);
            titulo.setForeground(COR_TEXTO);
            titulo.setAlignmentX(LEFT_ALIGNMENT);
            p.add(titulo);
            p.add(rigidH(18));

            // ── Nome ──────────────────────────────────────────────────────────────
            campNome = criarTextField("Nome...");
            campNome.setAlignmentX(LEFT_ALIGNMENT);
            p.add(campNome);
            lblErroNome = labelErro();
            p.add(lblErroNome);
            p.add(rigidH(8));

            // ── País (dropdown) ───────────────────────────────────────────────────
            List<String> paises = ctrlEquipa.getPaisesDisponiveis();
            String[] opcoes = new String[paises.size() + 1];
            opcoes[0] = "País";
            for (int i = 0; i < paises.size(); i++) opcoes[i + 1] = paises.get(i);
            comboPais = new JComboBox<>(opcoes);
            estilizarCombo(comboPais);
            p.add(comboPais);
            lblErroPais = labelErro();
            p.add(lblErroPais);
            p.add(rigidH(8));

            // ── Grupo (desativado) ────────────────────────────────────────────────
            campGrupo = new JTextField("Grupo (atribuído automaticamente)");
            campGrupo.setFont(FONTE_NORMAL);
            campGrupo.setEnabled(false);
            campGrupo.setDisabledTextColor(new Color(0x9E9E9E));
            campGrupo.setBackground(COR_CAMPO_DIS);
            campGrupo.setBorder(camposBorda(COR_BORDA, 1));
            campGrupo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            campGrupo.setAlignmentX(LEFT_ALIGNMENT);
            p.add(campGrupo);
            p.add(rigidH(12));

            // ── Secção Jogadores ──────────────────────────────────────────────────
            p.add(criarSecaoJogadores());
            p.add(rigidH(18));

            // ── Botão Concluído ───────────────────────────────────────────────────
            p.add(criarBotaoConcluido());

            return p;
        }

        // ── Secção Jogadores ──────────────────────────────────────────────────────

        private JPanel criarSecaoJogadores() {
            JPanel secao = new JPanel();
            secao.setLayout(new BoxLayout(secao, BoxLayout.Y_AXIS));
            secao.setOpaque(false);
            secao.setAlignmentX(LEFT_ALIGNMENT);
            secao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

            // Cabeçalho cinzento
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(COR_CINZENTO);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            // ── A MAGIA ESTÁ AQUI ──
            // Forçamos o cabeçalho a alinhar à esquerda para que não empurre os de baixo!
            header.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lbl = new JLabel("  Jogadores");
            lbl.setFont(FONTE_HEADER);
            lbl.setForeground(COR_BRANCO);
            header.add(lbl, BorderLayout.CENTER);
            secao.add(header);

            // Lista de jogadores (dinâmica)
            painelListaJogadores = new JPanel();
            painelListaJogadores.setLayout(new BoxLayout(painelListaJogadores, BoxLayout.Y_AXIS));
            painelListaJogadores.setBackground(new Color(0xF5F5F5));
            painelListaJogadores.setAlignmentX(LEFT_ALIGNMENT);
            atualizarListaJogadores();
            secao.add(painelListaJogadores);

            // Painel do link
            JPanel painelLink = new JPanel(new BorderLayout());
            painelLink.setBackground(new Color(0xF5F5F5));
            painelLink.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            painelLink.setAlignmentX(LEFT_ALIGNMENT);

            JLabel linkInserir = new JLabel("  Inserir jogador...");
            linkInserir.setFont(FONTE_NORMAL);
            linkInserir.setBorder(new EmptyBorder(6, 0, 6, 0));

            if (equipaParaEditar != null) {
                // Modo Edição (UC02): Botão Azul e Clicável
                linkInserir.setForeground(new Color(0x1565C0));
                linkInserir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                linkInserir.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        abrirFormularioJogador(null);
                    }
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                        linkInserir.setForeground(new Color(0x0D47A1));
                    }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) {
                        linkInserir.setForeground(new Color(0x1565C0));
                    }
                });
            } else {
                // Modo Inserção (UC01): Botão Cinzento e Bloqueado por segurança
                linkInserir.setForeground(COR_PLACEHOLDER);
                linkInserir.setToolTipText("Guarde primeiro a equipa para poder adicionar jogadores.");
            }

            painelLink.add(linkInserir, BorderLayout.CENTER);
            secao.add(painelLink);

            return secao;
        }
        /** Atualiza o painel com a lista de jogadores da equipa (UC02/UC03). */
        /** Atualiza o painel com a lista de jogadores da equipa (UC02/UC03/UC05). */
        private void atualizarListaJogadores() {
            painelListaJogadores.removeAll();
            if (equipaParaEditar != null && !equipaParaEditar.getJogadores().isEmpty()) {
                for (Jogador j : equipaParaEditar.getJogadores()) {

                    // 1. Contentor vertical principal para este jogador
                    JPanel containerJogador = new JPanel();
                    containerJogador.setLayout(new BoxLayout(containerJogador, BoxLayout.Y_AXIS));
                    containerJogador.setOpaque(false);
                    containerJogador.setAlignmentX(LEFT_ALIGNMENT);

                    // 2. Painel horizontal (Nome à esquerda, Botão Lesão à direita)
                    JPanel linhaJogador = new JPanel(new BorderLayout());
                    linhaJogador.setOpaque(false);
                    linhaJogador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                    boolean isInapto = j.getEstado().toString().equalsIgnoreCase("Inapto");
                    String textoEstado = isInapto ? "  [INAPTO]" : "";

                    JLabel lblNome = new JLabel("  " + j.getNomeCompleto()
                            + "  #" + j.getNumeroCamisola()
                            + "  " + j.getPosicao() + textoEstado);
                    lblNome.setFont(FONTE_NORMAL);
                    lblNome.setForeground(isInapto ? COR_PLACEHOLDER : COR_TEXTO);
                    lblNome.setBorder(new EmptyBorder(4, 4, 4, 4));
                    lblNome.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    // Clicar no nome continua a abrir a edição normal
                    lblNome.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override public void mouseClicked(java.awt.event.MouseEvent e) { abrirFormularioJogador(j); }
                        @Override public void mouseEntered(java.awt.event.MouseEvent e) { lblNome.setForeground(new Color(0x1565C0)); }
                        @Override public void mouseExited(java.awt.event.MouseEvent e) { lblNome.setForeground(isInapto ? COR_PLACEHOLDER : COR_TEXTO); }
                    });
                    linhaJogador.add(lblNome, BorderLayout.CENTER);

                    // 3. Botão Rápido de Lesão (só aparece se estiver Apto)
                    if (!isInapto) {
                        JButton btnLesao = new JButton("Marcar Lesão");
                        btnLesao.setFont(new Font("SansSerif", Font.PLAIN, 10)); // Botão pequenino
                        btnLesao.setFocusPainted(false);
                        btnLesao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        btnLesao.addActionListener(e -> {
                            ctrlJogador.marcarComoInapto(j);
                            atualizarListaJogadores(); // Recarrega para mostrar o link do substituto
                            if (aoAtualizar != null) aoAtualizar.run();
                        });
                        linhaJogador.add(btnLesao, BorderLayout.EAST);
                    }

                    containerJogador.add(linhaJogador);

                    // 4. Lógica UC05: O LINK DE SUBSTITUTO
                    if (isInapto) {
                        JLabel linkSubstituto = new JLabel("      ↳ Adicionar Substituto...");
                        linkSubstituto.setFont(FONTE_NORMAL);
                        linkSubstituto.setForeground(COR_ERRO); // Usa a tua cor vermelha definida no topo
                        linkSubstituto.setBorder(new EmptyBorder(0, 4, 6, 4));
                        linkSubstituto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                        linkSubstituto.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                                abrirFormularioJogador(null); // Abre vazio para criar o substituto
                            }
                            @Override public void mouseEntered(java.awt.event.MouseEvent e) { linkSubstituto.setForeground(new Color(0xB71C1C)); }
                            @Override public void mouseExited(java.awt.event.MouseEvent e) { linkSubstituto.setForeground(COR_ERRO); }
                        });
                        containerJogador.add(linkSubstituto);
                    }

                    painelListaJogadores.add(containerJogador);

                    // Separador final
                    JSeparator sep = new JSeparator();
                    sep.setForeground(COR_BORDA);
                    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    painelListaJogadores.add(sep);
                }
            }
            painelListaJogadores.revalidate();
            painelListaJogadores.repaint();
        }

        /** Abre o FormularioJogador para inserir ou editar (UC03). */
        private void abrirFormularioJogador(Jogador jogadorParaEditar) {
            FormularioJogador fj = new FormularioJogador(
                    this, ctrlJogador, equipaParaEditar, jogadorParaEditar,
                    () -> {
                        atualizarListaJogadores();
                        if (aoAtualizar != null) aoAtualizar.run();
                    });
            fj.setVisible(true);
        }

        // ── Pré-preenchimento (UC02 — Editar) ─────────────────────────────────────

        private void preencherFormulario() {
            // Nome
            campNome.setText(equipaParaEditar.getNome());
            campNome.setForeground(COR_TEXTO);

            // País — seleciona a opção correcta no combo
            String paisAtual = equipaParaEditar.getPais();
            for (int i = 0; i < comboPais.getItemCount(); i++) {
                if (comboPais.getItemAt(i).equalsIgnoreCase(paisAtual)) {
                    comboPais.setSelectedIndex(i);
                    break;
                }
            }

            // Grupo (apenas visual)
            String grupo = equipaParaEditar.getGrupo();
            if (!grupo.isBlank()) {
                campGrupo.setText("Grupo " + grupo);
            }

            // Lista jogadores
            atualizarListaJogadores();
        }

        // ── Botão Concluído ───────────────────────────────────────────────────────

        private JButton criarBotaoConcluido() {
            JButton btn = new JButton("Concluído") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover()
                            ? new Color(0xF0F0F0) : COR_BRANCO);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(FONTE_NORMAL);
            btn.setForeground(COR_TEXTO);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COR_BORDA, 1, true),
                    new EmptyBorder(8, 16, 8, 16)));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            btn.setAlignmentX(LEFT_ALIGNMENT);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(this::aoConcluir);
            return btn;
        }

        private void aoConcluir(ActionEvent e) {
            limparErros();

            String nome = textoReal(campNome, "Nome...");
            String pais = comboPais.getSelectedIndex() == 0
                    ? ""
                    : (String) comboPais.getSelectedItem();

            try {
                if (equipaParaEditar == null) {
                    // UC01 — Inserir
                    ctrlEquipa.adicionarEquipa(nome, pais);
                } else {
                    // UC02 — Editar
                    ctrlEquipa.editarEquipa(equipaParaEditar, nome, pais);
                }
                if (aoAtualizar != null) aoAtualizar.run();
                dispose();

            } catch (IllegalArgumentException ex) {
                tratarErro(ex.getMessage());
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                        "Não é possível editar equipas: os grupos já foram gerados.",
                        "Operação bloqueada", JOptionPane.WARNING_MESSAGE);
            }
        }

        // ── Gestão de erros visuais ────────────────────────────────────────────────

        private void tratarErro(String codigo) {
            switch (codigo) {
                case "CAMPO_NOME_VAZIO" -> {
                    destacarErro(campNome); lblErroNome.setText("O nome é obrigatório."); }
                case "CAMPO_PAIS_VAZIO" -> {
                    destacarComboErro(comboPais); lblErroPais.setText("Selecione um país."); }
                case "NOME_DUPLICADO"   -> {
                    destacarErro(campNome); lblErroNome.setText("Já existe uma equipa com este nome."); }
                case "PAIS_INVALIDO"    -> {
                    destacarComboErro(comboPais); lblErroPais.setText("País inválido."); }
                default -> JOptionPane.showMessageDialog(this, codigo, "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void destacarErro(JTextField tf) {
            tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COR_ERRO, 2, true),
                    new EmptyBorder(6, 10, 6, 10)));
        }

        private void destacarComboErro(JComboBox<?> cb) {
            cb.setBorder(new LineBorder(COR_ERRO, 2, true));
        }

        private void limparErros() {
            campNome.setBorder(camposBorda(COR_BORDA, 1));
            comboPais.setBorder(null);
            lblErroNome.setText(" ");
            lblErroPais.setText(" ");
        }

        // ── Utilitários de construção ──────────────────────────────────────────────

        private JTextField criarTextField(String placeholder) {
            JTextField tf = new JTextField();
            tf.setFont(FONTE_NORMAL);
            tf.setForeground(COR_PLACEHOLDER);
            tf.setText(placeholder);
            tf.setBorder(camposBorda(COR_BORDA, 1));
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

            tf.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) {
                    if (tf.getText().equals(placeholder)) {
                        tf.setText(""); tf.setForeground(COR_TEXTO);
                    }
                    tf.setBorder(camposBorda(COR_FOCO, 1));
                }
                @Override public void focusLost(java.awt.event.FocusEvent e) {
                    if (tf.getText().isBlank()) {
                        tf.setText(placeholder); tf.setForeground(COR_PLACEHOLDER);
                    }
                    tf.setBorder(camposBorda(COR_BORDA, 1));
                }
            });
            return tf;
        }

        private void estilizarCombo(JComboBox<String> cb) {
            cb.setFont(FONTE_NORMAL);
            cb.setBackground(COR_BRANCO);
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            cb.setAlignmentX(LEFT_ALIGNMENT);
        }

        private JLabel labelErro() {
            JLabel lbl = new JLabel(" ");
            lbl.setFont(FONTE_ERRO);
            lbl.setForeground(COR_ERRO);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            return lbl;
        }

        private static javax.swing.border.Border camposBorda(Color cor, int espessura) {
            return BorderFactory.createCompoundBorder(
                    new LineBorder(cor, espessura, true),
                    new EmptyBorder(6, 10, 6, 10));
        }

        private static Box.Filler rigidH(int altura) {
            return (Box.Filler) Box.createRigidArea(new Dimension(0, altura));
        }

        private String textoReal(JTextField tf, String placeholder) {
            String t = tf.getText().trim();
            return t.equals(placeholder) ? "" : t;
        }
    }