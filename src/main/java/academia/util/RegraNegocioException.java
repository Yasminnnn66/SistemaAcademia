package academia.util;

/**
 * Lançada quando uma operação viola uma regra de negócio do sistema,
 * por exemplo: tentar check-in com plano vencido, código de plano duplicado,
 * aluno já matriculado em plano ativo, etc.
 */
public class RegraNegocioException extends Exception {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
