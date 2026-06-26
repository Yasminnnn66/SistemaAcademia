package academia.ui;

import academia.dao.AlunoDAO;
import academia.dao.FichaTreinoDAO;
import academia.model.Aluno;
import academia.model.FichaTreino;
import academia.model.HistoricoFicha;
import academia.util.CampoObrigatorioException;
import academia.util.Tema;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de Treinos: cadastro de fichas A/B/C com exercícios e metas (texto livre),
 * e visualização do histórico de alterações de cada ficha.
 * Visual temático com ícone de "haltere" no cabeçalho e cards organizados.
 */
public class TelaTreino extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] LETRAS_FICHA = {"A", "B", "C", "D", "E"};

    private final AlunoDAO alunoDAO = new AlunoDAO();
    private final FichaTreinoDAO fichaDAO = new FichaTreinoDAO();

    private final JComboBox<Aluno> comboAlunos = new JComboBox<>();
    private final JComboBox<String> comboFichas = new JComboBox<>();
    private final JComboBox<String> comboLetraNova = new JComboBox<>(LETRAS_FICHA);
    private final JTextArea txtExercicios = new JTextArea(8, 30);
    private final JTextArea txtMetas = new JTextArea(4, 30);
    private final JLabel lblUltimaAtualizacao = new JLabel(" ");

    private FichaTreino fichaCarregada;

    public TelaTreino(JFrame pai) {
        super(pai, "Montagem de Treino (Fichas A/B/C)", true);
        setSize(760, 640);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.BRANCO);

        add(montarCabecalho(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setBackground(Tema.BRANCO);
        centro.setBorder(BorderFactory.createEmptyBorder(16, 24, 0, 24));
        centro.add(montarPainelSuperior(), BorderLayout.NORTH);
        centro.add(montarPainelFormulario(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        add(montarPainelBotoes(), BorderLayout.SOUTH);

        carregarAlunos();
        comboAlunos.addActionListener(e -> carregarFichasDoAluno());
        comboFichas.addActionListener(e -> carregarFichaSelecionada());
    }

    private JPanel montarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Montagem de Treino");
        titulo.setFont(Tema.FONTE_SUBTITULO.deriveFont(18f));
        titulo.setForeground(Color.WHITE);
        topo.add(titulo, BorderLayout.WEST);
        return topo;
    }

    private JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.FONTE_LABEL);
        l.setForeground(Tema.CINZA_MEDIO.darker());
        return l;
    }

    private JPanel montarPainelSuperior() {
        JPanel card = Tema.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; card.add(rotulo("Aluno:"), c);
        c.gridx = 1; c.weightx = 1; card.add(comboAlunos, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; card.add(rotulo("Ficha existente:"), c);
        c.gridx = 1; c.weightx = 1; card.add(comboFichas, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; card.add(rotulo("Ou criar nova ficha (letra):"), c);
        c.gridx = 1; c.weightx = 1; card.add(comboLetraNova, c);

        return card;
    }

    private JPanel montarPainelFormulario() {
        JPanel card = Tema.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(Tema.tituloSecao("Ficha de Treino"));

        txtExercicios.setLineWrap(true);
        txtExercicios.setWrapStyleWord(true);
        txtMetas.setLineWrap(true);
        txtMetas.setWrapStyleWord(true);
        txtExercicios.setFont(Tema.FONTE_LABEL);
        txtMetas.setFont(Tema.FONTE_LABEL);

        JLabel lblExercicios = rotulo("Exercícios (um por linha):");
        lblExercicios.setFont(Tema.FONTE_LABEL_BOLD);
        card.add(lblExercicios);
        card.add(Box.createVerticalStrut(4));
        JScrollPane scrollExercicios = new JScrollPane(txtExercicios);
        scrollExercicios.setBorder(new Tema.BordaArredondada(8, Tema.CINZA_CLARO));
        card.add(scrollExercicios);

        card.add(Box.createVerticalStrut(12));
        JLabel lblMetas = rotulo("Metas:");
        lblMetas.setFont(Tema.FONTE_LABEL_BOLD);
        card.add(lblMetas);
        card.add(Box.createVerticalStrut(4));
        JScrollPane scrollMetas = new JScrollPane(txtMetas);
        scrollMetas.setBorder(new Tema.BordaArredondada(8, Tema.CINZA_CLARO));
        card.add(scrollMetas);

        card.add(Box.createVerticalStrut(8));
        lblUltimaAtualizacao.setForeground(Tema.LARANJA);
        lblUltimaAtualizacao.setFont(Tema.FONTE_LABEL_BOLD);
        card.add(lblUltimaAtualizacao);

        return card;
    }

    private JPanel montarPainelBotoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        painel.setBackground(Tema.BRANCO);

        JButton btnSalvar = Tema.botaoPrimario("Salvar Ficha");
        btnSalvar.addActionListener(e -> salvarFicha());

        JButton btnHistorico = Tema.botaoSecundario("Ver Histórico de Alterações");
        btnHistorico.addActionListener(e -> verHistorico());

        JButton btnExcluir = Tema.botaoPerigo("Excluir Ficha");
        btnExcluir.addActionListener(e -> excluirFicha());

        JButton btnFechar = Tema.botaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dispose());

        painel.add(btnSalvar);
        painel.add(btnHistorico);
        painel.add(btnExcluir);
        painel.add(btnFechar);
        return painel;
    }

    private void carregarAlunos() {
        try {
            List<Aluno> alunos = alunoDAO.listarTodos();
            for (Aluno a : alunos) {
                comboAlunos.addItem(a);
            }
            if (alunos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Não há alunos cadastrados. Cadastre um aluno antes de montar um treino.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                carregarFichasDoAluno();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar alunos: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarFichasDoAluno() {
        comboFichas.removeAllItems();
        comboFichas.addItem("(Nenhuma selecionada)");
        Aluno aluno = (Aluno) comboAlunos.getSelectedItem();
        if (aluno == null) {
            return;
        }
        try {
            List<FichaTreino> fichas = fichaDAO.listarPorAluno(aluno.getCpf());
            for (FichaTreino f : fichas) {
                comboFichas.addItem("Ficha " + f.getLetraFicha());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar fichas: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
        limparFormulario();
    }

    private void carregarFichaSelecionada() {
        Aluno aluno = (Aluno) comboAlunos.getSelectedItem();
        Object selecionado = comboFichas.getSelectedItem();
        if (aluno == null || selecionado == null || selecionado.toString().startsWith("(Nenhuma")) {
            limparFormulario();
            return;
        }
        String letra = selecionado.toString().replace("Ficha ", "").trim();
        try {
            List<FichaTreino> fichas = fichaDAO.listarPorAluno(aluno.getCpf());
            for (FichaTreino f : fichas) {
                if (f.getLetraFicha().equalsIgnoreCase(letra)) {
                    fichaCarregada = f;
                    txtExercicios.setText(f.getExercicios());
                    txtMetas.setText(f.getMetas());
                    lblUltimaAtualizacao.setText("Última atualização: "
                            + f.getUltimaAtualizacao().format(FORMATO_DATA_HORA));
                    return;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar ficha: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        fichaCarregada = null;
        txtExercicios.setText("");
        txtMetas.setText("");
        lblUltimaAtualizacao.setText(" ");
    }

    private void salvarFicha() {
        Aluno aluno = (Aluno) comboAlunos.getSelectedItem();
        if (aluno == null) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String exercicios = txtExercicios.getText().trim();
            String metas = txtMetas.getText().trim();
            if (exercicios.isEmpty()) {
                throw new CampoObrigatorioException("Informe ao menos um exercício.");
            }

            if (fichaCarregada != null) {
                fichaCarregada.setExercicios(exercicios);
                fichaCarregada.setMetas(metas);
                fichaDAO.atualizar(fichaCarregada);
                JOptionPane.showMessageDialog(this, "Ficha " + fichaCarregada.getLetraFicha()
                        + " atualizada com sucesso! Uma cópia foi salva no histórico.");
            } else {
                String letra = (String) comboLetraNova.getSelectedItem();
                List<FichaTreino> existentes = fichaDAO.listarPorAluno(aluno.getCpf());
                boolean jaExiste = existentes.stream().anyMatch(f -> f.getLetraFicha().equalsIgnoreCase(letra));
                if (jaExiste) {
                    JOptionPane.showMessageDialog(this,
                            "Este aluno já possui uma ficha " + letra + ". Selecione-a na lista para editar.",
                            "Ficha já existe", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                FichaTreino nova = new FichaTreino();
                nova.setCpfAluno(aluno.getCpf());
                nova.setLetraFicha(letra);
                nova.setExercicios(exercicios);
                nova.setMetas(metas);
                fichaDAO.inserir(nova);
                JOptionPane.showMessageDialog(this, "Ficha " + letra + " criada com sucesso!");
            }
            carregarFichasDoAluno();
        } catch (CampoObrigatorioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar ficha: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verHistorico() {
        if (fichaCarregada == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma ficha existente na lista para ver o histórico.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<HistoricoFicha> historico = fichaDAO.listarHistorico(fichaCarregada.getId());
            if (historico.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum histórico encontrado para esta ficha.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (HistoricoFicha h : historico) {
                sb.append("Alteração em: ").append(h.getDataAlteracao().format(FORMATO_DATA_HORA)).append("\n");
                sb.append("Exercícios: ").append(h.getExercicios()).append("\n");
                sb.append("Metas: ").append(h.getMetas()).append("\n");
                sb.append("------------------------------------------\n");
            }
            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(Tema.FONTE_LABEL);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(500, 400));
            JOptionPane.showMessageDialog(this, scroll,
                    "Histórico - Ficha " + fichaCarregada.getLetraFicha(), JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirFicha() {
        if (fichaCarregada == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma ficha existente na lista para excluir.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir a ficha " + fichaCarregada.getLetraFicha() + "?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            fichaDAO.excluir(fichaCarregada.getId());
            JOptionPane.showMessageDialog(this, "Ficha excluída com sucesso!");
            carregarFichasDoAluno();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir ficha: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
