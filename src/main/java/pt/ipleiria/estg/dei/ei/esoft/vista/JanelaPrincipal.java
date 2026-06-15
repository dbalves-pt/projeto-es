package pt.ipleiria.estg.dei.ei.esoft.vista;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;

import javax.swing.*;
import java.awt.*;

public class JanelaPrincipal extends JFrame {

    private final EquipaControlador equipaControlador;
    private DefaultListModel<Equipa> listModel;
    private JList<Equipa> listaEquipas;

    public JanelaPrincipal(EquipaControlador equipaControlador) {
        this.equipaControlador = equipaControlador;
        construirUI();
    }

    private void construirUI() {
        setTitle("Gestão do Campeonato do Mundo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── Painel Esquerdo (Lista de Equipas) ──
        JPanel painelEsquerdo = new JPanel(new BorderLayout());
        painelEsquerdo.setPreferredSize(new Dimension(250, 0));
        painelEsquerdo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel tituloEquipas = new JLabel("Equipas");
        tituloEquipas.setFont(new Font("SansSerif", Font.BOLD, 16));
        painelEsquerdo.add(tituloEquipas, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        listaEquipas = new JList<>(listModel);
        painelEsquerdo.add(new JScrollPane(listaEquipas), BorderLayout.CENTER);

        // ── Link/Botão "Inserir equipa..." ──
        JButton btnInserir = new JButton("Inserir equipa...");
        btnInserir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bloqueia o botão se os grupos já estiverem gerados (UC07)
        btnInserir.setEnabled(!equipaControlador.isInsercaoDeEquipaBloqueada());

        btnInserir.addActionListener(e -> abrirFormularioInserir());
        painelEsquerdo.add(btnInserir, BorderLayout.SOUTH);

        add(painelEsquerdo, BorderLayout.WEST);

        // Painel Central (Ficará para os detalhes da equipa no UC02)
        JPanel painelCentral = new JPanel();
        painelCentral.setBackground(Color.WHITE);
        add(painelCentral, BorderLayout.CENTER);

        atualizarLista();
    }

    private void abrirFormularioInserir() {
        // Abre o pop-up que o Claude te deu.
        // Passamos 'this::atualizarLista' para a lista atualizar assim que o formulário fechar!
        FormularioInserirEquipa form = new FormularioInserirEquipa(this, equipaControlador, this::atualizarLista);
        form.setVisible(true);
    }

    private void atualizarLista() {
        listModel.clear();
        for (Equipa eq : equipaControlador.getEquipas()) {
            listModel.addElement(eq);
        }
    }
}