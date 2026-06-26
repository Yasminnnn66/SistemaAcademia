package academia.ui;

import academia.dao.AlunoDAO;
import academia.dao.MatriculaDAO;
import academia.dao.PlanoDAO;
import academia.model.Aluno;
import academia.model.Matricula;
import academia.model.Plano;
import academia.util.Tema;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de Renovação/Cancelamento: permite renovar (gerando novo vencimento)
 * ou cancelar a matrícula (com motivo obrigatório), sempre pedindo confirmação.
 * Visual temático com cartão de informações da matrícula e pílula de status.
 */
public class TelaRenovacaoCancelamento extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlunoDAO alunoDAO = new AlunoDAO();
    private final PlanoDAO planoDAO = new PlanoDAO();
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();

    private final JTextField txtBusca = new JTextField(20);
    private final JComboBox<Aluno> comboResultados = new JComboBox<>();
    private final JLabel lblInfoMatricula = new JLabel(" ");
    private final JPanel painelPilula = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JTextArea txtMotivo = new JTextArea(4, 25);

    private Matricula matriculaAtual;

    public TelaRenovacaoCancelamento(JFrame pai) {
        super(pai, "Renovação / Cancelamento de Matrícula", true);
        setSize(660, 600);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.BRANCO);

        add(montarCabecalho(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setBackground(Tema.BRANCO);
        centro.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));
        centro.add(montarPainelBusca(), BorderLayout.NORTH);
        centro.add(montarPainelInfo(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        add(montarPainelBotoes(), BorderLayout.SOUTH);

        comboResultados.addActionListener(e -> carregarMatricula());
    }

    private JPanel montarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Renovação / Cancelamento");
        titulo.setFont(Tema.FONTE_SUBTITULO.deriveFont(18f));
        titulo.setForeground(Color.WHITE);
        topo.add(titulo, BorderLayout.WEST);
        return topo;
    }

    private JPanel montarPainelBusca() {
        JPanel card = Tema.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton btnBuscar = Tema.botaoSecundario("Localizar");
        btnBuscar.addActionListener(e -> buscarAluno());
        txtBusca.addActionListener(e -> buscarAluno());

        JLabel rotulo = new JLabel("Nome ou CPF do aluno:");
        rotulo.setFont(Tema.FONTE_LABEL);
        rotulo.setForeground(Tema.CINZA_MEDIO.darker());

        c.gridx = 0; c.gridy = 0; card.add(rotulo, c);
        c.gridx = 1; c.weightx = 1; card.add(txtBusca, c);
        c.gridx = 2; c.weightx = 0; card.add(btnBuscar, c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 3; c.weightx = 1;
        card.add(comboResultados, c);

        return card;
    }

    private JPanel montarPainelInfo() {
        JPanel card = Tema.card();
        card.setLayout(new BorderLayout(10, 10));
        card.add(Tema.tituloSecao("Dados da Matrícula"), BorderLayout.NORTH);

        JPanel painelTopo = new JPanel();
        painelTopo.setOpaque(false);
        painelTopo.setLayout(new BoxLayout(painelTopo, BoxLayout.Y_AXIS));
        lblInfoMatricula.setVerticalAlignment(SwingConstants.TOP);
        lblInfoMatricula.setFont(Tema.FONTE_LABEL);
        painelPilula.setOpaque(false);
        painelTopo.add(lblInfoMatricula);
        painelTopo.add(Box.createVerticalStrut(8));
        painelTopo.add(painelPilula);

        JPanel painelMotivo = new JPanel(new BorderLayout(5, 5));
        painelMotivo.setOpaque(false);
        JLabel lblMotivo = new JLabel("Motivo do cancelamento (obrigatório apenas para cancelar):");
        lblMotivo.setFont(Tema.FONTE_LABEL_BOLD);
        lblMotivo.setForeground(Tema.CINZA_MEDIO.darker());
        painelMotivo.add(lblMotivo, BorderLayout.NORTH);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        txtMotivo.setFont(Tema.FONTE_LABEL);
        JScrollPane scrollMotivo = new JScrollPane(txtMotivo);
        scrollMotivo.setBorder(new Tema.BordaArredondada(8, Tema.CINZA_CLARO));
        painelMotivo.add(scrollMotivo, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(painelTopo, BorderLayout.NORTH);
        centro.add(painelMotivo, BorderLayout.CENTER);

        card.add(centro, BorderLayout.CENTER);
        return card;
    }

    private JPanel montarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        painel.setBackground(Tema.BRANCO);

        JButton btnRenovar = Tema.botaoPrimario("Renovar Plano");
        btnRenovar.addActionListener(e -> renovar());

        JButton btnCancelar = Tema.botaoPerigo("Cancelar Matrícula");
        btnCancelar.addActionListener(e -> cancelar());

        JButton btnFechar = Tema.botaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dispose());

        painel.add(btnRenovar);
        painel.add(btnCancelar);
        painel.add(btnFechar);
        return painel;
    }

    private void buscarAluno() {
        String termo = txtBusca.getText().trim();
        if (termo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o nome ou CPF do aluno.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        comboResultados.removeAllItems();
        try {
            List<Aluno> resultados = alunoDAO.buscarPorNomeOuCpf(termo);
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum aluno encontrado.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                limparInfo();
                return;
            }
            for (Aluno a : resultados) {
                comboResultados.addItem(a);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar aluno: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparInfo() {
        matriculaAtual = null;
        lblInfoMatricula.setText(" ");
        painelPilula.removeAll();
        painelPilula.revalidate();
        painelPilula.repaint();
        txtMotivo.setText("");
    }

    private void carregarMatricula() {
        Aluno aluno = (Aluno) comboResultados.getSelectedItem();
        if (aluno == null) {
            limparInfo();
            return;
        }
        try {
            matriculaAtual = matriculaDAO.buscarMatriculaMaisRecenteDoAluno(aluno.getCpf());
            painelPilula.removeAll();
            if (matriculaAtual == null) {
                lblInfoMatricula.setText("<html>Este aluno não possui nenhuma matrícula registrada.</html>");
                painelPilula.revalidate();
                painelPilula.repaint();
                return;
            }
            Plano plano = planoDAO.buscarPorCodigo(matriculaAtual.getCodigoPlano());
            String nomePlano = plano != null ? plano.getNome() : matriculaAtual.getCodigoPlano();

            String html = "<html>"
                    + "<b>Aluno:</b> " + aluno.getNome() + "<br>"
                    + "<b>Plano:</b> " + nomePlano + "<br>"
                    + "<b>Início:</b> " + matriculaAtual.getDataInicio().format(FORMATO_DATA) + "<br>"
                    + "<b>Vencimento:</b> " + matriculaAtual.getDataVencimento().format(FORMATO_DATA)
                    + "</html>";
            lblInfoMatricula.setText(html);

            JLabel pilula;
            if (Matricula.STATUS_CANCELADA.equals(matriculaAtual.getStatus())) {
                pilula = Tema.pilulaStatus("CANCELADA", Tema.StatusCor.NEUTRO);
            } else if (matriculaAtual.isVencidaHoje()) {
                pilula = Tema.pilulaStatus("VENCIDA", Tema.StatusCor.VERMELHO);
            } else {
                pilula = Tema.pilulaStatus("ATIVA", Tema.StatusCor.VERDE);
            }
            painelPilula.add(pilula);
            painelPilula.revalidate();
            painelPilula.repaint();

            txtMotivo.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar matrícula: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renovar() {
        if (matriculaAtual == null) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno com matrícula registrada.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Matricula.STATUS_CANCELADA.equals(matriculaAtual.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Esta matrícula foi cancelada. Para voltar a treinar, realize uma nova matrícula no menu 'Matrícula'.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Plano plano = planoDAO.buscarPorCodigo(matriculaAtual.getCodigoPlano());
            if (plano == null) {
                JOptionPane.showMessageDialog(this, "Plano vinculado não foi encontrado.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate baseCalculo = matriculaAtual.isVencidaHoje() ? LocalDate.now() : matriculaAtual.getDataVencimento();
            LocalDate novoVencimento = baseCalculo.plusDays(plano.getDuracaoDias());

            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Renovar o plano \"" + plano.getNome() + "\" até " + novoVencimento.format(FORMATO_DATA) + "?",
                    "Confirmar renovação", JOptionPane.YES_NO_OPTION);
            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }

            matriculaDAO.renovar(matriculaAtual.getId(), novoVencimento);
            JOptionPane.showMessageDialog(this, "Plano renovado com sucesso! Novo vencimento: "
                    + novoVencimento.format(FORMATO_DATA));
            carregarMatricula();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao renovar plano: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelar() {
        if (matriculaAtual == null) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno com matrícula registrada.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Matricula.STATUS_CANCELADA.equals(matriculaAtual.getStatus())) {
            JOptionPane.showMessageDialog(this, "Esta matrícula já está cancelada.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motivo = txtMotivo.getText().trim();
        if (motivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o motivo do cancelamento.",
                    "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja cancelar esta matrícula?\nEsta ação não pode ser desfeita.",
                "Confirmar cancelamento", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            matriculaDAO.cancelar(matriculaAtual.getId(), motivo);
            JOptionPane.showMessageDialog(this, "Matrícula cancelada com sucesso.");
            carregarMatricula();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao cancelar matrícula: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
