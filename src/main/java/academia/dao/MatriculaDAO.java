package academia.dao;

import academia.model.Matricula;
import academia.util.ConexaoBD;
import academia.util.RegraNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object responsável pelas operações da tabela "matricula":
 * matrícula/vínculo de plano, renovação e cancelamento.
 */
public class MatriculaDAO {

    /**
     * Cria uma nova matrícula vinculando aluno e plano.
     * Bloqueia caso o aluno já possua uma matrícula ATIVA (regra de negócio).
     */
    public void inserir(Matricula m) throws SQLException, RegraNegocioException {
        Matricula ativa = buscarMatriculaAtivaDoAluno(m.getCpfAluno());
        if (ativa != null) {
            throw new RegraNegocioException(
                    "O aluno já possui uma matrícula ativa (plano " + ativa.getCodigoPlano()
                    + ", vencimento em " + ativa.getDataVencimento() + "). "
                    + "Cancele ou aguarde o vencimento antes de matricular em outro plano.");
        }

        String sql = "INSERT INTO matricula (cpf_aluno, codigo_plano, data_inicio, data_vencimento, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getCpfAluno());
            ps.setString(2, m.getCodigoPlano());
            ps.setString(3, m.getDataInicio().toString());
            ps.setString(4, m.getDataVencimento().toString());
            ps.setString(5, Matricula.STATUS_ATIVA);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Renova a matrícula: define uma nova data de vencimento a partir da duração do plano
     * e garante que o status volte para ATIVA.
     */
    public void renovar(int matriculaId, LocalDate novaDataVencimento) throws SQLException {
        String sql = "UPDATE matricula SET data_vencimento = ?, status = ? WHERE id = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novaDataVencimento.toString());
            ps.setString(2, Matricula.STATUS_ATIVA);
            ps.setInt(3, matriculaId);
            ps.executeUpdate();
        }
    }

    /**
     * Cancela a matrícula registrando o motivo e a data do cancelamento.
     */
    public void cancelar(int matriculaId, String motivo) throws SQLException {
        String sql = "UPDATE matricula SET status = ?, motivo_cancelamento = ?, data_cancelamento = ? WHERE id = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Matricula.STATUS_CANCELADA);
            ps.setString(2, motivo);
            ps.setString(3, LocalDate.now().toString());
            ps.setInt(4, matriculaId);
            ps.executeUpdate();
        }
    }

    /**
     * Atualiza para VENCIDA todas as matrículas ATIVAS cuja data de vencimento já passou.
     * Deve ser chamado ao iniciar o sistema e antes de gerar relatórios, para manter
     * o status do banco sempre coerente com a data atual.
     */
    public void atualizarStatusVencidas() throws SQLException {
        String sql = "UPDATE matricula SET status = ? WHERE status = ? AND data_vencimento < ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Matricula.STATUS_VENCIDA);
            ps.setString(2, Matricula.STATUS_ATIVA);
            ps.setString(3, LocalDate.now().toString());
            ps.executeUpdate();
        }
    }

    /**
     * Busca a matrícula ATIVA mais recente de um aluno (se houver).
     * Usado para a regra de bloqueio de check-in e para impedir matrícula duplicada.
     */
    public Matricula buscarMatriculaAtivaDoAluno(String cpfAluno) throws SQLException {
        String sql = "SELECT * FROM matricula WHERE cpf_aluno = ? AND status = ? "
                + "ORDER BY data_vencimento DESC LIMIT 1";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfAluno);
            ps.setString(2, Matricula.STATUS_ATIVA);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca a matrícula mais recente (qualquer status) de um aluno.
     * Útil nas telas de Check-in e Renovação/Cancelamento para localizar o vínculo atual.
     */
    public Matricula buscarMatriculaMaisRecenteDoAluno(String cpfAluno) throws SQLException {
        String sql = "SELECT * FROM matricula WHERE cpf_aluno = ? ORDER BY id DESC LIMIT 1";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfAluno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public Matricula buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM matricula WHERE id = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todas as matrículas com dados do aluno e do plano (join), para exibição em telas/relatórios.
     */
    public List<Matricula> listarTodasComDetalhes() throws SQLException {
        String sql = "SELECT m.*, a.nome AS nome_aluno, p.nome AS nome_plano, p.valor AS valor_plano "
                + "FROM matricula m "
                + "JOIN aluno a ON a.cpf = m.cpf_aluno "
                + "JOIN plano p ON p.codigo = m.codigo_plano "
                + "ORDER BY m.data_vencimento DESC";
        return listarComQuery(sql, null);
    }

    /**
     * Relatório 1: Planos ativos e vencendo nos próximos X dias.
     */
    public List<Matricula> relatorioPlanosAtivosEVencendo(int diasLimite) throws SQLException {
        String sql = "SELECT m.*, a.nome AS nome_aluno, p.nome AS nome_plano, p.valor AS valor_plano "
                + "FROM matricula m "
                + "JOIN aluno a ON a.cpf = m.cpf_aluno "
                + "JOIN plano p ON p.codigo = m.codigo_plano "
                + "WHERE m.status = 'ATIVA' AND m.data_vencimento <= ? "
                + "ORDER BY m.data_vencimento";
        String limite = LocalDate.now().plusDays(diasLimite).toString();
        return listarComQuery(sql, limite);
    }

    /**
     * Relatório 4: Receita por plano dentro de um período (somatório do valor do plano
     * para cada matrícula iniciada/renovada dentro do período informado).
     */
    public List<Object[]> relatorioReceitaPorPlano(LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = "SELECT p.codigo, p.nome, COUNT(*) AS qtd, SUM(p.valor) AS total "
                + "FROM matricula m "
                + "JOIN plano p ON p.codigo = m.codigo_plano "
                + "WHERE m.data_inicio BETWEEN ? AND ? "
                + "GROUP BY p.codigo, p.nome "
                + "ORDER BY total DESC";
        List<Object[]> resultado = new ArrayList<>();
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Object[]{
                            rs.getString("codigo"),
                            rs.getString("nome"),
                            rs.getInt("qtd"),
                            rs.getBigDecimal("total")
                    });
                }
            }
        }
        return resultado;
    }

    /**
     * Relatório 6: Cancelamentos por período, com motivos.
     */
    public List<Matricula> relatorioCancelamentosPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = "SELECT m.*, a.nome AS nome_aluno, p.nome AS nome_plano, p.valor AS valor_plano "
                + "FROM matricula m "
                + "JOIN aluno a ON a.cpf = m.cpf_aluno "
                + "JOIN plano p ON p.codigo = m.codigo_plano "
                + "WHERE m.status = 'CANCELADA' AND m.data_cancelamento BETWEEN ? AND ? "
                + "ORDER BY m.data_cancelamento DESC";
        List<Matricula> lista = new ArrayList<>();
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearComDetalhes(rs));
                }
            }
        }
        return lista;
    }

    private List<Matricula> listarComQuery(String sql, String parametroData) throws SQLException {
        List<Matricula> lista = new ArrayList<>();
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parametroData != null) {
                ps.setString(1, parametroData);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearComDetalhes(rs));
                }
            }
        }
        return lista;
    }

    private Matricula mapear(ResultSet rs) throws SQLException {
        Matricula m = new Matricula();
        m.setId(rs.getInt("id"));
        m.setCpfAluno(rs.getString("cpf_aluno"));
        m.setCodigoPlano(rs.getString("codigo_plano"));
        m.setDataInicio(LocalDate.parse(rs.getString("data_inicio")));
        m.setDataVencimento(LocalDate.parse(rs.getString("data_vencimento")));
        m.setStatus(rs.getString("status"));
        m.setMotivoCancelamento(rs.getString("motivo_cancelamento"));
        String dataCanc = rs.getString("data_cancelamento");
        if (dataCanc != null) {
            m.setDataCancelamento(LocalDate.parse(dataCanc));
        }
        return m;
    }

    private Matricula mapearComDetalhes(ResultSet rs) throws SQLException {
        Matricula m = mapear(rs);
        m.setNomeAluno(rs.getString("nome_aluno"));
        m.setNomePlano(rs.getString("nome_plano"));
        m.setValorPlano(rs.getBigDecimal("valor_plano"));
        return m;
    }
}
