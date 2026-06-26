package academia.ui;

import academia.dao.AlunoDAO;
import academia.dao.MatriculaDAO;
import academia.dao.PresencaDAO;
import academia.model.Aluno;
import academia.model.Matricula;
import academia.util.RegraNegocioException;
import academia.util.Tema;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de Check-in: localiza o aluno, registra a presença e bloqueia
 * o check-in caso o plano esteja vencido ou não exista matrícula ativa.
 */
public class TelaCheckIn extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AlunoDAO alunoDAO = new AlunoDAO();
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();
    private final PresencaDAO presencaDAO = new PresencaDAO();

    private final JTextField txtBusca = new JTextField(20);
    private final JComboBox<Aluno> comboResultados = new JComboBox<>();
    private final JPanel painelPilula = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private final JLabel lblUltimoCheckIn = new JLabel(" ", SwingConstants.CENTER);
    private final JButton btnCheckIn = Tema.botaoPrimario("Registrar Check-in");

    public TelaCheckIn(JFrame pai) {
        super(pai, "Check-in / Controle de Presença", true);
        setSize(620, 480);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.BRANCO);

        add(montarCabecalho(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setBackground(Tema.BRANCO);
        centro.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));
        centro.add(montarPainelBusca(), BorderLayout.NORTH);
        centro.add(montarPainelStatus(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        add(montarPainelBotoes(), BorderLayout.SOUTH);

        comboResultados.addActionListener(e -> atualizarSituacao());
    }

    private JPanel montarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Check-in / Presença");
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

    private JPanel montarPainelStatus() {
        JPanel card = Tema.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(Tema.tituloSecao("Situação do Aluno"));

        painelPilula.setOpaque(false);
        card.add(Box.createVerticalStrut(10));
        card.add(painelPilula);
        card.add(Box.createVerticalStrut(14));

        lblUltimoCheckIn.setFont(Tema.FONTE_LABEL);
        lblUltimoCheckIn.setForeground(Tema.CINZA_MEDIO.darker());
        lblUltimoCheckIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblUltimoCheckIn);

        return card;
    }

    private JPanel montarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        painel.setBackground(Tema.BRANCO);
        btnCheckIn.setEnabled(false);
        btnCheckIn.addActionListener(e -> registrarCheckIn());
        JButton btnFechar = Tema.botaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dispose());
        painel.add(btnCheckIn);
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
                limparSituacao();
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

    private void limparSituacao() {
        painelPilula.removeAll();
        painelPilula.revalidate();
        painelPilula.repaint();
        lblUltimoCheckIn.setText(" ");
        btnCheckIn.setEnabled(false);
    }

    private void atualizarSituacao() {
        Aluno aluno = (Aluno) comboResultados.getSelectedItem();
        if (aluno == null) {
            limparSituacao();
            return;
        }
        try {
            Matricula ativa = matriculaDAO.buscarMatriculaAtivaDoAluno(aluno.getCpf());
            painelPilula.removeAll();
            JLabel pilula;

            if (ativa == null) {
                pilula = Tema.pilulaStatus("BLOQUEADO — sem matrícula ativa", Tema.StatusCor.VERMELHO);
                btnCheckIn.setEnabled(false);
            } else if (ativa.isVencidaHoje()) {
                pilula = Tema.pilulaStatus(
                        "BLOQUEADO — plano vencido em " + ativa.getDataVencimento().format(FORMATO_DATA),
                        Tema.StatusCor.VERMELHO);
                btnCheckIn.setEnabled(false);
            } else {
                pilula = Tema.pilulaStatus(
                        "LIBERADO — plano válido até " + ativa.getDataVencimento().format(FORMATO_DATA),
                        Tema.StatusCor.VERDE);
                btnCheckIn.setEnabled(true);
            }
            pilula.setFont(Tema.FONTE_SUBTITULO);
            painelPilula.add(pilula);
            painelPilula.revalidate();
            painelPilula.repaint();

            var ultimo = presencaDAO.buscarUltimoCheckIn(aluno.getCpf());
            lblUltimoCheckIn.setText(ultimo != null
                    ? "Último check-in: " + ultimo.format(FORMATO_DATA_HORA)
                    : "Nenhum check-in registrado anteriormente");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao verificar situação do aluno: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarCheckIn() {
        Aluno aluno = (Aluno) comboResultados.getSelectedItem();
        if (aluno == null) {
            return;
        }
        try {
            Matricula ativa = matriculaDAO.buscarMatriculaAtivaDoAluno(aluno.getCpf());
            if (ativa == null || ativa.isVencidaHoje()) {
                JOptionPane.showMessageDialog(this,
                        "Check-in bloqueado: o plano deste aluno está vencido ou inexistente. "
                        + "Oriente o aluno a renovar o plano na recepção.",
                        "Check-in bloqueado", JOptionPane.WARNING_MESSAGE);
                atualizarSituacao();
                return;
            }
            presencaDAO.registrarCheckIn(aluno.getCpf());
            JOptionPane.showMessageDialog(this, "Check-in registrado com sucesso para " + aluno.getNome() + "!");
            atualizarSituacao();
        } catch (RegraNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar check-in: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
