package academia.dao;

import academia.model.Plano;
import academia.util.ConexaoBD;
import academia.util.RegraNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object responsável pelas operações de CRUD da tabela "plano".
 */
public class PlanoDAO {

    /**
     * Insere um novo plano. Lança RegraNegocioException se o código já existir
     * (garante código único, conforme exigido na tela de CRUD de Planos).
     */
    public void inserir(Plano plano) throws SQLException, RegraNegocioException {
        if (buscarPorCodigo(plano.getCodigo()) != null) {
            throw new RegraNegocioException("Já existe um plano cadastrado com o código " + plano.getCodigo() + ".");
        }

        String sql = "INSERT INTO plano (codigo, nome, valor, duracao_dias, beneficios) VALUES (?, ?, ?, ?, ?)";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plano.getCodigo());
            ps.setString(2, plano.getNome());
            ps.setBigDecimal(3, plano.getValor());
            ps.setInt(4, plano.getDuracaoDias());
            ps.setString(5, plano.getBeneficios());
            ps.executeUpdate();
        }
    }

    /**
     * Atualiza os dados de um plano (o código não pode ser alterado, é a chave primária).
     */
    public void atualizar(Plano plano) throws SQLException {
        String sql = "UPDATE plano SET nome = ?, valor = ?, duracao_dias = ?, beneficios = ? WHERE codigo = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plano.getNome());
            ps.setBigDecimal(2, plano.getValor());
            ps.setInt(3, plano.getDuracaoDias());
            ps.setString(4, plano.getBeneficios());
            ps.setString(5, plano.getCodigo());
            ps.executeUpdate();
        }
    }

    /**
     * Remove um plano pelo código.
     * Atenção: o banco não permite excluir plano com matrículas vinculadas (chave estrangeira).
     */
    public void excluir(String codigo) throws SQLException {
        String sql = "DELETE FROM plano WHERE codigo = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    /**
     * Busca um plano pelo código. Retorna null se não encontrado.
     */
    public Plano buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM plano WHERE codigo = ?";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todos os planos cadastrados, ordenados por nome.
     */
    public List<Plano> listarTodos() throws SQLException {
        List<Plano> lista = new ArrayList<>();
        String sql = "SELECT * FROM plano ORDER BY nome";
        Connection conn = ConexaoBD.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Plano mapear(ResultSet rs) throws SQLException {
        Plano p = new Plano();
        p.setCodigo(rs.getString("codigo"));
        p.setNome(rs.getString("nome"));
        p.setValor(rs.getBigDecimal("valor"));
        p.setDuracaoDias(rs.getInt("duracao_dias"));
        p.setBeneficios(rs.getString("beneficios"));
        return p;
    }
}
