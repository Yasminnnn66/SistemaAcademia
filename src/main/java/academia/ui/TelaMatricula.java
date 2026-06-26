package academia.ui;

import academia.dao.AlunoDAO;
import academia.dao.MatriculaDAO;
import academia.dao.PlanoDAO;
import academia.model.Aluno;
import academia.model.Matricula;
import academia.model.Plano;
import academia.util.RegraNegocioException;
import academia.util.Tema;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class TelaMatricula extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlunoDAO alunoDAO = new AlunoDAO();
    private final PlanoDAO planoDAO = new PlanoDAO();
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();

    private final JComboBox<Aluno> comboAlunos = new JComboBox<>();
    private final JComboBox<Plano> comboPlanos = new JComboBox<>();
    private final JTextField txtDataInicio = new JTextField(LocalDate.now().format(FORMATO_DATA));
    private final JLabel lblDataVencimento = new JLabel("--/--/----");
    private JLabel pilulaStatusAtual;
    private final JPanel painelStatusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    public TelaMatricula(JFrame pai) {
        super(pai, "Matrícula de Aluno", true);
        setSize(620, 480);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.BRANCO);

        add(montarCabecalho(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Tema.BRANCO);
        centro.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));
        centro.add(montarFormulario(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        add(montarBotoes(), BorderLayout.SOUTH);

        carregarCombos();
        atualizarVencimento();

        comboPlanos.addActionListener(e -> atualizarVencimento());
        comboAlunos.addActionListener(e -> atualizarStatusAtualDoAluno());
    }

    private JPanel montarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Matrícula de Aluno");
        titulo.setFont(Tema.FONTE_SUBTITULO.deriveFont(18f));
        titulo.setForeground(Color.WHITE);
        topo.add(titulo, BorderLayout.WEST);
        return topo;
    }

    private JPanel montarFormulario() {
        JPanel card = Tema.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        int linha = 0;
        c.gridx = 0; c.gridy = linha; c.gridwidth = 2;
        card.add(Tema.tituloSecao("Dados da Matrícula"), c);
        c.gridwidth = 1;
        linha++;

        c.gridx = 0; c.gridy = linha; c.weightx = 0; card.add(rotulo("Aluno:"), c);
        c.gridx = 1; c.weightx = 1; card.add(comboAlunos, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0; card.add(rotulo("Situação atual:"), c);
        c.gridx = 1; c.weightx = 1; painelStatusContainer.setOpaque(false); card.add(painelStatusContainer, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0; card.add(rotulo("Plano:"), c);
        c.gridx = 1; c.weightx = 1; card.add(comboPlanos, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0; card.add(rotulo("Data de início (dd/MM/aaaa):"), c);
        c.gridx = 1; c.weightx = 1; card.add(txtDataInicio, c);

        linha++;
        c.gridx = 0; c.gridy = linha; c.weightx = 0; card.add(rotulo("Vencimento calculado:"), c);
        c.gridx = 1; c.weightx = 1;
        lblDataVencimento.setFont(Tema.FONTE_SUBTITULO);
        lblDataVencimento.setForeground(Tema.LARANJA);
        card.add(lblDataVencimento, c);

        txtDataInicio.addActionListener(e -> atualizarVencimento());
        txtDataInicio.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                atualizarVencimento();
            }
        });

        return card;
    }

    private JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.FONTE_LABEL);
        l.setForeground(Tema.CINZA_MEDIO.darker());
        return l;
    }

    private JPanel montarBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 16));
        painel.setBackground(Tema.BRANCO);
        JButton btnMatricular = Tema.botaoPrimario("Matricular");
        btnMatricular.addActionListener(e -> matricular());
        JButton btnFechar = Tema.botaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dispose());
        painel.add(btnMatricular);
        painel.add(btnFechar);
        return painel;
    }

    private void carregarCombos() {
        try {
            List<Aluno> alunos = alunoDAO.listarTodos();
            for (Aluno a : alunos) {
                comboAlunos.addItem(a);
            }
            List<Plano> planos = planoDAO.listarTodos();
            for (Plano p : planos) {
                comboPlanos.addItem(p);
            }
            if (alunos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Não há alunos cadastrados. Cadastre um aluno antes de matricular.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            if (planos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Não há planos cadastrados. Cadastre um plano antes de matricular.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
        atualizarStatusAtualDoAluno();
    }

    private void atualizarStatusAtualDoAluno() {
        Aluno aluno = (Aluno) comboAlunos.getSelectedItem();
        painelStatusContainer.removeAll();
        if (aluno == null) {
            painelStatusContainer.revalidate();
            painelStatusContainer.repaint();
            return;
        }
        try {
            Matricula ativa = matriculaDAO.buscarMatriculaAtivaDoAluno(aluno.getCpf());
            if (ativa != null) {
                pilulaStatusAtual = Tema.pilulaStatus(
                        "Matrícula ativa até " + ativa.getDataVencimento().format(FORMATO_DATA),
                        Tema.StatusCor.VERMELHO);
            } else {
                pilulaStatusAtual = Tema.pilulaStatus("Sem matrícula ativa - pode ser matriculado",
                        Tema.StatusCor.VERDE);
            }
            painelStatusContainer.add(pilulaStatusAtual);
        } catch (SQLException ex) {
            painelStatusContainer.add(rotulo(" "));
        }
        painelStatusContainer.revalidate();
        painelStatusContainer.repaint();
    }

    private void atualizarVencimento() {
        Plano plano = (Plano) comboPlanos.getSelectedItem();
        if (plano == null) {
            lblDataVencimento.setText("--/--/----");
            return;
        }
        try {
            LocalDate inicio = LocalDate.parse(txtDataInicio.getText().trim(), FORMATO_DATA);
            LocalDate vencimento = inicio.plusDays(plano.getDuracaoDias());
            lblDataVencimento.setText(vencimento.format(FORMATO_DATA));
        } catch (Exception ex) {
            lblDataVencimento.setText("Data de início inválida");
        }
    }

    private void matricular() {
        Aluno aluno = (Aluno) comboAlunos.getSelectedItem();
        Plano plano = (Plano) comboPlanos.getSelectedItem();

        if (aluno == null || plano == null) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno e um plano.",
                    "Dados incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate inicio;
        try {
            inicio = LocalDate.parse(txtDataInicio.getText().trim(), FORMATO_DATA);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Data de início inválida. Utilize o formato dd/MM/aaaa.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate vencimento = inicio.plusDays(plano.getDuracaoDias());

        Matricula matricula = new Matricula();
        matricula.setCpfAluno(aluno.getCpf());
        matricula.setCodigoPlano(plano.getCodigo());
        matricula.setDataInicio(inicio);
        matricula.setDataVencimento(vencimento);

        try {
            matriculaDAO.inserir(matricula);
            JOptionPane.showMessageDialog(this,
                    "Matrícula realizada com sucesso!\nVencimento: " + vencimento.format(FORMATO_DATA));
            atualizarStatusAtualDoAluno();
        } catch (RegraNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regra de negócio", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao matricular aluno: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
