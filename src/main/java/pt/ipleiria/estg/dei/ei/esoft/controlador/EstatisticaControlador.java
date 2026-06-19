package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.ClassificacaoEquipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.util.List;
import java.util.Map;

public class EstatisticaControlador {
    private final Torneio torneio;

    public EstatisticaControlador() {
        this.torneio = Torneio.getInstancia();
    }

    public List<Jogador> getMelhoresMarcadores() {
        return torneio.getMelhoresMarcadores();
    }

    public List<Jogador> getMaisAssistencias() {
        return torneio.getMaisAssistencias();
    }

    public List<Jogador> getMaisDefesas() {
        return torneio.getMaisDefesas();
    }

    public Map<String, List<ClassificacaoEquipa>> getClassificacaoTodosGrupos() {
        return torneio.getClassificacaoTodosGrupos();
    }
}