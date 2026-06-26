package academia.dao;

import academia.model.Aluno;
import academia.util.ConexaoBD;
import academia.util.RegraNegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object responsável pelas operações de CRUD da tabela "aluno".
 */
public class AlunoDAO {


     // Insere um novo aluno. Lança RegraNegocioException se o CPF já estiver cadastrado.
    public void inserir(Aluno aluno) throws SQLException, RegraNegocioException {
        if (buscarPorCpf(aluno.getCpf()) != null) {
            throw new RegraNegocioException("Já existe um aluno cadastrado com o CPF " + aluno.getCpf() + ".");
        }

        String sql = "INSERT INTO aluno (cpf, nome, data_nascimento, telefone, email, observacoes) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, aluno.getCpf());
            ps.setString(2, aluno.getNome());
            ps.setString(3, aluno.getDataNascimento().toString());
            ps.setString(4, aluno.getTelefone());
            ps.setString(5, aluno.getEmail());
            ps.setString(6, aluno.getObservacoes());
            ps.executeUpdate();
        }
    }

    /**
     * Atualiza os dados de um aluno já existente (o CPF não pode ser alterado).
     */
    public void atualizar(Aluno aluno) throws SQLException {
        String sql = "UPDATE aluno SET nome = ?, data_nascimento = ?, telefone = ?, "
                + "email = ?, observacoes = ? WHERE cpf = ?";

        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, aluno.getNome());
            ps.setString(2, aluno.getDataNascimento().toString());
            ps.setString(3, aluno.getTelefone());
            ps.setString(4, aluno.getEmail());
            ps.setString(5, aluno.getObservacoes());
            ps.setString(6, aluno.getCpf());
            ps.executeUpdate();
        }
    }

    /**
     * Remove um aluno pelo CPF.
     * Atenção: o banco não permite excluir aluno com matrículas/fichas/presenças
     * vinculadas (chave estrangeira) — esse cuidado é tratado na camada de UI.
     */
    public void excluir(String cpf) throws SQLException {
        String sql = "DELETE FROM aluno WHERE cpf = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.executeUpdate();
        }
    }

    /**
     * Busca um aluno pelo CPF. Retorna null se não encontrado.
     */
    public Aluno buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM aluno WHERE cpf = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todos os alunos cadastrados, ordenados por nome.
     */
    public List<Aluno> listarTodos() throws SQLException {
        List<Aluno> lista = new ArrayList<>();
        String sql = "SELECT * FROM aluno ORDER BY nome";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Busca alunos cujo nome ou CPF contenham o texto informado (para a busca rápida nas telas).
     */
    public List<Aluno> buscarPorNomeOuCpf(String termo) throws SQLException {
        List<Aluno> lista = new ArrayList<>();
        String sql = "SELECT * FROM aluno WHERE nome LIKE ? OR cpf LIKE ? ORDER BY nome";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + termo + "%");
            ps.setString(2, "%" + termo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Aluno mapear(ResultSet rs) throws SQLException {
        Aluno a = new Aluno();
        a.setCpf(rs.getString("cpf"));
        a.setNome(rs.getString("nome"));
        a.setDataNascimento(LocalDate.parse(rs.getString("data_nascimento")));
        a.setTelefone(rs.getString("telefone"));
        a.setEmail(rs.getString("email"));
        a.setObservacoes(rs.getString("observacoes"));
        return a;
    }
}
