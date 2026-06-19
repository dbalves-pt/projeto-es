package pt.ipleiria.estg.dei.ei.esoft.modelo;

public class ValidadorNIF {
    public static boolean validar(String nif) {
        if (nif == null || !nif.matches("\\d{9}")) return false;
        int[] pesos = {9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 8; i++) {
            soma += (nif.charAt(i) - '0') * pesos[i];
        }
        int digitoVerificador = 11 - (soma % 11);
        if (digitoVerificador >= 10) digitoVerificador = 0;
        return digitoVerificador == (nif.charAt(8) - '0');
    }
}