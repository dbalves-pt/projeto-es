package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Entidade Grupo — suporte para UC07 (Configurar Torneio), UC11 (apuramento
 * de equipas após os jogos do grupo terminarem) e UC18 (Consultar
 * Classificação da Fase de Grupos).
 *
 * Mantém a lista de equipas do grupo e calcula a respetiva classificação
 * com base nos jogos terminados que lhe pertencem.
 */
public class Grupo {

    /** Linha de classificação de uma equipa dentro do grupo. */
    public static class LinhaClassificacao {
        private final Equipa equipa;
        private int vitorias;
        private int empates;
        private int derrotas;
        private int golosMarcados;
        private int golosSofridos;

        LinhaClassificacao(Equipa equipa) { this.equipa = equipa; }

        public Equipa getEquipa()          { return equipa; }
        public int getVitorias()           { return vitorias; }
        public int getEmpates()            { return empates; }
        public int getDerrotas()           { return derrotas; }
        public int getGolosMarcados()      { return golosMarcados; }
        public int getGolosSofridos()      { return golosSofridos; }
        public int getPontos()             { return vitorias * 3 + empates; }
        public int getDiferencaGolos()     { return golosMarcados - golosSofridos; }
        public int getJogosDisputados()    { return vitorias + empates + derrotas; }

        public String getGolosFormatado()  { return golosMarcados + "-" + golosSofridos; }
    }

    private final String nome;
    private final List<Equipa> equipas;

    public Grupo(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("O nome do grupo é obrigatório.");
        this.nome = nome;
        this.equipas = new ArrayList<>();
    }

    public String getNome() { return nome; }

    public List<Equipa> getEquipas() { return equipas; }

    public void adicionarEquipa(Equipa equipa) {
        if (equipa != null && !equipas.contains(equipa)) equipas.add(equipa);
    }

    public boolean contemEquipa(Equipa equipa) {
        return equipas.contains(equipa);
    }

    /**
     * Calcula a classificação atual do grupo com base nos jogos terminados
     * que pertencem a este grupo.
     *
     * Critérios de ordenação (UC18):
     *   1. Pontos (desc.)
     *   2. Diferença de golos (desc.)
     *   3. Golos marcados (desc.)
     *   4. Ordem alfabética (critério final estável; o confronto direto
     *      fica para decisão manual do utilizador em caso de empate técnico)
     */
    public List<LinhaClassificacao> calcularClassificacao(List<Jogo> jogosDoTorneio) {
        List<LinhaClassificacao> linhas = new ArrayList<>();
        for (Equipa eq : equipas) linhas.add(new LinhaClassificacao(eq));

        for (Jogo jogo : jogosDoTorneio) {
            if (jogo.getGrupo() != this) continue;
            if (jogo.getEstado() != Jogo.Estado.TERMINADO) continue;

            LinhaClassificacao linhaCasa = encontrarLinha(linhas, jogo.getEquipaCasa());
            LinhaClassificacao linhaFora = encontrarLinha(linhas, jogo.getEquipaFora());
            if (linhaCasa == null || linhaFora == null) continue;

            int golosCasa = jogo.getGolosCasa();
            int golosFora = jogo.getGolosFora();

            linhaCasa.golosMarcados += golosCasa;
            linhaCasa.golosSofridos += golosFora;
            linhaFora.golosMarcados += golosFora;
            linhaFora.golosSofridos += golosCasa;

            if (golosCasa > golosFora) {
                linhaCasa.vitorias++;
                linhaFora.derrotas++;
            } else if (golosCasa < golosFora) {
                linhaFora.vitorias++;
                linhaCasa.derrotas++;
            } else {
                linhaCasa.empates++;
                linhaFora.empates++;
            }
        }

        linhas.sort(
                Comparator.comparingInt(LinhaClassificacao::getPontos).reversed()
                        .thenComparing(Comparator.comparingInt(LinhaClassificacao::getDiferencaGolos).reversed())
                        .thenComparing(Comparator.comparingInt(LinhaClassificacao::getGolosMarcados).reversed())
                        .thenComparing(l -> l.getEquipa().getNome())
        );
        return linhas;
    }

    /** Verifica se todos os jogos deste grupo já terminaram (UC11 — apuramento). */
    public boolean todosOsJogosTerminaram(List<Jogo> jogosDoTorneio) {
        boolean encontrouAlgum = false;
        for (Jogo jogo : jogosDoTorneio) {
            if (jogo.getGrupo() != this) continue;
            encontrouAlgum = true;
            if (jogo.getEstado() != Jogo.Estado.TERMINADO) return false;
        }
        return encontrouAlgum;
    }

    private LinhaClassificacao encontrarLinha(List<LinhaClassificacao> linhas, Equipa equipa) {
        for (LinhaClassificacao l : linhas) {
            if (l.getEquipa() == equipa) return l;
        }
        return null;
    }

    @Override
    public String toString() { return nome; }
}
