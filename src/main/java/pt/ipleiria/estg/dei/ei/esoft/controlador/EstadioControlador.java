package pt.ipleiria.estg.dei.ei.esoft.controlador;

import pt.ipleiria.estg.dei.ei.esoft.modelo.Bancada;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Estadio;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.util.List;

public class EstadioControlador {
    private final Torneio torneio;

    public EstadioControlador() {
        this.torneio = Torneio.getInstancia();
    }

    public EstadioControlador(Torneio torneio) {
        this.torneio = torneio;
    }

    public List<Estadio> getEstadios() {
        return torneio.getEstadios();
    }

    // ── Fluxo: Adicionar Estádio ──────────────────────────────────────────────
    public void adicionarEstadio(String nome, String cidade, String pais, String lotacaoStr) {
        if (nome.isBlank() || cidade.isBlank() || pais.isBlank() || lotacaoStr.isBlank()) {
            throw new IllegalArgumentException("CAMPOS_VAZIOS");
        }

        // Validação 6.1: Nome Duplicado
        boolean existe = torneio.getEstadios().stream()
                .anyMatch(e -> e.getNome().equalsIgnoreCase(nome));
        if (existe) throw new IllegalArgumentException("NOME_DUPLICADO");

        int lotacao;
        try {
            lotacao = Integer.parseInt(lotacaoStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("LOTACAO_INVALIDA");
        }
        if (lotacao <= 0) {
            throw new IllegalArgumentException("LOTACAO_INVALIDA");
        }

        Estadio novoEstadio = new Estadio(nome, cidade, pais, lotacao);
        torneio.adicionarEstadio(novoEstadio);
    }

    public void eliminarEstadio(Estadio estadio) {
        if (estadio != null) torneio.getEstadios().remove(estadio);
    }

    // ── Fluxo: Adicionar Bancada ──────────────────────────────────────────────

    /**
     * Adiciona uma bancada a um estádio.
     *
     * Códigos de erro lançados (IllegalArgumentException.getMessage()):
     *   CAMPOS_VAZIOS              – algum campo obrigatório está vazio
     *   NOME_BANCADA_DUPLICADO     – já existe bancada com o mesmo nome neste estádio
     *   VALOR_PRECO_INVALIDO       – preço não numérico ou <= 0
     *   VALOR_FILAS_INVALIDO       – filas não numérico ou <= 0
     *   VALOR_LUGARES_INVALIDO     – lugares não numérico ou <= 0
     *   LOTACAO_EXCEDIDA           – lugares pedidos excedem a lotação máxima do estádio
     */
    public void adicionarBancada(Estadio estadio, String nome, String precoStr,
                                 String categoria, String filasStr, String lugaresStr) {
        if (estadio == null) throw new IllegalArgumentException("ESTADIO_NULO");
        if (nome.isBlank() || precoStr.isBlank() || categoria.isBlank()
                || filasStr.isBlank() || lugaresStr.isBlank()) {
            throw new IllegalArgumentException("CAMPOS_VAZIOS");
        }

        // ── Nome de bancada duplicado dentro do MESMO estádio ──────────────────
        boolean nomeDuplicado = estadio.getBancadas().stream()
                .anyMatch(b -> b.getNome().equalsIgnoreCase(nome.trim()));
        if (nomeDuplicado) {
            throw new IllegalArgumentException("NOME_BANCADA_DUPLICADO");
        }

        // ── Validações numéricas separadas — cada uma com o seu próprio código ─
        double preco;
        try {
            preco = Double.parseDouble(precoStr.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VALOR_PRECO_INVALIDO");
        }
        if (preco <= 0) throw new IllegalArgumentException("VALOR_PRECO_INVALIDO");

        int filas;
        try {
            filas = Integer.parseInt(filasStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VALOR_FILAS_INVALIDO");
        }
        if (filas <= 0) throw new IllegalArgumentException("VALOR_FILAS_INVALIDO");

        int lugares;
        try {
            lugares = Integer.parseInt(lugaresStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VALOR_LUGARES_INVALIDO");
        }
        if (lugares <= 0) throw new IllegalArgumentException("VALOR_LUGARES_INVALIDO");

        // ── Validação 4.1: Lotação Excedida ────────────────────────────────────
        if (estadio.getLotacaoAtual() + lugares > estadio.getLotacaoMaxima()) {
            throw new IllegalArgumentException("LOTACAO_EXCEDIDA");
        }

        Bancada novaBancada = new Bancada(nome, preco, categoria, filas, lugares);
        estadio.adicionarBancada(novaBancada);
    }

    public void editarBancada(Estadio estadio, Bancada bancadaEdit, String nome, String precoStr, String categoria, String filasStr, String lugaresStr) {
        if (nome.isBlank() || precoStr.isBlank() || categoria.isBlank() || filasStr.isBlank() || lugaresStr.isBlank()) {
            throw new IllegalArgumentException("CAMPOS_VAZIOS");
        }

        // Validação 6.1: Nome Duplicado (ignora se o nome for igual ao da própria bancada que estamos a editar)
        boolean nomeExiste = estadio.getBancadas().stream()
                .anyMatch(b -> b != bancadaEdit && b.getNome().equalsIgnoreCase(nome));
        if (nomeExiste) throw new IllegalArgumentException("NOME_BANCADA_DUPLICADO");

        try {
            double preco = Double.parseDouble(precoStr.replace(",", "."));
            int filas = Integer.parseInt(filasStr.trim());
            int lugares = Integer.parseInt(lugaresStr.trim());

            if (preco < 0) throw new IllegalArgumentException("VALOR_PRECO_INVALIDO");
            if (filas <= 0) throw new IllegalArgumentException("VALOR_FILAS_INVALIDO");
            if (lugares <= 0) throw new IllegalArgumentException("VALOR_LUGARES_INVALIDO");

            // Validação 4.1: Lotação (Retira os lugares antigos da bancada atual e soma os novos)
            int lotacaoSemEstaBancada = estadio.getLotacaoAtual() - bancadaEdit.getLugares();
            if (lotacaoSemEstaBancada + lugares > estadio.getLotacaoMaxima()) {
                throw new IllegalArgumentException("LOTACAO_EXCEDIDA");
            }

            // Se passou em todas as validações, atualiza os dados da bancada!
            bancadaEdit.setNome(nome);
            bancadaEdit.setPreco(preco);
            bancadaEdit.setCategoria(categoria);
            bancadaEdit.setFilas(filas);
            bancadaEdit.setLugares(lugares);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VALORES_NUMERICOS_INVALIDOS");
        }
    }
}