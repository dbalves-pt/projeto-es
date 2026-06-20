package pt.ipleiria.estg.dei.ei.esoft.controlador;

public class FinanceiroControlador {
    private final BilheteControlador bilheteControlador;
    private final PatrocinioControlador patrocinioControlador;

    public FinanceiroControlador(BilheteControlador bilheteControlador, PatrocinioControlador patrocinioControlador) {
        this.bilheteControlador = bilheteControlador;
        this.patrocinioControlador = patrocinioControlador;
    }

    public double getReceitaTotal() {
        return bilheteControlador.getReceitaTotalBilheteira() + patrocinioControlador.getReceitaTotalPatrocinios();
    }

    public double getReceitaBilheteira() {
        return bilheteControlador.getReceitaTotalBilheteira();
    }

    public double getReceitaPatrocinios() {
        return patrocinioControlador.getReceitaTotalPatrocinios();
    }

    public double getReceitaBilheteiraJogosTerminados() {
        return bilheteControlador.getReceitaBilheteiraJogosTerminados();
    }
}