package academia.ui;

import academia.dao.MatriculaDAO;
import academia.util.Icones;
import academia.util.Tema;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;


public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("Sistema de Gestão de Academia - Planos e Treinos");
        setSize(880, 620);
        setMinimumSize(new Dimension(760, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.BRANCO);

        // Ao abrir o sistema, atualiza o status das matrículas vencidas (regra de negócio)
        try {
            new MatriculaDAO().atualizarStatusVencidas();
        } catch (SQLException e) {
            mostrarErroBanco(e);
        }

        add(montarTopo(), BorderLayout.NORTH);
        add(montarPainelBotoes(), BorderLayout.CENTER);
        add(montarRodape(), BorderLayout.SOUTH);
    }

    private JPanel montarTopo() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.PRETO_GRAFITE);
        topo.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel selo = new JLabel("\u25CF ACADEMIA");
        selo.setForeground(Tema.LARANJA);
        selo.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel titulo = new JLabel("Painel Principal");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(Tema.FONTE_TITULO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(selo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(titulo);

        topo.add(textos, BorderLayout.WEST);
        return topo;
    }

    private JPanel montarPainelBotoes() {
        JPanel painel = new JPanel(new GridLayout(4, 2, 18, 18));
        painel.setBackground(Tema.BRANCO);
        painel.setBorder(BorderFactory.createEmptyBorder(26, 30, 10, 30));

        painel.add(itemMenu(Icones.Tipo.ALUNO, "Cadastro de Alunos",
                "CPF, dados de contato e restrições", e -> new TelaCrudAluno(this).setVisible(true)));
        painel.add(itemMenu(Icones.Tipo.PLANO, "Cadastro de Planos",
                "Mensal, trimestral, valores e benefícios", e -> new TelaCrudPlano(this).setVisible(true)));
        painel.add(itemMenu(Icones.Tipo.MATRICULA, "Matrícula",
                "Vincular aluno a um plano", e -> new TelaMatricula(this).setVisible(true)));
        painel.add(itemMenu(Icones.Tipo.TREINO, "Treinos (Fichas A/B/C)",
                "Exercícios, metas e histórico", e -> new TelaTreino(this).setVisible(true)));
        painel.add(itemMenu(Icones.Tipo.CHECKIN, "Check-in / Presença",
                "Registrar presença diária", e -> new TelaCheckIn(this).setVisible(true)));
        painel.add(itemMenu(Icones.Tipo.RENOVACAO, "Renovação / Cancelamento",
                "Renovar plano ou cancelar matrícula", e -> new TelaRenovacaoCancelamento(this).setVisible(true)));
        painel.add(itemMenu(Icones.Tipo.RELATORIO, "Relatórios",
                "Indicadores e exportações", e -> new TelaRelatorios(this).setVisible(true)));
        painel.add(itemMenu(Icones.Tipo.SAIR, "Sair", "Encerrar o sistema", e -> System.exit(0)));

        return painel;
    }


    private JButton itemMenu(Icones.Tipo tipo, String titulo, String descricao, java.awt.event.ActionListener acao) {
        Tema.BotaoArredondado botao = new Tema.BotaoArredondado(
                "", Tema.PRETO_GRAFITE, Tema.CINZA_ESCURO, Color.WHITE);
        botao.setLayout(new BorderLayout(14, 0));
        botao.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel iconeLabel = new JLabel(Icones.get(tipo, Tema.LARANJA));
        botao.add(iconeLabel, BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(Tema.FONTE_BOTAO_GRANDE);

        JLabel lblDescricao = new JLabel(descricao);
        lblDescricao.setForeground(new Color(0xB8, 0xBA, 0xBF));
        lblDescricao.setFont(new Font("SansSerif", Font.PLAIN, 11));

        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(lblDescricao);

        botao.add(textos, BorderLayout.CENTER);
        botao.addActionListener(acao);
        return botao;
    }

    private JPanel montarRodape() {
        JPanel rodape = new JPanel();
        rodape.setBackground(Tema.BRANCO);
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        JLabel texto = new JLabel("Selecione um módulo para começar");
        texto.setForeground(Tema.CINZA_MEDIO);
        texto.setFont(Tema.FONTE_LABEL);
        rodape.add(texto);
        return rodape;
    }

    private void mostrarErroBanco(SQLException e) {
        JTextArea area = new JTextArea(e.getMessage());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Tema.FONTE_MONO);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(560, 260));
        JOptionPane.showMessageDialog(this, scroll,
                "Erro ao conectar com o banco de dados", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        Tema.aplicarGlobal();
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}
