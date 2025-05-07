import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private JLabel statusBar;
    private Board board; // Will be initialized in initUI
    private Dimension boardPanelSize; // Stores the calculated pixel dimensions for the Board panel

    // Define board dimensions in cells consistently
    // These could also be public static final in Board and accessed via Board.BOARD_WIDTH_CELLS
    public static final int BOARD_WIDTH_IN_CELLS = 10;
    public static final int BOARD_HEIGHT_IN_CELLS_VISIBLE = 20;


    public Tetris() {
        initUI();
    }

    private void initUI() {
        statusBar = new JLabel("Score: 0 Lines: 0");
        add(statusBar, BorderLayout.SOUTH);

        // Define the desired target cell size (can be adjusted)
        int targetCellPixelSize = 20;

        // Calculate the board panel's pixel dimensions based on cell count and target cell size
        int boardPixelWidth = BOARD_WIDTH_IN_CELLS * targetCellPixelSize;
        int boardPixelHeight = BOARD_HEIGHT_IN_CELLS_VISIBLE * targetCellPixelSize;
        this.boardPanelSize = new Dimension(boardPixelWidth, boardPixelHeight); // Initialize boardPanelSize HERE

        // Now create the Board. The Board constructor will call getCellSize(),
        // which will use the 'this.boardPanelSize' that we just set.
        board = new Board(this);
        board.setPreferredSize(this.boardPanelSize); // Set the preferred size for the Board panel
        add(board, BorderLayout.CENTER);

        setTitle("Simple Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack(); // Pack frame around components

        setLocationRelativeTo(null); // Center the window
        setResizable(false);

        statusBar.setText("Press 'S' to Start. Arrows/WASD to move, UP/W/Z to rotate, SPACE to drop, P to pause.");
    }

    public JLabel getStatusBar() {
        return statusBar;
    }


    public int getCellSize() {
        if (this.boardPanelSize == null) {
            // This should not happen if initUI() is ordered correctly.
            // Fallback to a default if it does.
            System.err.println("Warning: boardPanelSize was null when getCellSize() was called. Using default cell size 20.");
            return 20;
        }
        // Calculate cell size based on the panel's height and the number of visible vertical cells.
        return (int) this.boardPanelSize.getHeight() / BOARD_HEIGHT_IN_CELLS_VISIBLE;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}