package academia.model;

import java.math.BigDecimal;

/**
 * Representa um plano de academia (mensal, trimestral, anual etc.).
 */
public class Plano {

    private String codigo;        // chave primária, definida pelo usuário (única)
    private String nome;          // nome do plano
    private BigDecimal valor;     // valor cobrado
    private int duracaoDias;      // duração em dias
    private String beneficios;    // texto curto descrevendo benefícios

    public Plano() {
    }

    public Plano(String codigo, String nome, BigDecimal valor, int duracaoDias, String beneficios) {
        this.codigo = codigo;
        this.nome = nome;
        this.valor = valor;
        this.duracaoDias = duracaoDias;
        this.beneficios = beneficios;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public int getDuracaoDias() {
        return duracaoDias;
    }

    public void setDuracaoDias(int duracaoDias) {
        this.duracaoDias = duracaoDias;
    }

    public String getBeneficios() {
        return beneficios;
    }

    public void setBeneficios(String beneficios) {
        this.beneficios = beneficios;
    }

    @Override
    public String toString() {
        // Usado nos JComboBox para exibir o plano de forma legível
        return codigo + " - " + nome + " (R$ " + valor + ")";
    }
}
