import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private JLabel statusBar;
    private Board board; 
    private Dimension boardPanelSize; 

    public static final int BOARD_WIDTH_IN_CELLS = 10;
    public static final int BOARD_HEIGHT_IN_CELLS_VISIBLE = 20;


    public Tetris() {
        initUI();
    }

    private void initUI() {
        statusBar = new JLabel("Score: 0 Lines: 0");
        add(statusBar, BorderLayout.SOUTH);

        int targetCellPixelSize = 20;

        int boardPixelWidth = BOARD_WIDTH_IN_CELLS * targetCellPixelSize;
        int boardPixelHeight = BOARD_HEIGHT_IN_CELLS_VISIBLE * targetCellPixelSize;
        this.boardPanelSize = new Dimension(boardPixelWidth, boardPixelHeight); 

        board = new Board(this);
        board.setPreferredSize(this.boardPanelSize);
        add(board, BorderLayout.CENTER);

        setTitle("Simple Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack(); 

        setLocationRelativeTo(null); 
        setResizable(true); // Allow resizing

        statusBar.setText("Press 'S' to Start. Arrows/WASD to move, UP/W/Z to rotate, SPACE to drop, P to pause.");

        // Listen for resize events to repaint the board
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                board.updateCellSize(); // No arguments needed
                board.repaint();
            }
        });
    }

    public JLabel getStatusBar() {
        return statusBar;
    }


    public int getCellSize() {
        if (this.boardPanelSize == null) {
            System.err.println("Warning: boardPanelSize was null when getCellSize() was called. Using default cell size 20.");
            return 20;
        }
        return (int) this.boardPanelSize.getHeight() / BOARD_HEIGHT_IN_CELLS_VISIBLE;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}
