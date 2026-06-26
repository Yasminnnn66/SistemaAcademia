package academia.util;

/**
 * Lançada quando um valor numérico (valor do plano, duração em dias etc.)
 * é inválido — negativo, zero quando não permitido, ou não numérico.
 */
public class ValorInvalidoException extends Exception {

    public ValorInvalidoException(String mensagem) {
        super(mensagem);
    }
}
