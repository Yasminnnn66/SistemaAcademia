package academia.dao;

import academia.model.FichaTreino;
import academia.model.HistoricoFicha;
import academia.util.ConexaoBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object responsável pelas fichas de treino (A/B/C) e seu histórico de alterações.
 */
public class FichaTreinoDAO {

    /**
     * Insere uma nova ficha de treino para o aluno.
     */
    public void inserir(FichaTreino ficha) throws SQLException {
        String sql = "INSERT INTO ficha_treino (cpf_aluno, letra_ficha, exercicios, metas, ultima_atualizacao) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ficha.getCpfAluno());
            ps.setString(2, ficha.getLetraFicha());
            ps.setString(3, ficha.getExercicios());
            ps.setString(4, ficha.getMetas());
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ficha.setId(rs.getInt(1));
                }
            }
        }
        registrarHistorico(ficha.getId(), ficha.getExercicios(), ficha.getMetas());
    }

    /**
     * Atualiza uma ficha existente e grava automaticamente um snapshot no histórico,
     * atendendo ao requisito de "histórico de alterações" da tela de Treinos.
     */
    public void atualizar(FichaTreino ficha) throws SQLException {
        String sql = "UPDATE ficha_treino SET exercicios = ?, metas = ?, ultima_atualizacao = ? WHERE id = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ficha.getExercicios());
            ps.setString(2, ficha.getMetas());
            ps.setString(3, LocalDateTime.now().toString());
            ps.setInt(4, ficha.getId());
            ps.executeUpdate();
        }
        registrarHistorico(ficha.getId(), ficha.getExercicios(), ficha.getMetas());
    }

    public void excluir(int id) throws SQLException {
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM historico_ficha WHERE ficha_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ficha_treino WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Lista todas as fichas de um aluno (A, B, C...), ordenadas pela letra.
     */
    public List<FichaTreino> listarPorAluno(String cpfAluno) throws SQLException {
        List<FichaTreino> lista = new ArrayList<>();
        String sql = "SELECT * FROM ficha_treino WHERE cpf_aluno = ? ORDER BY letra_ficha";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfAluno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Lista todas as fichas de todos os alunos com nome do aluno (para o relatório "Treinos por Aluno").
     */
    public List<FichaTreino> listarTodasComNomeAluno() throws SQLException {
        List<FichaTreino> lista = new ArrayList<>();
        String sql = "SELECT f.*, a.nome AS nome_aluno FROM ficha_treino f "
                + "JOIN aluno a ON a.cpf = f.cpf_aluno "
                + "ORDER BY a.nome, f.letra_ficha";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FichaTreino f = mapear(rs);
                f.setNomeAluno(rs.getString("nome_aluno"));
                lista.add(f);
            }
        }
        return lista;
    }

    /**
     * Retorna o histórico de alterações de uma ficha, do mais recente para o mais antigo.
     */
    public List<HistoricoFicha> listarHistorico(int fichaId) throws SQLException {
        List<HistoricoFicha> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_ficha WHERE ficha_id = ? ORDER BY data_alteracao DESC";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fichaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistoricoFicha h = new HistoricoFicha();
                    h.setId(rs.getInt("id"));
                    h.setFichaId(rs.getInt("ficha_id"));
                    h.setExercicios(rs.getString("exercicios"));
                    h.setMetas(rs.getString("metas"));
                    h.setDataAlteracao(LocalDateTime.parse(rs.getString("data_alteracao")));
                    lista.add(h);
                }
            }
        }
        return lista;
    }

    private void registrarHistorico(int fichaId, String exercicios, String metas) throws SQLException {
        String sql = "INSERT INTO historico_ficha (ficha_id, exercicios, metas, data_alteracao) VALUES (?, ?, ?, ?)";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fichaId);
            ps.setString(2, exercicios);
            ps.setString(3, metas);
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private FichaTreino mapear(ResultSet rs) throws SQLException {
        FichaTreino f = new FichaTreino();
        f.setId(rs.getInt("id"));
        f.setCpfAluno(rs.getString("cpf_aluno"));
        f.setLetraFicha(rs.getString("letra_ficha"));
        f.setExercicios(rs.getString("exercicios"));
        f.setMetas(rs.getString("metas"));
        f.setUltimaAtualizacao(LocalDateTime.parse(rs.getString("ultima_atualizacao")));
        return f;
    }
}
