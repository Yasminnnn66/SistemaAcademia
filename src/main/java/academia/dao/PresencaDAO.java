package academia.dao;

import academia.model.Presenca;
import academia.util.ConexaoBD;
import academia.util.RegraNegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object responsável pelo controle de presença (check-in) dos alunos.
 */
public class PresencaDAO {

    /**
     * Registra o check-in do aluno. Impede registro duplicado no mesmo dia,
     * para refletir corretamente o relatório de "Horários de Pico" e "Alunos Inativos".
     */
    public void registrarCheckIn(String cpfAluno) throws SQLException, RegraNegocioException {
        if (jaFezCheckInHoje(cpfAluno)) {
            throw new RegraNegocioException("Este aluno já registrou check-in hoje.");
        }

        String sql = "INSERT INTO presenca (cpf_aluno, data_hora) VALUES (?, ?)";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cpfAluno);
            ps.setString(2, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public boolean jaFezCheckInHoje(String cpfAluno) throws SQLException {
        String sql = "SELECT COUNT(*) FROM presenca WHERE cpf_aluno = ? AND date(data_hora) = date('now', 'localtime')";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfAluno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retorna a data/hora do último check-in do aluno (ou null se nunca fez check-in).
     */
    public LocalDateTime buscarUltimoCheckIn(String cpfAluno) throws SQLException {
        String sql = "SELECT data_hora FROM presenca WHERE cpf_aluno = ? ORDER BY data_hora DESC LIMIT 1";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfAluno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return LocalDateTime.parse(rs.getString(1));
                }
            }
        }
        return null;
    }

    /**
     * Relatório 2: Presenças por período — lista detalhada de cada check-in no intervalo.
     */
    public List<Presenca> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        List<Presenca> lista = new ArrayList<>();
        String sql = "SELECT p.*, a.nome AS nome_aluno FROM presenca p "
                + "JOIN aluno a ON a.cpf = p.cpf_aluno "
                + "WHERE date(p.data_hora) BETWEEN ? AND ? "
                + "ORDER BY p.data_hora";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Presenca p = mapear(rs);
                    p.setNomeAluno(rs.getString("nome_aluno"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    /**
     * Relatório 2 (fluxo diário): quantidade de check-ins por dia, dentro do período.
     */
    public List<Object[]> fluxoDiarioPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        List<Object[]> resultado = new ArrayList<>();
        String sql = "SELECT date(data_hora) AS dia, COUNT(*) AS qtd FROM presenca "
                + "WHERE date(data_hora) BETWEEN ? AND ? "
                + "GROUP BY date(data_hora) ORDER BY dia";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Object[]{rs.getString("dia"), rs.getInt("qtd")});
                }
            }
        }
        return resultado;
    }

    /**
     * Relatório 3: Alunos inativos — sem check-in nos últimos X dias (ou nunca fizeram check-in).
     */
    public List<Object[]> alunosInativos(int dias) throws SQLException {
        List<Object[]> resultado = new ArrayList<>();
        String sql = "SELECT a.cpf, a.nome, "
                + "(SELECT MAX(data_hora) FROM presenca pr WHERE pr.cpf_aluno = a.cpf) AS ultimo_checkin "
                + "FROM aluno a "
                + "WHERE a.cpf NOT IN ("
                + "   SELECT cpf_aluno FROM presenca WHERE date(data_hora) >= date('now', ?)"
                + ") "
                + "ORDER BY a.nome";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "-" + dias + " days");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Object[]{
                            rs.getString("cpf"),
                            rs.getString("nome"),
                            rs.getString("ultimo_checkin") // pode ser null
                    });
                }
            }
        }
        return resultado;
    }

    /**
     * Relatório 7: Horários de pico — distribuição da quantidade de check-ins por faixa horária (0h-23h).
     */
    public List<Object[]> horariosDePico() throws SQLException {
        List<Object[]> resultado = new ArrayList<>();
        String sql = "SELECT CAST(strftime('%H', data_hora) AS INTEGER) AS hora, COUNT(*) AS qtd "
                + "FROM presenca GROUP BY hora ORDER BY hora";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(new Object[]{rs.getInt("hora"), rs.getInt("qtd")});
            }
        }
        return resultado;
    }

    private Presenca mapear(ResultSet rs) throws SQLException {
        Presenca p = new Presenca();
        p.setId(rs.getInt("id"));
        p.setCpfAluno(rs.getString("cpf_aluno"));
        p.setDataHora(LocalDateTime.parse(rs.getString("data_hora")));
        return p;
    }
}
