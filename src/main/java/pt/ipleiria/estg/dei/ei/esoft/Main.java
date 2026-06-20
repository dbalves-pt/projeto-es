package pt.ipleiria.estg.dei.ei.esoft;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.controlador.TorneioControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.*;
import pt.ipleiria.estg.dei.ei.esoft.vista.JanelaPrincipal;

import javax.swing.*;
import java.time.LocalDate;

/**
 * Classe de entrada principal (Motor de arranque) da aplicação.
 */
public class Main {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ── INJETA OS DADOS E GERA O TORNEIO NOS BASTIDORES ──
        carregarDadosEGerarTorneio();

        EquipaControlador equipaControlador = new EquipaControlador();

        SwingUtilities.invokeLater(() -> {
            JanelaPrincipal janela = new JanelaPrincipal(equipaControlador);
            janela.setVisible(true);
        });
    }

    private static void carregarDadosEGerarTorneio() {
        Torneio torneio = Torneio.getInstancia();

        // 1. Criar Estádio
        Estadio e1 = new Estadio("Estádio da Luz", "Lisboa", "Portugal", 65000);
        torneio.adicionarEstadio(e1);

        String[] nomesEquipas = {"Portugal", "França", "Espanha", "Brasil"};

        // 2. Criar cada equipa com 23 jogadores E TREINADOR OBRIGATÓRIO
        for (String nome : nomesEquipas) {
            Equipa equipa = new Equipa(nome, nome);
            equipa.setTreinador("Treinador de " + nome); // <--- Cumpre a nova regra!

            for (int i = 1; i <= 23; i++) {
                Jogador j = new Jogador(
                        nome + " Jogador " + i,
                        equipa,
                        Jogador.Posicao.AVANCADO,
                        LocalDate.of(2000, 1, 1),
                        i,
                        Jogador.Estado.APTO
                );
                equipa.adicionarJogador(j);
            }
            torneio.adicionarEquipa(equipa);
        }

        // 3. CONFIGURAR O TORNEIO AUTOMATICAMENTE
        try {
            TorneioControlador tc = new TorneioControlador();

            LocalDate dataInicio = LocalDate.now().plusDays(1); // Começa amanhã (Passa a validação de datas antigas)
            LocalDate dataFim = dataInicio.plusDays(35);        // Duração > 30 dias (Passa a validação de tempo)

            // Isto gera os grupos, valida os jogadores/treinadores e avança o estado!
            tc.configurarTorneio(dataInicio, dataFim, 3);

            System.out.println("Torneio configurado automaticamente com sucesso para testes!");

        } catch (Exception ex) {
            System.err.println("Erro na automação: " + ex.getMessage());
        }
    }
}