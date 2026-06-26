package academia.util;

/**
 * Lançada quando um CPF informado não passa pela validação
 * (formato incorreto ou dígitos verificadores inválidos).
 */
public class CpfInvalidoException extends Exception {

    public CpfInvalidoException(String mensagem) {
        super(mensagem);
    }
}
