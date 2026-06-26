package academia.ui;

import academia.dao.FichaTreinoDAO;
import academia.dao.MatriculaDAO;
import academia.dao.PresencaDAO;
import academia.model.FichaTreino;
import academia.model.Matricula;
import academia.model.Presenca;
import academia.util.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Tela de Relatórios: permite selecionar o tipo de relatório, aplicar filtros
 * (período ou quantidade de dias) e visualizar o resultado em uma tabela.
 * Visual temático com tabela de cabeçalho escuro e resumo destacado em laranja.
 */
public class TelaRelatorios extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String[] RELATORIOS = {
            "1. Planos Ativos e Vencendo (próximos X dias)",
            "2. Presenças por Período (lista)",
            "3. Alunos Inativos (sem check-in nos últimos X dias)",
            "4. Receita por Plano (somatório por período)",
            "5. Treinos por Aluno (fichas cadastradas)",
            "6. Cancelamentos por Período (motivos)",
            "7. Horários de Pico (distribuição por faixa horária)"
    };

    private final MatriculaDAO matriculaDAO = new MatriculaDAO();
    private final PresencaDAO presencaDAO = new PresencaDAO();
    private final FichaTreinoDAO fichaDAO = new FichaTreinoDAO();

    private final JComboBox<String> comboRelatorio = new JComboBox<>(RELATORIOS);

    private final JTextField txtDias = new JTextField("7", 5);
    private final JTextField txtDataInicio = new JTextField(LocalDate.now().minusDays(30).format(FORMATO_DATA), 10);
    private final JTextField txtDataFim = new JTextField(LocalDate.now().format(FORMATO_DATA), 10);
    private final JPanel painelFiltroDias = new JPanel();
    private final JPanel painelFiltroPeriodo = new JPanel();

    private final DefaultTableModel modeloTabela = new DefaultTableModel();
    private final JTable tabela = new JTable(modeloTabela);
    private final JLabel lblResumo = new JLabel(" ");

    public TelaRelatorios(JFrame pai) {
        super(pai, "Relatórios", true);
        setSize(960, 640);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.BRANCO);

        add(montarCabecalho(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setBackground(Tema.BRANCO);
        centro.setBorder(BorderFactory.createEmptyBorder(16, 24, 0, 24));
        centro.add(montarPainelTopo(), BorderLayout.NORTH);
        centro.add(montarPainelTabela(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        add(montarPainelInferior(), BorderLayout.SOUTH);

        comboRelatorio.addActionListener(e -> ajustarFiltrosVisiveis());
        ajustarFiltrosVisiveis();
    }

    private JPanel montarCabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Relatórios");
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

    private JPanel montarPainelTopo() {
        JPanel card = Tema.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel linhaTipo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaTipo.setOpaque(false);
        linhaTipo.add(rotulo("Tipo de relatório:"));
        linhaTipo.add(comboRelatorio);
        card.add(linhaTipo);

        painelFiltroDias.setLayout(new FlowLayout(FlowLayout.LEFT));
        painelFiltroDias.setOpaque(false);
        painelFiltroDias.add(rotulo("Quantidade de dias:"));
        painelFiltroDias.add(txtDias);
        card.add(painelFiltroDias);

        painelFiltroPeriodo.setLayout(new FlowLayout(FlowLayout.LEFT));
        painelFiltroPeriodo.setOpaque(false);
        painelFiltroPeriodo.add(rotulo("De (dd/MM/aaaa):"));
        painelFiltroPeriodo.add(txtDataInicio);
        painelFiltroPeriodo.add(rotulo("Até (dd/MM/aaaa):"));
        painelFiltroPeriodo.add(txtDataFim);
        card.add(painelFiltroPeriodo);

        return card;
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
        header.setForeground(Color.RED);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new Tema.BordaArredondada(10, Tema.CINZA_CLARO));
        return scroll;
    }

    private JPanel montarPainelInferior() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Tema.BRANCO);

        lblResumo.setBorder(BorderFactory.createEmptyBorder(8, 24, 4, 24));
        lblResumo.setFont(Tema.FONTE_LABEL_BOLD);
        lblResumo.setForeground(Tema.LARANJA_ESCURO);
        painel.add(lblResumo, BorderLayout.NORTH);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        painelBotoes.setBackground(Tema.BRANCO);
        JButton btnGerar = Tema.botaoPrimario("Gerar Relatório");
        btnGerar.addActionListener(e -> gerarRelatorio());
        JButton btnFechar = Tema.botaoSecundario("Fechar");
        btnFechar.addActionListener(e -> dispose());
        painelBotoes.add(btnGerar);
        painelBotoes.add(btnFechar);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        return painel;
    }

    private void ajustarFiltrosVisiveis() {
        int indice = comboRelatorio.getSelectedIndex();
        painelFiltroDias.setVisible(indice == 0 || indice == 2);
        painelFiltroPeriodo.setVisible(indice == 1 || indice == 3 || indice == 5);
    }

    private void gerarRelatorio() {
        int indice = comboRelatorio.getSelectedIndex();
        try {
            matriculaDAO.atualizarStatusVencidas();
            switch (indice) {
                case 0 -> relatorioPlanosAtivosEVencendo();
                case 1 -> relatorioPresencasPorPeriodo();
                case 2 -> relatorioAlunosInativos();
                case 3 -> relatorioReceitaPorPlano();
                case 4 -> relatorioTreinosPorAluno();
                case 5 -> relatorioCancelamentosPorPeriodo();
                case 6 -> relatorioHorariosDePico();
                default -> { }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantidade de dias inválida. Informe um número inteiro.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Data inválida. Utilize o formato dd/MM/aaaa.",
                    "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int lerDias() {
        return Integer.parseInt(txtDias.getText().trim());
    }

    private LocalDate lerDataInicio() {
        return LocalDate.parse(txtDataInicio.getText().trim(), FORMATO_DATA);
    }

    private LocalDate lerDataFim() {
        return LocalDate.parse(txtDataFim.getText().trim(), FORMATO_DATA);
    }

    private void relatorioPlanosAtivosEVencendo() throws SQLException {
        int dias = lerDias();
        List<Matricula> lista = matriculaDAO.relatorioPlanosAtivosEVencendo(dias);
        modeloTabela.setDataVector(new Object[0][0], new Object[]{
                "Aluno", "Plano", "Início", "Vencimento", "Dias Restantes"
        });
        for (Matricula m : lista) {
            modeloTabela.addRow(new Object[]{
                    m.getNomeAluno(),
                    m.getNomePlano(),
                    m.getDataInicio().format(FORMATO_DATA),
                    m.getDataVencimento().format(FORMATO_DATA),
                    java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), m.getDataVencimento())
            });
        }
        lblResumo.setText("Total de matrículas ativas vencendo em até " + dias + " dia(s): " + lista.size());
    }

    private void relatorioPresencasPorPeriodo() throws SQLException {
        LocalDate inicio = lerDataInicio();
        LocalDate fim = lerDataFim();
        List<Presenca> lista = presencaDAO.listarPorPeriodo(inicio, fim);
        modeloTabela.setDataVector(new Object[0][0], new Object[]{"Aluno", "Data/Hora do Check-in"});
        for (Presenca p : lista) {
            modeloTabela.addRow(new Object[]{p.getNomeAluno(), p.getDataHora().format(FORMATO_DATA_HORA)});
        }

        List<Object[]> fluxo = presencaDAO.fluxoDiarioPorPeriodo(inicio, fim);
        StringBuilder sb = new StringBuilder("<html>Total de presenças: " + lista.size() + " | Fluxo diário: ");
        for (Object[] linha : fluxo) {
            sb.append(linha[0]).append(" (").append(linha[1]).append(") &nbsp;&nbsp;");
        }
        sb.append("</html>");
        lblResumo.setText(sb.toString());
    }

    private void relatorioAlunosInativos() throws SQLException {
        int dias = lerDias();
        List<Object[]> lista = presencaDAO.alunosInativos(dias);
        modeloTabela.setDataVector(new Object[0][0], new Object[]{"CPF", "Nome", "Último Check-in"});
        for (Object[] linha : lista) {
            String ultimo = linha[2] != null
                    ? java.time.LocalDateTime.parse((String) linha[2]).format(FORMATO_DATA_HORA)
                    : "Nunca fez check-in";
            modeloTabela.addRow(new Object[]{linha[0], linha[1], ultimo});
        }
        lblResumo.setText("Total de alunos inativos (sem check-in nos últimos " + dias + " dias): " + lista.size());
    }

    private void relatorioReceitaPorPlano() throws SQLException {
        LocalDate inicio = lerDataInicio();
        LocalDate fim = lerDataFim();
        List<Object[]> lista = matriculaDAO.relatorioReceitaPorPlano(inicio, fim);
        modeloTabela.setDataVector(new Object[0][0], new Object[]{
                "Código", "Plano", "Quantidade de Matrículas", "Receita Total (R$)"
        });
        BigDecimal totalGeral = BigDecimal.ZERO;
        for (Object[] linha : lista) {
            modeloTabela.addRow(new Object[]{
                    linha[0], linha[1], linha[2],
                    String.format(new Locale("pt", "BR"), "%,.2f", (BigDecimal) linha[3])
            });
            totalGeral = totalGeral.add((BigDecimal) linha[3]);
        }
        lblResumo.setText("Receita total no período: R$ " + String.format(new Locale("pt", "BR"), "%,.2f", totalGeral));
    }

    private void relatorioTreinosPorAluno() throws SQLException {
        List<FichaTreino> lista = fichaDAO.listarTodasComNomeAluno();
        modeloTabela.setDataVector(new Object[0][0], new Object[]{
                "Aluno", "Ficha", "Última Atualização"
        });
        for (FichaTreino f : lista) {
            modeloTabela.addRow(new Object[]{
                    f.getNomeAluno(),
                    "Ficha " + f.getLetraFicha(),
                    f.getUltimaAtualizacao().format(FORMATO_DATA_HORA)
            });
        }
        lblResumo.setText("Total de fichas cadastradas no sistema: " + lista.size());
    }

    private void relatorioCancelamentosPorPeriodo() throws SQLException {
        LocalDate inicio = lerDataInicio();
        LocalDate fim = lerDataFim();
        List<Matricula> lista = matriculaDAO.relatorioCancelamentosPorPeriodo(inicio, fim);
        modeloTabela.setDataVector(new Object[0][0], new Object[]{
                "Aluno", "Plano", "Data Cancelamento", "Motivo"
        });
        for (Matricula m : lista) {
            modeloTabela.addRow(new Object[]{
                    m.getNomeAluno(),
                    m.getNomePlano(),
                    m.getDataCancelamento().format(FORMATO_DATA),
                    m.getMotivoCancelamento()
            });
        }
        lblResumo.setText("Total de cancelamentos no período: " + lista.size());
    }

    private void relatorioHorariosDePico() throws SQLException {
        List<Object[]> lista = presencaDAO.horariosDePico();
        modeloTabela.setDataVector(new Object[0][0], new Object[]{"Faixa Horária", "Quantidade de Check-ins"});
        int total = 0;
        String horarioPico = "-";
        int maiorQtd = -1;
        for (Object[] linha : lista) {
            int hora = (int) linha[0];
            int qtd = (int) linha[1];
            modeloTabela.addRow(new Object[]{String.format("%02d:00 - %02d:59", hora, hora), qtd});
            total += qtd;
            if (qtd > maiorQtd) {
                maiorQtd = qtd;
                horarioPico = String.format("%02d:00 - %02d:59", hora, hora);
            }
        }
        lblResumo.setText("Total de check-ins registrados: " + total + " | Horário de maior movimento: " + horarioPico);
    }
}
