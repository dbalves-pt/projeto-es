package pt.ipleiria.estg.dei.ei.esoft.controlador;


import pt.ipleiria.estg.dei.ei.esoft.modelo.Equipa;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador.Estado;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Jogador.Posicao;
import pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador UC03 — Adicionar Jogador a Equipa.
 *
 * Caminho Principal:
 *   1. Utilizador selecciona uma equipa no painel esquerdo.
 *   2. Utilizador clica em 'Inserir jogador…' no painel central.
 *   3. Sistema abre o formulário 'Inserir/Editar Jogador'.
 *   4. Utilizador preenche os dados e clica em 'Concluído'.
 *   5. Sistema regista o jogador e actualiza a lista no painel central.
 *
 * CA 5.1 — Campo vazio: sistema assinala o erro nos campos em falta.
 */
public class JogadorControlador {

    private final Torneio torneio;

    public JogadorControlador()           { this.torneio = Torneio.getInstancia(); }
    public JogadorControlador(Torneio t)  { this.torneio = t; }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC03 — Adicionar Jogador
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Cria e regista um jogador na equipa seleccionada.
     *
     * @param equipa            Equipa actualmente seleccionada no painel esquerdo.
     * @param nomeCompleto      Campo "Nome Completo…"
     * @param descricaoPosicao  Valor seleccionado no dropdown "Posição"
     * @param dataTexto         Campo "Data de Nascimento…" (formato dd/MM/yyyy)
     * @param numeroCamisolaStr Campo "Número de Camisola…"
     * @param descricaoEstado   Valor seleccionado no dropdown "Estado"
     *
     * @throws IllegalArgumentException com código de erro em caso de validação falhada.
     */


    // ══════════════════════════════════════════════════════════════════════════
    //  UC03 — Adicionar Jogador
    // ══════════════════════════════════════════════════════════════════════════

    // ══════════════════════════════════════════════════════════════════════════
    //  UC03 — Adicionar Jogador
    // ══════════════════════════════════════════════════════════════════════════

    public void adicionarJogador(Equipa equipa,
                                 String nomeCompleto,
                                 String descricaoPosicao,
                                 String dataTexto,
                                 String numeroCamisolaStr,
                                 String descricaoEstado) {

        // ── VERIFICAÇÃO NOVA E ÚNICA: Bloqueia se a bola já rolou no 1º jogo ──
        if (isMercadoFechado()) {
            throw new IllegalStateException("TORNEIO_EM_CURSO_INSERCAO_BLOQUEADA");
        }

        // CA 5.1 — Campos obrigatórios vazios
        if (nomeCompleto == null || nomeCompleto.isBlank())
            throw new IllegalArgumentException("CAMPO_NOME_VAZIO");
        if (descricaoPosicao == null || descricaoPosicao.isBlank())
            throw new IllegalArgumentException("CAMPO_POSICAO_VAZIO");
        if (dataTexto == null || dataTexto.isBlank())
            throw new IllegalArgumentException("CAMPO_DATA_VAZIO");
        if (numeroCamisolaStr == null || numeroCamisolaStr.isBlank())
            throw new IllegalArgumentException("CAMPO_NUMERO_VAZIO");
        if (descricaoEstado == null || descricaoEstado.isBlank())
            throw new IllegalArgumentException("CAMPO_ESTADO_VAZIO");
        if (equipa == null)
            throw new IllegalArgumentException("EQUIPA_NULA");

        // Parsing e validações de tipo
        Posicao   posicao = parsarPosicao(descricaoPosicao);
        LocalDate data    = parsarData(dataTexto);
        int       numero  = parsarNumeroCamisola(numeroCamisolaStr);
        Estado    estado  = parsarEstado(descricaoEstado);

        // Número de camisola único dentro da equipa
        if (equipa.existeNumeroCamisola(numero))
            throw new IllegalArgumentException("NUMERO_CAMISOLA_DUPLICADO");

        // VALIDAÇÃO DO LIMITE DE 23 JOGADORES APTOS
        if (estado == Estado.APTO) {
            validarLimiteAptos(equipa);
        }

        Jogador novoJogador = new Jogador(nomeCompleto, equipa, posicao, data, numero, estado);
        equipa.adicionarJogador(novoJogador);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC03 — Editar Jogador (formulário reutilizado)
    // ══════════════════════════════════════════════════════════════════════════

    public void editarJogador(Jogador jogador,
                              String nomeCompleto,
                              String descricaoPosicao,
                              String dataTexto,
                              String numeroCamisolaStr,
                              String descricaoEstado) {

        if (nomeCompleto == null || nomeCompleto.isBlank())
            throw new IllegalArgumentException("CAMPO_NOME_VAZIO");
        if (descricaoPosicao == null || descricaoPosicao.isBlank())
            throw new IllegalArgumentException("CAMPO_POSICAO_VAZIO");
        if (dataTexto == null || dataTexto.isBlank())
            throw new IllegalArgumentException("CAMPO_DATA_VAZIO");
        if (numeroCamisolaStr == null || numeroCamisolaStr.isBlank())
            throw new IllegalArgumentException("CAMPO_NUMERO_VAZIO");
        if (descricaoEstado == null || descricaoEstado.isBlank())
            throw new IllegalArgumentException("CAMPO_ESTADO_VAZIO");

        // 1º FAZ O PARSING DAS VARIÁVEIS PRIMEIRO!
        Posicao   posicao = parsarPosicao(descricaoPosicao);
        LocalDate data    = parsarData(dataTexto);
        int       numero  = parsarNumeroCamisola(numeroCamisolaStr);
        Estado    estado  = parsarEstado(descricaoEstado);

        // 2º ── VERIFICAÇÃO NOVA E ÚNICA: SE A BOLA JÁ ROLOU, SÓ MUDA ESTADO MÉDICO ──
        if (isMercadoFechado()) {
            if (!jogador.getNomeCompleto().trim().equalsIgnoreCase(nomeCompleto.trim()) ||
                    jogador.getPosicao() != posicao ||
                    !jogador.getDataNascimento().equals(data) ||
                    jogador.getNumeroCamisola() != numero) {
                throw new IllegalStateException("APENAS_ALTERACAO_DE_ESTADO_PERMITIDA");
            }
        }

        // USA O MÉTODO AUXILIAR PARA LIMPAR O CÓDIGO
        if (estado == Estado.APTO && jogador.getEstado() != Estado.APTO) {
            validarLimiteAptos(jogador.getEquipa());
        }

        // Verificar duplicado excluindo o próprio jogador
        if (jogador.getEquipa().existeNumeroCamisolaExcluindo(numero, jogador))
            throw new IllegalArgumentException("NUMERO_CAMISOLA_DUPLICADO");

        jogador.setNomeCompleto(nomeCompleto);
        jogador.setPosicao(posicao);
        jogador.setDataNascimento(data);
        jogador.setNumeroCamisola(numero);
        jogador.setEstado(estado);
    }
    // ══════════════════════════════════════════════════════════════════════════
    //  Auxiliares para a Vista
    // ══════════════════════════════════════════════════════════════════════════

    public List<String> getDescricoesPosicao() {
        return Arrays.stream(Posicao.values())
                .map(Posicao::toString)
                .collect(Collectors.toList());
    }

    public List<String> getDescricoesEstado() {
        return Arrays.stream(Estado.values())
                .map(Estado::toString)
                .collect(Collectors.toList());
    }

    // ── Parsing privado ────────────────────────────────────────────────────────

    private Posicao parsarPosicao(String descricao) {
        try {
            return Posicao.fromDescricao(descricao);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("POSICAO_INVALIDA");
        }
    }

    private LocalDate parsarData(String texto) {
        try {
            LocalDate data = Jogador.parsarData(texto);
            LocalDate hoje = LocalDate.now();

            // Calcula a idade aproximada do jogador
            int idade = hoje.getYear() - data.getYear();

            // ── VALIDAÇÕES REALISTAS DE IDADE PARA FUTEBOL ──
            if (idade < 15 || idade > 50) {
                throw new IllegalArgumentException("IDADE_INVALIDA");
            }

            return data;

        } catch (IllegalArgumentException e) {
            // Se o erro foi a idade que definimos acima, passa o erro para a frente
            if (e.getMessage().equals("IDADE_INVALIDA")) {
                throw e;
            }
            // Qualquer outro erro de formato
            throw new IllegalArgumentException("DATA_INVALIDA");
        } catch (Exception e) {
            throw new IllegalArgumentException("DATA_INVALIDA");
        }
    }


    // ── Método auxiliar para verificar limite de jogadores APTOS ──
    private void validarLimiteAptos(Equipa equipa) {
        long numeroAptos = equipa.getJogadores().stream()
                .filter(j -> j.getEstado() == Estado.APTO)
                .count();
        if (numeroAptos >= 23) {
            throw new IllegalArgumentException("LIMITE_JOGADORES_APTO_EXCEDIDO");
        }
    }

    // ── MÉTODO INTELIGENTE: Verifica se o primeiro jogo já começou ──
    public boolean isMercadoFechado() {
        if (pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.getInstancia().getEstado() != pt.ipleiria.estg.dei.ei.esoft.modelo.Torneio.Estado.EM_CURSO) {
            return false; // Se o calendário nem foi gerado, o mercado está abertíssimo
        }

        // Se já foi gerado, vai ver se algum jogo já saiu do estado "Calendarizado"
        pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador jc = new pt.ipleiria.estg.dei.ei.esoft.controlador.JogoControlador();
        return jc.getJogos().stream().anyMatch(j ->
                j.getEstado() != pt.ipleiria.estg.dei.ei.esoft.modelo.Jogo.Estado.CALENDARIZADO
        );
    }

    private int parsarNumeroCamisola(String texto) {
        try {
            int n = Integer.parseInt(texto.trim());
            if (n < 1 || n > 99) throw new IllegalArgumentException();
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NUMERO_INVALIDO");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("NUMERO_FORA_INTERVALO");
        }
    }

    private Estado parsarEstado(String descricao) {
        try {
            return Estado.fromDescricao(descricao);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ESTADO_INVALIDO");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UC05 — Marcar Jogador como Inapto (Lesão)
    // ══════════════════════════════════════════════════════════════════════════

    public void marcarComoInapto(Jogador jogador) {
        if (jogador == null) return;
        // Assume que "Inapto" ou "INAPTO" é o texto que tens no teu Enum
        // Se o teu enum for diferente (ex: "Lesionado"), muda o texto aqui em baixo
        jogador.setEstado(parsarEstado("Inapto"));
    }

    public void eliminarJogador(Equipa equipa, Jogador jogador) {
        if (equipa != null && jogador != null) {
            // Se a tua lista for privada, garante que o método remove() existe na classe Equipa
            // Caso contrário, tenta: equipa.getJogadores().remove(jogador);
            equipa.removerJogador(jogador); // Usa o método da tua classe Equipa
        }
    }
}