package academia.model;

import java.time.LocalDate;

/**
 * Representa o vínculo entre um aluno e um plano (matrícula).
 * Guarda data de início, vencimento e status atual.
 */
public class Matricula {

    public static final String STATUS_ATIVA = "ATIVA";
    public static final String STATUS_VENCIDA = "VENCIDA";
    public static final String STATUS_CANCELADA = "CANCELADA";

    private int id;
    private String cpfAluno;
    private String codigoPlano;
    private LocalDate dataInicio;
    private LocalDate dataVencimento;
    private String status;          // ATIVA, VENCIDA ou CANCELADA
    private String motivoCancelamento; // preenchido somente se CANCELADA
    private LocalDate dataCancelamento;

    // Campos auxiliares (não persistidos diretamente, usados para exibição em telas/relatórios)
    private String nomeAluno;
    private String nomePlano;
    private java.math.BigDecimal valorPlano;

    public Matricula() {
    }

    public Matricula(int id, String cpfAluno, String codigoPlano, LocalDate dataInicio,
                      LocalDate dataVencimento, String status) {
        this.id = id;
        this.cpfAluno = cpfAluno;
        this.codigoPlano = codigoPlano;
        this.dataInicio = dataInicio;
        this.dataVencimento = dataVencimento;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCpfAluno() {
        return cpfAluno;
    }

    public void setCpfAluno(String cpfAluno) {
        this.cpfAluno = cpfAluno;
    }

    public String getCodigoPlano() {
        return codigoPlano;
    }

    public void setCodigoPlano(String codigoPlano) {
        this.codigoPlano = codigoPlano;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public LocalDate getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDate dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public String getNomePlano() {
        return nomePlano;
    }

    public void setNomePlano(String nomePlano) {
        this.nomePlano = nomePlano;
    }

    public java.math.BigDecimal getValorPlano() {
        return valorPlano;
    }

    public void setValorPlano(java.math.BigDecimal valorPlano) {
        this.valorPlano = valorPlano;
    }

    /**
     * Verifica se a matrícula está vencida com base na data de hoje,
     * independentemente do status gravado no banco (regra de negócio em tempo real).
     */
    public boolean isVencidaHoje() {
        return dataVencimento != null && dataVencimento.isBefore(LocalDate.now());
    }
}
