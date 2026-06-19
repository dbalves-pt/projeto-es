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

        // 1. Criar Estádios e Bancadas
        Estadio e1 = new Estadio("Estádio da Luz", "Lisboa", "Portugal", 65000);
        e1.adicionarBancada(new Bancada("Bancada VIP", 150.0, "VIP", 10, 500));
        torneio.adicionarEstadio(e1);

        // 2. Criar Equipas e Jogadores
        // Criamos a equipa primeiro
        Equipa portugal = new Equipa("Portugal", "Portugal");

        // Criamos o jogador associando-o à equipa recém-criada
        Jogador j1 = new Jogador(
                "Cristiano Ronaldo",
                portugal,
                Jogador.Posicao.AVANCADO,
                LocalDate.of(1985, 2, 5),
                7,
                Jogador.Estado.APTO
        );

        // Adicionamos o jogador à lista da equipa (supondo que tens um método adicionarJogador na classe Equipa)
        portugal.adicionarJogador(j1);

        // Adicionamos a equipa ao torneio
        torneio.adicionarEquipa(portugal);

        // Adicionamos outras equipas simples
        Equipa franca = new Equipa("França", "França");
        Jogador j2 = new Jogador(
                "Cristiano Ronaldo",
                franca,
                Jogador.Posicao.AVANCADO,
                LocalDate.of(1985, 2, 5),
                7,
                Jogador.Estado.APTO
        );

        // Adicionamos o jogador à lista da equipa (supondo que tens um método adicionarJogador na classe Equipa)
        franca.adicionarJogador(j1);

        // Adicionamos a equipa ao torneio
        torneio.adicionarEquipa(franca);
        Equipa espanha = new Equipa("Espanha", "Espanha");
        Jogador j3 = new Jogador(
                "Cristiano Ronaldo",
                espanha,
                Jogador.Posicao.AVANCADO,
                LocalDate.of(1985, 2, 5),
                7,
                Jogador.Estado.APTO
        );

        // Adicionamos o jogador à lista da equipa (supondo que tens um método adicionarJogador na classe Equipa)
        espanha.adicionarJogador(j1);

        // Adicionamos a equipa ao torneio
        torneio.adicionarEquipa(espanha);
        Equipa brasil = new Equipa("Brasil", "Brasil");
        Jogador j4 = new Jogador(
                "Cristiano Ronaldo",
                brasil,
                Jogador.Posicao.AVANCADO,
                LocalDate.of(1985, 2, 5),
                7,
                Jogador.Estado.APTO
        );

        // Adicionamos o jogador à lista da equipa (supondo que tens um método adicionarJogador na classe Equipa)
        brasil.adicionarJogador(j1);

        // Adicionamos a equipa ao torneio
        torneio.adicionarEquipa(brasil);
    }


}