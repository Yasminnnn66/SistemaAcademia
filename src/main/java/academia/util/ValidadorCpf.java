package academia.util;

/**
 * Utilitário para validação e formatação de CPF.
 * Implementa o cálculo padrão dos dígitos verificadores.
 */
public class ValidadorCpf {

    private ValidadorCpf() {
        // classe utilitária, não deve ser instanciada
    }

    /**
     * Remove tudo que não for dígito (pontos, traço, espaços).
     */
    public static String somenteNumeros(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("[^0-9]", "");
    }

    /**
     * Valida um CPF verificando tamanho, repetição de dígitos e
     * os dois dígitos verificadores.
     */
    public static boolean isValido(String cpf) {
        String numeros = somenteNumeros(cpf);

        if (numeros.length() != 11) {
            return false;
        }

        // CPFs com todos os dígitos iguais (ex: 00000000000) são inválidos
        if (numeros.chars().distinct().count() == 1) {
            return false;
        }

        int[] digitos = new int[11];
        for (int i = 0; i < 11; i++) {
            digitos[i] = numeros.charAt(i) - '0';
        }

        // Cálculo do primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += digitos[i] * (10 - i);
        }
        int resto = soma % 11;
        int dv1 = (resto < 2) ? 0 : (11 - resto);
        if (dv1 != digitos[9]) {
            return false;
        }

        // Cálculo do segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += digitos[i] * (11 - i);
        }
        resto = soma % 11;
        int dv2 = (resto < 2) ? 0 : (11 - resto);
        return dv2 == digitos[10];
    }

    /**
     * Formata um CPF de 11 dígitos no padrão 000.000.000-00.
     * Se a entrada não tiver 11 dígitos, retorna o valor original sem alterações.
     */
    public static String formatar(String cpf) {
        String numeros = somenteNumeros(cpf);
        if (numeros.length() != 11) {
            return cpf;
        }
        return numeros.substring(0, 3) + "." + numeros.substring(3, 6) + "."
                + numeros.substring(6, 9) + "-" + numeros.substring(9, 11);
    }
}
