import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private Board board; // <-- Add this line
    private JLabel scoreLabel;
    private JLabel linesLabel;
    private NextPanel nextPanel;

    public static final int BOARD_WIDTH_IN_CELLS = 10;
    public static final int BOARD_HEIGHT_IN_CELLS_VISIBLE = 20;

    public Tetris() {
        initUI();
    }

    private void initUI() {
        // Use BorderLayout for the frame
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Board panel (will expand to fill center)
        board = new Board(this);
        board.setBackground(Color.BLACK);
        board.setMinimumSize(new Dimension(300, 600));
        add(board, BorderLayout.CENTER);

        // Side panel for next shape and score
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(new Color(30, 30, 30));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        sidePanel.setMinimumSize(new Dimension(180, 200));
        sidePanel.setPreferredSize(new Dimension(200, 400));

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 22));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        linesLabel = new JLabel("Lines: 0");
        linesLabel.setForeground(Color.WHITE);
        linesLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        linesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidePanel.add(scoreLabel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(linesLabel);
        sidePanel.add(Box.createVerticalStrut(40));

        nextPanel = new NextPanel();
        nextPanel.setPreferredSize(new Dimension(140, 140));
        nextPanel.setMinimumSize(new Dimension(100, 100));
        nextPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextPanel.setBackground(new Color(40, 40, 40));
        nextPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1), "Next", 0, 0, new Font("Arial", Font.BOLD, 14), Color.LIGHT_GRAY
        ));
        sidePanel.add(nextPanel);

        sidePanel.add(Box.createVerticalGlue());

        add(sidePanel, BorderLayout.EAST);

        setTitle("Simple Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setResizable(true);

        // Repaint board and next panel on resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                board.updateCellSize();
                board.repaint();
                nextPanel.repaint();
            }
        });
    }

    public void updateScoreAndLines(int score, int lines) {
        scoreLabel.setText("Score: " + score);
        linesLabel.setText("Lines: " + lines);
    }

    public NextPanel getNextPanel() {
        return nextPanel;
    }

    public int getCellSize() {
        // Not used anymore, cell size is computed in Board based on panel size
        return 0;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }

    // Panel for next shape preview
    public static class NextPanel extends JPanel {
        private Shape nextShape;

        public void setNextShape(Shape shape) {
            this.nextShape = shape;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (nextShape == null || nextShape.getPieceShape() == Shape.PieceShape.NoShape) return;

            int cell = Math.min(getWidth(), getHeight()) / 5;
            int offsetX = getWidth() / 2;
            int offsetY = getHeight() / 2;

            // Center the shape
            int minX = 0, maxX = 0, minY = 0, maxY = 0;
            for (int i = 0; i < 4; i++) {
                minX = Math.min(minX, nextShape.getX(i));
                maxX = Math.max(maxX, nextShape.getX(i));
                minY = Math.min(minY, nextShape.getY(i));
                maxY = Math.max(maxY, nextShape.getY(i));
            }
            int shapeWidth = (maxX - minX + 1) * cell;
            int shapeHeight = (maxY - minY + 1) * cell;
            int baseX = offsetX - shapeWidth / 2 - minX * cell;
            int baseY = offsetY - shapeHeight / 2 - minY * cell;

            for (int i = 0; i < 4; i++) {
                int x = nextShape.getX(i);
                int y = nextShape.getY(i);
                drawSquare(g, baseX + x * cell, baseY + y * cell, cell, nextShape.getColor());
            }
        }

        private void drawSquare(Graphics g, int x, int y, int size, Color color) {
            g.setColor(color);
            g.fillRect(x + 1, y + 1, size - 2, size - 2);
            g.setColor(color.brighter());
            g.drawLine(x, y + size - 1, x, y);
            g.drawLine(x, y, x + size - 1, y);
            g.setColor(color.darker());
            g.drawLine(x + 1, y + size - 1, x + size - 1, y + size - 1);
            g.drawLine(x + size - 1, y + size - 1, x + size - 1, y + 1);
        }
    }
}
