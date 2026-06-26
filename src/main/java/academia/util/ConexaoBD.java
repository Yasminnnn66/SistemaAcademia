package academia.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Responsável por criar e fornecer a conexão única com o banco SQLite,
 * além de criar as tabelas do sistema caso ainda não existam (auto-setup).
 *
 * O arquivo do banco (academia.db) é criado automaticamente na primeira
 * execução, na raiz do projeto.
 */
public class ConexaoBD {

    private static final String URL = "jdbc:sqlite:academia.db";
    private static Connection conexao;

    private ConexaoBD() {
    }

    /**
     * Retorna a conexão ativa com o banco, criando-a (e as tabelas) se necessário.
     */
    public static Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                String classpathAtual = System.getProperty("java.class.path");
                throw new SQLException(
                        "Driver JDBC do SQLite não encontrado no classpath.\n\n"
                        + "Verifique se o arquivo sqlite-jdbc.jar foi adicionado às dependências do projeto:\n"
                        + "- Eclipse: clique direito no projeto > Build Path > Configure Build Path > Libraries > Add JARs\n"
                        + "- IntelliJ: File > Project Structure > Modules > Dependencies > '+' > JARs or Directories\n\n"
                        + "Classpath atual usado por esta execução:\n" + classpathAtual, e);
            }
            conexao = DriverManager.getConnection(URL);
            // Garante que chaves estrangeiras sejam respeitadas pelo SQLite
            try (Statement stmt = conexao.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            criarTabelas(conexao);
        }
        return conexao;
    }

    /**
     * Cria todas as tabelas do sistema caso ainda não existam.
     * Usar CREATE TABLE IF NOT EXISTS evita erros em execuções subsequentes.
     */
    private static void criarTabelas(Connection conn) throws SQLException {
        String sqlAluno = """
            CREATE TABLE IF NOT EXISTS aluno (
                cpf TEXT PRIMARY KEY,
                nome TEXT NOT NULL,
                data_nascimento TEXT NOT NULL,
                telefone TEXT,
                email TEXT,
                observacoes TEXT
            );
        """;

        String sqlPlano = """
            CREATE TABLE IF NOT EXISTS plano (
                codigo TEXT PRIMARY KEY,
                nome TEXT NOT NULL,
                valor REAL NOT NULL,
                duracao_dias INTEGER NOT NULL,
                beneficios TEXT
            );
        """;

        String sqlMatricula = """
            CREATE TABLE IF NOT EXISTS matricula (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cpf_aluno TEXT NOT NULL,
                codigo_plano TEXT NOT NULL,
                data_inicio TEXT NOT NULL,
                data_vencimento TEXT NOT NULL,
                status TEXT NOT NULL,
                motivo_cancelamento TEXT,
                data_cancelamento TEXT,
                FOREIGN KEY (cpf_aluno) REFERENCES aluno(cpf),
                FOREIGN KEY (codigo_plano) REFERENCES plano(codigo)
            );
        """;

        String sqlFicha = """
            CREATE TABLE IF NOT EXISTS ficha_treino (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cpf_aluno TEXT NOT NULL,
                letra_ficha TEXT NOT NULL,
                exercicios TEXT,
                metas TEXT,
                ultima_atualizacao TEXT NOT NULL,
                FOREIGN KEY (cpf_aluno) REFERENCES aluno(cpf)
            );
        """;

        String sqlHistoricoFicha = """
            CREATE TABLE IF NOT EXISTS historico_ficha (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ficha_id INTEGER NOT NULL,
                exercicios TEXT,
                metas TEXT,
                data_alteracao TEXT NOT NULL,
                FOREIGN KEY (ficha_id) REFERENCES ficha_treino(id)
            );
        """;

        String sqlPresenca = """
            CREATE TABLE IF NOT EXISTS presenca (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cpf_aluno TEXT NOT NULL,
                data_hora TEXT NOT NULL,
                FOREIGN KEY (cpf_aluno) REFERENCES aluno(cpf)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlAluno);
            stmt.execute(sqlPlano);
            stmt.execute(sqlMatricula);
            stmt.execute(sqlFicha);
            stmt.execute(sqlHistoricoFicha);
            stmt.execute(sqlPresenca);
        }
    }

    /**
     * Fecha a conexão com o banco. Deve ser chamado ao encerrar a aplicação.
     */
    public static void fecharConexao() {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
