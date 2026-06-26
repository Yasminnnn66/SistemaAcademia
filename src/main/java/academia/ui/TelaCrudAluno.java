package academia.ui;

import academia.dao.AlunoDAO;
import academia.model.Aluno;
import academia.util.CampoObrigatorioException;
import academia.util.CpfInvalidoException;
import academia.util.RegraNegocioException;
import academia.util.Tema;
import academia.util.ValidadorCpf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tela de CRUD de Alunos, com validação de CPF e de contatos (telefone/e-mail).
 */
public class TelaCrudAluno extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern PADRAO_EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PADRAO_TELEFONE = Pattern.compile("^[0-9()\\-\\s]{8,15}$");

    private final AlunoDAO alunoDAO = new AlunoDAO();

    private final JTextField txtCpf = new JTextField();
    private final JTextField txtNome = new JTextField();
    private final JTextField txtDataNascimento = new JTextField();
    private final JTextField txtTelefone = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextArea txtObservacoes = new JTextArea(3, 20);


    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"CPF", "Nome", "Nascimento", "Telefone", "E-mail"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    public TelaCrudAluno(JFrame pai) {
        super(pai, "Cadastro de Alunos", true);
        setSize(880, 620);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.BRANCO);

        add(montarCabecalho(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setBackground(Tema.BRANCO);
        centro.setBorder(BorderFactory.createEmptyBorder(16, 24, 0, 24));
        centro.add(montarPainelFormulario(), BorderLayout.NORTH);
        centro.add(montarPainelTabela(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        add(montarPainelBotoes(), BorderLayout.SOUTH);

        carregarTabela(null);
    }

    private JPanel montarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        tabela.setForeground(Color.BLACK);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Cadastro de Alunos");
        titulo.setFont(Tema.FONTE_SUBTITULO.deriveFont(18f));
        titulo.setForeground(Color.WHITE);
        topo.add(titulo, BorderLayout.WEST);
        return topo;
    }

    private JPanel montarPainelFormulario() {
        JPanel card = Tema.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        int linha = 0;
        c.gridx = 0; c.gridy = linha; c.gridwidth = 4;
        card.add(Tema.tituloSecao("Dados do Aluno"), c);
        c.gridwidth = 1;
        linha++;

        adicionarCampo(card, c, 0, linha, "CPF (somente números):", txtCpf);
        adicionarCampo(card, c, 2, linha, "Nome:", txtNome);
        linha++;
        adicionarCampo(card, c, 0, linha, "Nascimento (dd/MM/aaaa):", txtDataNascimento);
        adicionarCampo(card, c, 2, linha, "Telefone:", txtTelefone);
        linha++;
        adicionarCampo(card, c, 0, linha, "E-mail:", txtEmail);
        linha++;

        c.gridx = 0; c.gridy = linha; c.gridwidth = 1;
        card.add(rotulo("Restrições/Observações (opcional):"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setRows(2);
        card.add(new JScrollPane(txtObservacoes), c);

        return card;
    }

    private JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.FONTE_LABEL);
        l.setForeground(Tema.CINZA_MEDIO.darker());
        return l;
    }

    private void adicionarCampo(JPanel painel, GridBagConstraints c, int colInicial, int linha, String rotuloTexto, JComponent campo) {
        c.gridx = colInicial;
        c.gridy = linha;
        c.weightx = 0;
        c.gridwidth = 1;
        painel.add(rotulo(rotuloTexto), c);
        c.gridx = colInicial + 1;
        c.weightx = 1;
        painel.add(campo, c);
    }

    private JScrollPane montarPainelTabela() {
        tabela.setRowHeight(28);
        tabela.setFont(Tema.FONTE_LABEL);
        tabela.setSelectionBackground(Tema.LARANJA_CLARO);
        tabela.setSelectionForeground(Tema.PRETO_GRAFITE);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setFillsViewportHeight(true);


        JTableHeader header = tabela.getTableHeader();
        header.setFont(Tema.FONTE_LABEL_BOLD);
        header.setBackground(Tema.PRETO_GRAFITE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));
        header.setOpaque(true);
        header.setBackground(Color.BLACK);
        header.setForeground(Color.RED);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha >= 0) {
                preencherFormularioComLinhaSelecionada(linha);
            }
        });

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new Tema.BordaArredondada(10, Tema.CINZA_CLARO));
        return scroll;
    }

    private void preencherFormularioComLinhaSelecionada(int linha) {
        txtCpf.setText((String) modeloTabela.getValueAt(linha, 0));
        txtCpf.setEditable(false); // CPF é a chave primária, não pode ser alterado em uma edição
        txtNome.setText((String) modeloTabela.getValueAt(linha, 1));
        txtDataNascimento.setText((String) modeloTabela.getValueAt(linha, 2));
        txtTelefone.setText((String) modeloTabela.getValueAt(linha, 3));
        txtEmail.setText((String) modeloTabela.getValueAt(linha, 4));
        try {
            Aluno a = alunoDAO.buscarPorCpf(ValidadorCpf.somenteNumeros(txtCpf.getText()));
            txtObservacoes.setText(a != null ? a.getObservacoes() : "");
        } catch (SQLException ex) {
            txtObservacoes.setText("");
        }
    }

    private JPanel montarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        painel.setBackground(Tema.BRANCO);
        painel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JTextField txtBusca = new JTextField(15);
        JButton btnBuscar = Tema.botaoSecundario("Buscar");
        btnBuscar.addActionListener(e -> carregarTabela(txtBusca.getText().trim()));

        JButton btnNovo = Tema.botaoSecundario("Novo");
        btnNovo.addActionListener(e -> limparFormulario());

        JButton btnSalvar = Tema.botaoPrimario("Salvar (Incluir/Atualizar)");
        btnSalvar.addActionListener(e -> salvar());

        JButton btnExcluir = Tema.botaoPerigo("Excluir");
        btnExcluir.addActionListener(e -> excluir());

        JButton btnFechar = Tema.botaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dispose());

        JLabel lblBusca = rotulo("Buscar (nome/CPF):");

        painel.add(lblBusca);
        painel.add(txtBusca);
        painel.add(btnBuscar);
        painel.add(btnNovo);
        painel.add(btnSalvar);
        painel.add(btnExcluir);
        painel.add(btnFechar);
        return painel;
    }

    private void carregarTabela(String termoBusca) {
        modeloTabela.setRowCount(0);
        try {
            List<Aluno> alunos = (termoBusca == null || termoBusca.isEmpty())
                    ? alunoDAO.listarTodos()
                    : alunoDAO.buscarPorNomeOuCpf(termoBusca);
            for (Aluno a : alunos) {
                modeloTabela.addRow(new Object[]{
                        a.getCpf(),
                        a.getNome(),
                        a.getDataNascimento().format(FORMATO_DATA),
                        a.getTelefone(),
                        a.getEmail()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar alunos: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        txtCpf.setText("");
        txtCpf.setEditable(true);
        txtNome.setText("");
        txtDataNascimento.setText("");
        txtTelefone.setText("");
        txtEmail.setText("");
        txtObservacoes.setText("");
        tabela.clearSelection();
    }

    private void salvar() {
        try {
            Aluno aluno = validarFormulario();
            Aluno existente = alunoDAO.buscarPorCpf(aluno.getCpf());
            if (existente == null) {
                alunoDAO.inserir(aluno);
                JOptionPane.showMessageDialog(this, "Aluno cadastrado com sucesso!");
            } else {
                alunoDAO.atualizar(aluno);
                JOptionPane.showMessageDialog(this, "Dados do aluno atualizados com sucesso!");
            }
            limparFormulario();
            carregarTabela(null);
        } catch (CampoObrigatorioException | CpfInvalidoException | DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, mensagemAmigavel(ex), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (RegraNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regra de negócio", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar aluno: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String mensagemAmigavel(Exception ex) {
        if (ex instanceof DateTimeParseException) {
            return "Data de nascimento inválida. Utilize o formato dd/MM/aaaa.";
        }
        return ex.getMessage();
    }

    /**
     * Valida todos os campos do formulário e retorna um objeto Aluno pronto para persistir.
     * Lança exceções customizadas específicas para cada tipo de problema encontrado.
     */
    private Aluno validarFormulario() throws CampoObrigatorioException, CpfInvalidoException {
        String cpfDigitado = txtCpf.getText().trim();
        String nome = txtNome.getText().trim();
        String dataTexto = txtDataNascimento.getText().trim();
        String telefone = txtTelefone.getText().trim();
        String email = txtEmail.getText().trim();
        String observacoes = txtObservacoes.getText().trim();

        if (cpfDigitado.isEmpty()) {
            throw new CampoObrigatorioException("Informe o CPF do aluno.");
        }
        if (nome.isEmpty()) {
            throw new CampoObrigatorioException("Informe o nome do aluno.");
        }
        if (dataTexto.isEmpty()) {
            throw new CampoObrigatorioException("Informe a data de nascimento.");
        }
        if (telefone.isEmpty()) {
            throw new CampoObrigatorioException("Informe o telefone do aluno.");
        }
        if (email.isEmpty()) {
            throw new CampoObrigatorioException("Informe o e-mail do aluno.");
        }

        String cpfNumerico = ValidadorCpf.somenteNumeros(cpfDigitado);
        if (!ValidadorCpf.isValido(cpfNumerico)) {
            throw new CpfInvalidoException("CPF inválido. Verifique os números digitados.");
        }

        if (!PADRAO_TELEFONE.matcher(telefone).matches()) {
            throw new CampoObrigatorioException("Telefone inválido. Use apenas números, parênteses, espaços e traço.");
        }

        if (!PADRAO_EMAIL.matcher(email).matches()) {
            throw new CampoObrigatorioException("E-mail inválido. Verifique o formato (ex: nome@dominio.com).");
        }

        LocalDate dataNascimento;
        try {
            dataNascimento = LocalDate.parse(dataTexto, FORMATO_DATA);
        } catch (DateTimeException ex) {
            throw new CampoObrigatorioException("Data de nascimento inválida. Utilize o formato dd/MM/aaaa.");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new CampoObrigatorioException("Data de nascimento não pode ser no futuro.");
        }

        return new Aluno(cpfNumerico, nome, dataNascimento, telefone, email,
                observacoes.isEmpty() ? null : observacoes);
    }

    private void excluir() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno na tabela para excluir.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String cpf = (String) modeloTabela.getValueAt(linha, 0);
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir o aluno de CPF " + cpf + "?\n"
                + "Não é possível excluir alunos com matrículas, fichas ou presenças registradas.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            alunoDAO.excluir(cpf);
            JOptionPane.showMessageDialog(this, "Aluno excluído com sucesso!");
            limparFormulario();
            carregarTabela(null);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível excluir este aluno. Verifique se há matrículas, fichas de treino "
                    + "ou presenças vinculadas a ele.\n\nDetalhe técnico: " + ex.getMessage(),
                    "Erro ao excluir", JOptionPane.ERROR_MESSAGE);
        }
    }
}
