package academia.model;

import java.time.LocalDateTime;

/**
 * Representa o registro de check-in (presença) de um aluno.
 */
public class Presenca {

    private int id;
    private String cpfAluno;
    private LocalDateTime dataHora;

    // auxiliar para exibição em relatórios
    private String nomeAluno;

    public Presenca() {
    }

    public Presenca(int id, String cpfAluno, LocalDateTime dataHora) {
        this.id = id;
        this.cpfAluno = cpfAluno;
        this.dataHora = dataHora;
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }
}
