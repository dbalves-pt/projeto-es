package pt.ipleiria.estg.dei.ei.esoft.modelo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Repositório estático País → Cidades.
 *
 * Usado pelo FormularioEstadio para popular o dropdown "Cidade" de forma
 * dependente do país seleccionado no dropdown "País".
 *
 * Nota de design: optámos por um mapa estático em memória (em vez de uma
 * API externa de geolocalização) para manter o projeto autónomo, sem
 * dependências de rede, conforme a natureza académica do trabalho.
 * A lista cobre os países habitualmente envolvidos no Campeonato do Mundo;
 * pode ser facilmente estendida adicionando novas entradas ao mapa.
 */
public final class PaisCidade {

    private static final Map<String, String[]> MAPA = new LinkedHashMap<>();

    static {
        MAPA.put("Portugal", new String[]{
                "Lisboa", "Porto", "Braga", "Coimbra", "Leiria", "Faro", "Aveiro"
        });
        MAPA.put("Espanha", new String[]{
                "Madrid", "Barcelona", "Sevilha", "Valência", "Bilbau"
        });
        MAPA.put("Alemanha", new String[]{
                "Berlim", "Munique", "Hamburgo", "Dortmund", "Frankfurt"
        });
        MAPA.put("França", new String[]{
                "Paris", "Marselha", "Lyon", "Lille", "Nice"
        });
        MAPA.put("Brasil", new String[]{
                "Rio de Janeiro", "São Paulo", "Brasília", "Salvador", "Belo Horizonte"
        });
        MAPA.put("Argentina", new String[]{
                "Buenos Aires", "Córdoba", "Rosário", "Mendoza"
        });
        MAPA.put("Inglaterra", new String[]{
                "Londres", "Manchester", "Liverpool", "Birmingham"
        });
        MAPA.put("Itália", new String[]{
                "Roma", "Milão", "Turim", "Nápoles"
        });
        MAPA.put("EUA", new String[]{
                "Nova Iorque", "Los Angeles", "Miami", "Chicago", "Dallas"
        });
        MAPA.put("México", new String[]{
                "Cidade do México", "Guadalajara", "Monterrey"
        });
        MAPA.put("Marrocos", new String[]{
                "Rabat", "Casablanca", "Marraquexe", "Tânger"
        });
        MAPA.put("Qatar", new String[]{
                "Doha", "Al Wakrah", "Al Rayyan"
        });
        MAPA.put("Holanda", new String[]{
                "Amesterdão", "Roterdão", "Eindhoven"
        });
        MAPA.put("Bélgica", new String[]{
                "Bruxelas", "Antuérpia", "Bruges"
        });
        MAPA.put("Japão", new String[]{
                "Tóquio", "Osaca", "Yokohama"
        });
    }

    private PaisCidade() { }

    /** Devolve a lista de países disponíveis (ordem de inserção). */
    public static String[] getPaises() {
        return MAPA.keySet().toArray(new String[0]);
    }

    /** Devolve as cidades associadas ao país, ou array vazio se o país não existir no mapa. */
    public static String[] getCidades(String pais) {
        return MAPA.getOrDefault(pais, new String[0]);
    }

    /** Indica se o país tem cidades pré-definidas no sistema. */
    public static boolean temCidades(String pais) {
        return MAPA.containsKey(pais);
    }
}