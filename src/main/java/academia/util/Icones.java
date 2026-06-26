package academia.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Gera ícones simples desenhados via Graphics2D (sem depender de arquivos de imagem externos),
 * usados nos botões do menu principal para reforçar a identidade visual de academia.
 */
public final class Icones {

    private Icones() {
    }

    public enum Tipo { ALUNO, PLANO, MATRICULA, TREINO, CHECKIN, RENOVACAO, RELATORIO, SAIR }

    public static Icon get(Tipo tipo, Color cor) {
        return new IconeDesenhado(tipo, cor);
    }

    private static class IconeDesenhado implements Icon {
        private final Tipo tipo;
        private final Color cor;
        private static final int TAMANHO = 28;

        IconeDesenhado(Tipo tipo, Color cor) {
            this.tipo = tipo;
            this.cor = cor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (tipo) {
                case ALUNO -> desenharAluno(g2);
                case PLANO -> desenharPlano(g2);
                case MATRICULA -> desenharMatricula(g2);
                case TREINO -> desenharTreino(g2);
                case CHECKIN -> desenharCheckin(g2);
                case RENOVACAO -> desenharRenovacao(g2);
                case RELATORIO -> desenharRelatorio(g2);
                case SAIR -> desenharSair(g2);
            }
            g2.dispose();
        }

        private void desenharAluno(Graphics2D g2) {
            g2.draw(new Ellipse2D.Double(9, 2, 10, 10));
            g2.draw(new java.awt.geom.Arc2D.Double(2, 14, 24, 16, 0, 180, java.awt.geom.Arc2D.OPEN));
        }

        private void desenharPlano(Graphics2D g2) {
            g2.draw(new RoundRectangle2D.Double(3, 5, 22, 18, 4, 4));
            g2.drawLine(3, 11, 25, 11);
            g2.drawLine(7, 16, 15, 16);
        }

        private void desenharMatricula(Graphics2D g2) {
            g2.draw(new RoundRectangle2D.Double(4, 3, 16, 22, 3, 3));
            g2.drawLine(8, 9, 16, 9);
            g2.drawLine(8, 14, 16, 14);
            g2.drawLine(20, 19, 25, 24);
            g2.drawLine(25, 19, 20, 24);
        }

        private void desenharTreino(Graphics2D g2) {
            // representação de um "haltere"
            g2.draw(new RoundRectangle2D.Double(1, 11, 6, 6, 2, 2));
            g2.draw(new RoundRectangle2D.Double(21, 11, 6, 6, 2, 2));
            g2.drawLine(7, 14, 21, 14);
            g2.draw(new RoundRectangle2D.Double(3, 9, 3, 10, 1, 1));
            g2.draw(new RoundRectangle2D.Double(22, 9, 3, 10, 1, 1));
        }

        private void desenharCheckin(Graphics2D g2) {
            g2.draw(new RoundRectangle2D.Double(3, 3, 22, 22, 5, 5));
            g2.drawLine(8, 14, 13, 19);
            g2.drawLine(13, 19, 21, 9);
        }

        private void desenharRenovacao(Graphics2D g2) {
            g2.draw(new java.awt.geom.Arc2D.Double(4, 4, 20, 20, 30, 280, java.awt.geom.Arc2D.OPEN));
            int[] px = {20, 25, 24};
            int[] py = {5, 7, 12};
            g2.drawPolyline(px, py, 3);
        }

        private void desenharRelatorio(Graphics2D g2) {
            g2.draw(new RoundRectangle2D.Double(3, 3, 22, 22, 4, 4));
            g2.drawLine(8, 19, 8, 13);
            g2.drawLine(14, 19, 14, 9);
            g2.drawLine(20, 19, 20, 15);
        }

        private void desenharSair(Graphics2D g2) {
            g2.draw(new RoundRectangle2D.Double(4, 4, 14, 20, 3, 3));
            g2.drawLine(13, 14, 25, 14);
            int[] px = {21, 25, 21};
            int[] py = {10, 14, 18};
            g2.drawPolyline(px, py, 3);
        }

        @Override
        public int getIconWidth() {
            return TAMANHO;
        }

        @Override
        public int getIconHeight() {
            return TAMANHO;
        }
    }
}
