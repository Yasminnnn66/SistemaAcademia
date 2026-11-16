package academia.util;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;


public final class Tema {

    // Paleta de cores
    public static final Color PRETO_GRAFITE   = new Color(0x1C, 0x1C, 0x1E);
    public static final Color CINZA_ESCURO     = new Color(0x2B, 0x2D, 0x31);
    public static final Color CINZA_MEDIO      = new Color(0x6B, 0x70, 0x78);
    public static final Color CINZA_CLARO      = new Color(0xE9, 0xEA, 0xEC);
    public static final Color BRANCO           = new Color(0xFA, 0xFA, 0xFA);

    public static final Color LARANJA          = new Color(0xFF, 0x6B, 0x00); // cor de destaque / energia
    public static final Color LARANJA_ESCURO   = new Color(0xE0, 0x5C, 0x00); // hover/pressed
    public static final Color LARANJA_CLARO    = new Color(0xFF, 0xE4, 0xCC); // fundos suaves

    public static final Color VERDE_ATIVO      = new Color(0x1F, 0xA9, 0x55); // status OK / liberado
    public static final Color VERDE_CLARO_BG   = new Color(0xE3, 0xF6, 0xE9);
    public static final Color VERMELHO_ALERTA  = new Color(0xD7, 0x33, 0x3B); // status bloqueado/vencido
    public static final Color VERMELHO_CLARO_BG = new Color(0xFC, 0xE5, 0xE6);
    public static final Color AMARELO_AVISO    = new Color(0xE0, 0xA0, 0x00);
    public static final Color AMARELO_CLARO_BG = new Color(0xFD, 0xF1, 0xD8);

    // ---------- Fontes ----------
    public static final Font FONTE_TITULO      = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONTE_SUBTITULO   = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONTE_LABEL       = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONTE_LABEL_BOLD  = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONTE_BOTAO       = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONTE_BOTAO_GRANDE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONTE_MONO        = new Font("Monospaced", Font.PLAIN, 13);

    private Tema() {
    }

    /** Aplica configurações globais de UI (cores padrão de tabelas, tooltips, etc.). */
    public static void aplicarGlobal() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // segue com o padrão do Swing
        }
        UIManager.put("ToolTip.background", PRETO_GRAFITE);
        UIManager.put("ToolTip.foreground", BRANCO);
        UIManager.put("OptionPane.messageFont", FONTE_LABEL);
        UIManager.put("OptionPane.buttonFont", FONTE_BOTAO);
    }

    // ---------- Fábricas de componentes ----------

    /** Botão grande de ação principal (laranja), usado no menu da tela principal. */
    public static JButton botaoMenu(String texto, Icon icone) {
        BotaoArredondado b = new BotaoArredondado(texto, PRETO_GRAFITE, CINZA_ESCURO, BRANCO);
        b.setFont(FONTE_BOTAO_GRANDE);
        b.setIcon(icone);
        b.setIconTextGap(12);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setPreferredSize(new Dimension(260, 64));
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return b;
    }

    /** Botão de ação primária (laranja vibrante) — usado para Salvar, Confirmar, Matricular etc. */
    public static JButton botaoPrimario(String texto) {
        BotaoArredondado b = new BotaoArredondado(texto, LARANJA, LARANJA_ESCURO, Color.WHITE);
        b.setFont(FONTE_BOTAO);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    /** Botão de ação secundária (neutro) — usado para Novo, Buscar, Fechar etc. */
    public static JButton botaoSecundario(String texto) {
        BotaoArredondado b = new BotaoArredondado(texto, CINZA_CLARO, new Color(0xDB, 0xDC, 0xDF), PRETO_GRAFITE);
        b.setFont(FONTE_BOTAO);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    /** Botão de ação de risco (vermelho)*/
    public static JButton botaoPerigo(String texto) {
        BotaoArredondado b = new BotaoArredondado(texto, VERMELHO_ALERTA, new Color(0xB8, 0x27, 0x2E), Color.WHITE);
        b.setFont(FONTE_BOTAO);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    /** Cria um rótulo de status (pílula colorida) — verde, vermelho ou amarelo. */
    public static JLabel pilulaStatus(String texto, StatusCor cor) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(FONTE_LABEL_BOLD);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        switch (cor) {
            case VERDE -> {
                label.setBackground(VERDE_CLARO_BG);
                label.setForeground(VERDE_ATIVO.darker());
            }
            case VERMELHO -> {
                label.setBackground(VERMELHO_CLARO_BG);
                label.setForeground(VERMELHO_ALERTA.darker());
            }
            case AMARELO -> {
                label.setBackground(AMARELO_CLARO_BG);
                label.setForeground(AMARELO_AVISO.darker());
            }
            default -> {
                label.setBackground(CINZA_CLARO);
                label.setForeground(PRETO_GRAFITE);
            }
        }
        return label;
    }

    public enum StatusCor { VERDE, VERMELHO, AMARELO, NEUTRO }

    /** Painel com cantos arredondados e fundo branco, usado como "card" de seção. */
    public static JPanel card() {
        JPanel painel = new JPanel();
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(14, CINZA_CLARO),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return painel;
    }

    public static JLabel tituloSecao(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_SUBTITULO);
        label.setForeground(PRETO_GRAFITE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }

    // Componentes internos

    public static class BordaArredondada extends AbstractBorder {
        private final int raio;
        private final Color cor;

        public BordaArredondada(int raio, Color cor) {
            this.raio = raio;
            this.cor = cor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new RoundRectangle2D.Double(x + 1, y + 1, w - 2, h - 2, raio, raio));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(raio / 3, raio / 3, raio / 3, raio / 3);
        }
    }

    public static class BotaoArredondado extends JButton {
        private final Color corBase;
        private final Color corHover;
        private boolean sobreMouse = false;

        public BotaoArredondado(String texto, Color corBase, Color corHover, Color corTexto) {
            super(texto);
            this.corBase = corBase;
            this.corHover = corHover;
            setForeground(corTexto);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    sobreMouse = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    sobreMouse = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? (sobreMouse ? corHover : corBase) : CINZA_MEDIO);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
