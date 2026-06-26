package academia.util;

/**
 * Lançada quando um campo obrigatório não foi preenchido pelo usuário.
 */
public class CampoObrigatorioException extends Exception {

    public CampoObrigatorioException(String mensagem) {
        super(mensagem);
    }
}
