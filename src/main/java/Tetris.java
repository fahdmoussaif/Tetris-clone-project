import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private Board board;
    private JLabel scoreLabel;
    private JLabel linesLabel;
    private NextPanel nextPanel;

    public static final int BOARD_WIDTH_IN_CELLS = 10;
    public static final int BOARD_HEIGHT_IN_CELLS_VISIBLE = 20;

    public Tetris() {
        initUI();
    }

    private void initUI() {
        // Use a panel with GridBagLayout to tightly pack board, instructions, and side panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        // Board panel
        board = new Board(this);
        board.setBackground(Color.BLACK);
        board.setMinimumSize(new Dimension(300, 600));
        board.setPreferredSize(new Dimension(480, 960));

        // Instructions panel (middle)
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBackground(new Color(25, 25, 25));
        instructionsPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        instructionsPanel.setMinimumSize(new Dimension(250, 200));
        instructionsPanel.setPreferredSize(new Dimension(300, 400));

        JLabel title = new JLabel("How to Play");
        title.setForeground(Color.ORANGE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea instructions = new JTextArea(
            """
            Press 'S' to Start or Restart

            Controls:
            - Left/Right Arrow or A/D: Move
            - Down Arrow or S: Soft Drop
            - Up Arrow or W: Rotate Right
            - Z: Rotate Left
            - Space: Hard Drop
            - P: Pause/Resume

            Clear lines for points!
            """
        );
        instructions.setEditable(false);
        instructions.setFocusable(false);
        instructions.setOpaque(false);
        instructions.setForeground(Color.WHITE);
        instructions.setFont(new Font("Arial", Font.PLAIN, 16));
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);

        instructionsPanel.add(title);
        instructionsPanel.add(Box.createVerticalStrut(20));
        instructionsPanel.add(instructions);
        instructionsPanel.add(Box.createVerticalGlue());

        // Side panel for next shape and score (right)
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

        // Layout constraints for board, instructions, and side panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // Board: take as much space as possible (left)
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(board, gbc);

        // Instructions panel (middle)
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(instructionsPanel, gbc);

        // Side panel: fixed width, fill vertically (right)
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(sidePanel, gbc);

        setContentPane(mainPanel);

        setTitle("Simple Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setResizable(true);

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
        return 0;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }

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
