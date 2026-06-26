package academia.model;

import java.time.LocalDate;


public class Aluno {

    private String cpf;              // chave primária (somente números, 11 dígitos)
    private String nome;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String observacoes;       // restrições/observações (opcional)

    public Aluno() {
    }

    public Aluno(String cpf, String nome, LocalDate dataNascimento, String telefone,
                 String email, String observacoes) {
        this.cpf = cpf;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.email = email;
        this.observacoes = observacoes;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @Override
    public String toString() {

        return cpf + " - " + nome;
    }
}
