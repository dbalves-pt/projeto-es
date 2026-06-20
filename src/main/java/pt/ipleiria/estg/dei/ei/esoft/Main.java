package pt.ipleiria.estg.dei.ei.esoft;

import pt.ipleiria.estg.dei.ei.esoft.controlador.EquipaControlador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.*;
import pt.ipleiria.estg.dei.ei.esoft.vista.JanelaPrincipal;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador; // Importa o Jogador
import java.time.LocalDate;                           // Importa a data
import javax.swing.*;

/**
 * Classe de entrada principal (Motor de arranque) da aplicação.
 * Configura o Look and Feel do sistema, inicializa os controladores centrais
 * e inicia a interface gráfica principal.
 */
public class Main {
    public static void main(String[] args) {

        // 1. Configurar o estilo visual moderno (Look and Feel) do Sistema Operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // 2. Injetar dados de teste para não termos de preencher tudo à mão sempre que abrimos o programa!
        carregarDadosDeTeste();

        // 3. Inicializar APENAS o controlador de equipas (pois a Janela cria o outro)
        EquipaControlador equipaControlador = new EquipaControlador();

        // 4. Executar e apresentar a janela principal
        SwingUtilities.invokeLater(() -> {
            // ATENÇÃO: letra minúscula (variável) e apenas 1 parâmetro!
            JanelaPrincipal janela = new JanelaPrincipal(equipaControlador);
            janela.setVisible(true);
        });
    }

    /**
     * Método provisório para desenvolvimento.
     * Injeta dados diretamente no Singleton para facilitar os testes da interface gráfica.
     */

    private static void carregarDadosDeTeste() {
        Torneio torneio = Torneio.getInstancia();

        // 1. Criar Estádio
        Estadio e1 = new Estadio("Estádio da Luz", "Lisboa", "Portugal", 65000);
        torneio.adicionarEstadio(e1);

        // Nomes das equipas para o ciclo
        String[] nomesEquipas = {"Portugal", "França", "Espanha", "Brasil"};

        // 2. Criar cada equipa com 23 jogadores
        for (String nome : nomesEquipas) {
            Equipa equipa = new Equipa(nome, nome);

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
    }
}