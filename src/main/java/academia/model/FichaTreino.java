package academia.model;

import java.time.LocalDateTime;

/**
 * Representa uma ficha de treino (A, B, C ...) vinculada a um aluno.
 */
public class FichaTreino {

    private int id;
    private String cpfAluno;
    private String letraFicha;       // A, B, C, D...
    private String exercicios;       // texto livre (um exercício por linha, por convenção)
    private String metas;            // texto livre com metas/observações
    private LocalDateTime ultimaAtualizacao;

    // auxiliar para exibição
    private String nomeAluno;

    public FichaTreino() {
    }

    public FichaTreino(int id, String cpfAluno, String letraFicha, String exercicios,
                        String metas, LocalDateTime ultimaAtualizacao) {
        this.id = id;
        this.cpfAluno = cpfAluno;
        this.letraFicha = letraFicha;
        this.exercicios = exercicios;
        this.metas = metas;
        this.ultimaAtualizacao = ultimaAtualizacao;
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

    public String getLetraFicha() {
        return letraFicha;
    }

    public void setLetraFicha(String letraFicha) {
        this.letraFicha = letraFicha;
    }

    public String getExercicios() {
        return exercicios;
    }

    public void setExercicios(String exercicios) {
        this.exercicios = exercicios;
    }

    public String getMetas() {
        return metas;
    }

    public void setMetas(String metas) {
        this.metas = metas;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }
}
