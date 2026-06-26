package academia.ui;

import academia.dao.PlanoDAO;
import academia.model.Plano;
import academia.util.CampoObrigatorioException;
import academia.util.RegraNegocioException;
import academia.util.Tema;
import academia.util.ValorInvalidoException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Tela de CRUD de Planos, com regras de validação para valor/duração e código único.
 * Visual temático: cabeçalho grafite, formulário em card branco, tabela com cabeçalho escuro.
 */
public class TelaCrudPlano extends JDialog {

    private final PlanoDAO planoDAO = new PlanoDAO();

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNome = new JTextField();
    private final JTextField txtValor = new JTextField();
    private final JTextField txtDuracao = new JTextField();
    private final JTextArea txtBeneficios = new JTextArea(2, 20);

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"Código", "Nome", "Valor (R$)", "Duração (dias)", "Benefícios"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    public TelaCrudPlano(JFrame pai) {
        super(pai, "Cadastro de Planos", true);
        setSize(880, 600);
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

        carregarTabela();
    }

    private JPanel montarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Cadastro de Planos");
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
        card.add(Tema.tituloSecao("Dados do Plano"), c);
        c.gridwidth = 1;
        linha++;

        adicionarCampo(card, c, 0, linha, "Código (único):", txtCodigo);
        adicionarCampo(card, c, 2, linha, "Nome (ex: Mensal, Trimestral):", txtNome);
        linha++;
        adicionarCampo(card, c, 0, linha, "Valor (R$):", txtValor);
        adicionarCampo(card, c, 2, linha, "Duração (dias):", txtDuracao);
        linha++;

        c.gridx = 0; c.gridy = linha; c.gridwidth = 1;
        card.add(rotulo("Benefícios (texto curto):"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        txtBeneficios.setLineWrap(true);
        txtBeneficios.setWrapStyleWord(true);
        card.add(new JScrollPane(txtBeneficios), c);

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
        txtCodigo.setText((String) modeloTabela.getValueAt(linha, 0));
        txtCodigo.setEditable(false); // código é a chave primária
        txtNome.setText((String) modeloTabela.getValueAt(linha, 1));
        // remove formatação de moeda para reedição
        String valorTexto = ((String) modeloTabela.getValueAt(linha, 2)).replace(".", "").replace(",", ".");
        txtValor.setText(valorTexto);
        txtDuracao.setText(String.valueOf(modeloTabela.getValueAt(linha, 3)));
        txtBeneficios.setText((String) modeloTabela.getValueAt(linha, 4));
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        try {
            List<Plano> planos = planoDAO.listarTodos();
            for (Plano p : planos) {
                modeloTabela.addRow(new Object[]{
                        p.getCodigo(),
                        p.getNome(),
                        String.format(new Locale("pt", "BR"), "%,.2f", p.getValor()),
                        p.getDuracaoDias(),
                        p.getBeneficios()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar planos: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        txtCodigo.setText("");
        txtCodigo.setEditable(true);
        txtNome.setText("");
        txtValor.setText("");
        txtDuracao.setText("");
        txtBeneficios.setText("");
        tabela.clearSelection();
    }

    private void salvar() {
        try {
            Plano plano = validarFormulario();
            Plano existente = planoDAO.buscarPorCodigo(plano.getCodigo());
            if (existente == null) {
                planoDAO.inserir(plano);
                JOptionPane.showMessageDialog(this, "Plano cadastrado com sucesso!");
            } else {
                planoDAO.atualizar(plano);
                JOptionPane.showMessageDialog(this, "Plano atualizado com sucesso!");
            }
            limparFormulario();
            carregarTabela();
        } catch (CampoObrigatorioException | ValorInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (RegraNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regra de negócio", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar plano: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida os campos do formulário, garantindo valor positivo e duração em dias positiva.
     */
    private Plano validarFormulario() throws CampoObrigatorioException, ValorInvalidoException {
        String codigo = txtCodigo.getText().trim();
        String nome = txtNome.getText().trim();
        String valorTexto = txtValor.getText().trim().replace(",", ".");
        String duracaoTexto = txtDuracao.getText().trim();
        String beneficios = txtBeneficios.getText().trim();

        if (codigo.isEmpty()) {
            throw new CampoObrigatorioException("Informe o código do plano.");
        }
        if (nome.isEmpty()) {
            throw new CampoObrigatorioException("Informe o nome do plano.");
        }
        if (valorTexto.isEmpty()) {
            throw new CampoObrigatorioException("Informe o valor do plano.");
        }
        if (duracaoTexto.isEmpty()) {
            throw new CampoObrigatorioException("Informe a duração do plano em dias.");
        }

        BigDecimal valor;
        try {
            valor = new BigDecimal(valorTexto);
        } catch (NumberFormatException ex) {
            throw new ValorInvalidoException("Valor do plano inválido. Use apenas números (ex: 99.90).");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor do plano deve ser maior que zero.");
        }

        int duracaoDias;
        try {
            duracaoDias = Integer.parseInt(duracaoTexto);
        } catch (NumberFormatException ex) {
            throw new ValorInvalidoException("Duração inválida. Informe um número inteiro de dias.");
        }
        if (duracaoDias <= 0) {
            throw new ValorInvalidoException("A duração do plano deve ser maior que zero dias.");
        }

        return new Plano(codigo, nome, valor, duracaoDias, beneficios.isEmpty() ? null : beneficios);
    }

    private JPanel montarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        painel.setBackground(Tema.BRANCO);

        JButton btnNovo = Tema.botaoSecundario("Novo");
        btnNovo.addActionListener(e -> limparFormulario());

        JButton btnSalvar = Tema.botaoPrimario("Salvar (Incluir/Atualizar)");
        btnSalvar.addActionListener(e -> salvar());

        JButton btnExcluir = Tema.botaoPerigo("Excluir");
        btnExcluir.addActionListener(e -> excluir());

        JButton btnFechar = Tema.botaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dispose());

        painel.add(btnNovo);
        painel.add(btnSalvar);
        painel.add(btnExcluir);
        painel.add(btnFechar);
        return painel;
    }

    private void excluir() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um plano na tabela para excluir.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String codigo = (String) modeloTabela.getValueAt(linha, 0);
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir o plano " + codigo + "?\n"
                + "Não é possível excluir planos com matrículas vinculadas.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            planoDAO.excluir(codigo);
            JOptionPane.showMessageDialog(this, "Plano excluído com sucesso!");
            limparFormulario();
            carregarTabela();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível excluir este plano. Verifique se há matrículas vinculadas a ele.\n\n"
                    + "Detalhe técnico: " + ex.getMessage(),
                    "Erro ao excluir", JOptionPane.ERROR_MESSAGE);
        }
    }
}
