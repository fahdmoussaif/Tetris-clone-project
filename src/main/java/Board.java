import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class Board extends JPanel implements ActionListener {

    // Enum for Tetromino shapes, their coordinates, and colors
    enum PieceShape {
        NoShape(new int[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}}, new Color(0, 0, 0)),
        ZShape(new int[][]{{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, new Color(204, 102, 102)),
        SShape(new int[][]{{0, -1}, {0, 0}, {1, 0}, {1, 1}}, new Color(102, 204, 102)),
        LineShape(new int[][]{{0, -1}, {0, 0}, {0, 1}, {0, 2}}, new Color(102, 102, 204)),
        TShape(new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, new Color(204, 204, 102)),
        SquareShape(new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}, new Color(204, 102, 204)),
        LShape(new int[][]{{-1, 1}, {0, 1}, {0, 0}, {0, -1}}, new Color(102, 204, 204)), // Adjusted L for pivot
        JShape(new int[][]{{1, 1}, {0, 1}, {0, 0}, {0, -1}}, new Color(204, 170, 102)); // Adjusted J for pivot

        public final int[][] coords; // Relative coordinates of the 4 blocks from pivot
        public final Color color;

        PieceShape(int[][] coords, Color color) {
            this.coords = coords;
            this.color = color;
        }

        public static PieceShape getRandomShape() {
            Random r = new Random();
            PieceShape[] values = PieceShape.values();
            return values[r.nextInt(values.length - 1) + 1]; // Exclude NoShape
        }
    }

    // Class representing the currently falling Tetromino
    static class Shape {
        protected PieceShape pieceShape;
        protected int[][] coords; // Current relative coordinates of the 4 blocks

        public Shape() {
            coords = new int[4][2];
            setPieceShape(PieceShape.NoShape);
        }


        public int getTopmostRelativeY() {
            int minY = coords[0][1];
            for (int i = 1; i < 4; i++) {
                if (coords[i][1] < minY) {
                    minY = coords[i][1];
                }
            }
            return minY;
        }


        public void setPieceShape(PieceShape shape) {
            this.pieceShape = shape;
            for (int i = 0; i < 4; i++) {
                System.arraycopy(shape.coords[i], 0, this.coords[i], 0, 2);
            }
        }

        public PieceShape getPieceShape() {
            return pieceShape;
        }

        public Color getColor() {
            return pieceShape.color;
        }

        public int getX(int index) {
            return coords[index][0];
        }

        public int getY(int index) {
            return coords[index][1];
        }

        public void setX(int index, int x) {
            coords[index][0] = x;
        }

        public void setY(int index, int y) {
            coords[index][1] = y;
        }

        // Rotation: returns a NEW Shape object with rotated coordinates
        public Shape rotateRight() {
            if (pieceShape == PieceShape.SquareShape) return this; // Square doesn't rotate

            Shape newShape = new Shape();
            newShape.setPieceShape(this.pieceShape); // Keep same type and color

            for (int i = 0; i < 4; i++) {
                newShape.setX(i, -getY(i)); // Clockwise: x' = -y
                newShape.setY(i, getX(i));   //             y' =  x
            }
            return newShape;
        }

        public Shape rotateLeft() {
            if (pieceShape == PieceShape.SquareShape) return this;

            Shape newShape = new Shape();
            newShape.setPieceShape(this.pieceShape);

            for (int i = 0; i < 4; i++) {
                newShape.setX(i, getY(i));   // Counter-clockwise: x' =  y
                newShape.setY(i, -getX(i));  //                    y' = -x
            }
            return newShape;
        }

        // Helper to find the lowest Y coordinate (max Y value) for spawning logic
        public int getLowestYCoord() {
            int maxY = coords[0][1];
            for (int i = 1; i < 4; i++) {
                if (coords[i][1] > maxY) {
                    maxY = coords[i][1];
                }
            }
            return maxY;
        }
    }

    private static final int BOARD_WIDTH_CELLS = 10;
    private static final int BOARD_HEIGHT_CELLS_VISIBLE = 20;
    private static final int HIDDEN_ROWS_ABOVE = 2; // For pieces to spawn and rotate
    private static final int TOTAL_BOARD_HEIGHT_CELLS = BOARD_HEIGHT_CELLS_VISIBLE + HIDDEN_ROWS_ABOVE;

    private final int CELL_SIZE;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;

    private int numLinesRemoved = 0;
    private int score = 0;

    // Current piece's pivot point position on the board grid
    private int curPieceX = 0;
    private int curPieceY = 0;

    private Shape currentPiece;
    private Shape nextPiece;

    // The game grid: stores the Color of landed pieces, null if empty
    private Color[][] grid;

    private JLabel statusBar;
    private Tetris parentFrame;

    public Board(Tetris parent) {
        this.parentFrame = parent;
        this.statusBar = parent.getStatusBar();
        // Calculate CELL_SIZE based on the preferred size passed from Tetris main frame
        this.CELL_SIZE = parent.getCellSize();
        initBoard();
    }

    private void initBoard() {
        setFocusable(true);
        addKeyListener(new TAdapter());
        setBackground(Color.BLACK); // Background for the game area

        grid = new Color[BOARD_WIDTH_CELLS][TOTAL_BOARD_HEIGHT_CELLS];
        clearBoardGrid();

        currentPiece = new Shape();
        nextPiece = new Shape();
        nextPiece.setPieceShape(PieceShape.getRandomShape()); // Pre-load the next piece

        timer = new Timer(400, this); // Game ticks every 400ms
    }

    public void start() {
        if (isPaused) return;

        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        score = 0;
        clearBoardGrid();
        spawnNewPiece();
        timer.start();
        updateStatusBar();
    }

    private void pause() {
        if (!isStarted || isFallingFinished) return; // Can't pause if not started or game over

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            statusBar.setText("Paused. Score: " + score);
        } else {
            timer.start();
            updateStatusBar(); // Restore normal status text
        }
        repaint(); // To draw/clear the pause message
    }

    private void updateStatusBar() {
        statusBar.setText("Score: " + score + " Lines: " + numLinesRemoved);
    }

    private void clearBoardGrid() {
        for (int i = 0; i < BOARD_WIDTH_CELLS; i++) {
            for (int j = 0; j < TOTAL_BOARD_HEIGHT_CELLS; j++) {
                grid[i][j] = null;
            }
        }
    }
/**
    private void spawnNewPiece() {
        currentPiece.setPieceShape(nextPiece.getPieceShape());
        nextPiece.setPieceShape(PieceShape.getRandomShape());

        curPieceX = BOARD_WIDTH_CELLS / 2;
        // Spawn so the lowest part of the piece is near the top of the hidden area
        curPieceY = HIDDEN_ROWS_ABOVE - 1 - currentPiece.getLowestYCoord();


        if (!tryMove(currentPiece, curPieceX, curPieceY)) {
            // Game Over Condition: New piece cannot be placed
            currentPiece.setPieceShape(PieceShape.NoShape); // Make current piece disappear
            timer.stop();
            isStarted = false;
            statusBar.setText("Game Over! Final Score: " + score + ". Press 'S' to Restart.");
        }
        updateStatusBar();
    }

 **/


private void spawnNewPiece() {
    currentPiece.setPieceShape(nextPiece.getPieceShape());
    nextPiece.setPieceShape(PieceShape.getRandomShape());

    curPieceX = BOARD_WIDTH_CELLS / 2; // Center horizontally

    // Adjust Y so the topmost block of the piece starts at board row 0 (top of hidden area)
    // or slightly below if pieces are defined with all positive Y and pivot at top-left.
    // Given our piece definitions (e.g. LineShape {{0,-1},..{0,2}}),
    // we want curPieceY + topmost_relative_Y = 0 (or a bit more for deeper spawn).
    // So, curPieceY = -currentPiece.getTopmostRelativeY().
    // This will place the part of the shape with the smallest Y coordinate at board row 0.
    curPieceY = -currentPiece.getTopmostRelativeY();

    // If we want to spawn it one row deeper into the hidden rows (if available):
    // curPieceY = -currentPiece.getTopmostRelativeY() + 1;
    // Ensure this doesn't conflict with HIDDEN_ROWS_ABOVE logic.
    // For now, -getTopmostRelativeY() is a good start.

    if (!tryMove(currentPiece, curPieceX, curPieceY)) {
        // Game Over Condition: New piece cannot be placed even with adjusted Y
        currentPiece.setPieceShape(PieceShape.NoShape);
        timer.stop();
        isStarted = false;
        isFallingFinished = true; // Indicate game is truly finished
        statusBar.setText("Game Over! Final Score: " + score + ". Press 'S' to Restart.");
    } else {
        isFallingFinished = false; // New piece spawned and can fall
    }
    updateStatusBar(); // Ensure status bar is updated even on game over
}



    private boolean tryMove(Shape pieceToTry, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int boardX = newX + pieceToTry.getX(i);
            int boardY = newY + pieceToTry.getY(i);

            // Boundary checks
            if (boardX < 0 || boardX >= BOARD_WIDTH_CELLS || boardY < 0 || boardY >= TOTAL_BOARD_HEIGHT_CELLS) {
                return false;
            }
            // Collision with existing pieces on the grid
            if (grid[boardX][boardY] != null) { // null means empty
                return false;
            }
        }

        // If all checks pass, update current piece and its position
        currentPiece = pieceToTry; // This is important if pieceToTry was a rotated copy
        curPieceX = newX;
        curPieceY = newY;
        repaint();
        return true;
    }

    private void pieceLanded() {
        // Add the blocks of the current piece to the grid
        for (int i = 0; i < 4; i++) {
            int boardX = curPieceX + currentPiece.getX(i);
            int boardY = curPieceY + currentPiece.getY(i);
            // Check bounds before placing, though tryMove should prevent out-of-bounds placements
            if (boardX >= 0 && boardX < BOARD_WIDTH_CELLS && boardY >= 0 && boardY < TOTAL_BOARD_HEIGHT_CELLS) {
                grid[boardX][boardY] = currentPiece.getColor();
            }
        }

        removeFullLines();

        if (!isFallingFinished) { // isFallingFinished might be set true by game over in removeFullLines
            spawnNewPiece();
        }
    }

    private void oneLineDown() {
        if (!tryMove(currentPiece, curPieceX, curPieceY + 1)) {
            pieceLanded();
        }
    }

    private void dropDownHard() {
        int newY = curPieceY;
        while (tryMove(currentPiece, curPieceX, newY + 1)) {
            newY++;
        }
        pieceLanded();
    }


    private void removeFullLines() {
        int numFullLinesInThisTurn = 0;
        for (int y = TOTAL_BOARD_HEIGHT_CELLS - 1; y >= 0; y--) { // Iterate from bottom up
            boolean lineIsFull = true;
            for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
                if (grid[x][y] == null) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLinesInThisTurn++;
                // Move all lines above this one, down by one row
                for (int currentY = y; currentY > 0; currentY--) {
                    for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
                        grid[x][currentY] = grid[x][currentY - 1];
                    }
                }
                // Clear the topmost line (it's now empty as everything shifted down)
                for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
                    grid[x][0] = null;
                }
                // Since a line was removed and lines shifted, re-check the current row 'y' again
                y++;
            }
        }

        if (numFullLinesInThisTurn > 0) {
            numLinesRemoved += numFullLinesInThisTurn;
            // Basic scoring: 100 for 1 line, 300 for 2, 500 for 3, 800 for 4 (Tetris)
            if (numFullLinesInThisTurn == 1) score += 100;
            else if (numFullLinesInThisTurn == 2) score += 300;
            else if (numFullLinesInThisTurn == 3) score += 500;
            else if (numFullLinesInThisTurn == 4) score += 800; // Tetris!

            updateStatusBar();
            // isFallingFinished = false; // Game continues after clearing lines
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGameArea(g); // Draw grid lines and background
        drawLandedPieces(g);
        drawCurrentFallingPiece(g);
        drawNextPiecePreview(g);

        if (isPaused) {
            drawPauseScreen(g);
        }
        if (!isStarted && currentPiece.getPieceShape() == PieceShape.NoShape && score > 0) { // Game over state
            drawGameOverScreen(g);
        }
    }

    private void drawGameArea(Graphics g) {
        Dimension size = getSize();
        int boardPixelWidth = BOARD_WIDTH_CELLS * CELL_SIZE;
        int boardPixelHeight = BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE;

        g.setColor(getBackground()); // Should be Color.BLACK as set in initBoard
        g.fillRect(0, 0, size.width, size.height);

        // Draw grid lines for the visible part of the board
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_WIDTH_CELLS; i++) { // Vertical lines
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, boardPixelHeight);
        }
        for (int i = 0; i <= BOARD_HEIGHT_CELLS_VISIBLE; i++) { // Horizontal lines
            g.drawLine(0, i * CELL_SIZE, boardPixelWidth, i * CELL_SIZE);
        }
    }


    private void drawLandedPieces(Graphics g) {
        for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
            // Only draw visible rows (j starts from HIDDEN_ROWS_ABOVE in grid coordinates)
            for (int yGrid = HIDDEN_ROWS_ABOVE; yGrid < TOTAL_BOARD_HEIGHT_CELLS; yGrid++) {
                if (grid[x][yGrid] != null) {
                    int yScreen = yGrid - HIDDEN_ROWS_ABOVE; // Convert grid Y to screen Y
                    drawSquare(g, x * CELL_SIZE, yScreen * CELL_SIZE, grid[x][yGrid]);
                }
            }
        }
    }

    private void drawCurrentFallingPiece(Graphics g) {
        if (currentPiece.getPieceShape() != PieceShape.NoShape) {
            for (int i = 0; i < 4; i++) {
                int xGrid = curPieceX + currentPiece.getX(i);
                int yGrid = curPieceY + currentPiece.getY(i);

                // Only draw if the block is within the visible part of the board
                if (yGrid >= HIDDEN_ROWS_ABOVE) {
                    int yScreen = yGrid - HIDDEN_ROWS_ABOVE;
                    drawSquare(g, xGrid * CELL_SIZE, yScreen * CELL_SIZE, currentPiece.getColor());
                }
            }
        }
    }

    private void drawNextPiecePreview(Graphics g) {
        if (nextPiece.getPieceShape() != PieceShape.NoShape && isStarted) {
            int previewAreaX = (BOARD_WIDTH_CELLS + 1) * CELL_SIZE;
            int previewAreaY = CELL_SIZE; // Start preview one cell down from top

            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Next:", previewAreaX, previewAreaY);

            // Find center of preview piece to align it
            int minPieceX = 0, maxPieceX = 0, minPieceY = 0, maxPieceY = 0;
            for(int i=0; i<4; i++){
                minPieceX = Math.min(minPieceX, nextPiece.getX(i));
                maxPieceX = Math.max(maxPieceX, nextPiece.getX(i));
                minPieceY = Math.min(minPieceY, nextPiece.getY(i));
                maxPieceY = Math.max(maxPieceY, nextPiece.getY(i));
            }
            // Offset to draw piece centered in a small 4x4 cell area
            int offsetX = previewAreaX + (2 - (minPieceX + maxPieceX)/2) * CELL_SIZE;
            int offsetY = previewAreaY + CELL_SIZE + (2 - (minPieceY + maxPieceY)/2) * CELL_SIZE;


            for (int i = 0; i < 4; i++) {
                int x = nextPiece.getX(i);
                int y = nextPiece.getY(i);
                drawSquare(g, offsetX + x * CELL_SIZE, offsetY + y * CELL_SIZE, nextPiece.getColor());
            }
        }
    }

    private void drawSquare(Graphics g, int screenX, int screenY, Color color) {
        g.setColor(color);
        g.fillRect(screenX + 1, screenY + 1, CELL_SIZE - 2, CELL_SIZE - 2);

        // Simple 3D effect
        g.setColor(color.brighter());
        g.drawLine(screenX, screenY + CELL_SIZE - 1, screenX, screenY); // Left edge
        g.drawLine(screenX, screenY, screenX + CELL_SIZE - 1, screenY); // Top edge

        g.setColor(color.darker());
        g.drawLine(screenX + 1, screenY + CELL_SIZE - 1, screenX + CELL_SIZE - 1, screenY + CELL_SIZE - 1); // Bottom edge
        g.drawLine(screenX + CELL_SIZE - 1, screenY + CELL_SIZE - 1, screenX + CELL_SIZE - 1, screenY + 1); // Right edge
    }

    private void drawPauseScreen(Graphics g) {
        g.setColor(new Color(50, 50, 50, 180)); // Semi-transparent dark overlay
        g.fillRect(0, 0, BOARD_WIDTH_CELLS * CELL_SIZE, BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Helvetica", Font.BOLD, 20));
        String msg = "PAUSED";
        FontMetrics fm = getFontMetrics(g.getFont());
        int msgWidth = fm.stringWidth(msg);
        g.drawString(msg, (BOARD_WIDTH_CELLS * CELL_SIZE - msgWidth) / 2, (BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE) / 2);
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(50, 50, 50, 200)); // Darker overlay for game over
        g.fillRect(0, 0, BOARD_WIDTH_CELLS * CELL_SIZE, BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE);
        g.setColor(Color.RED);
        g.setFont(new Font("Helvetica", Font.BOLD, 24));
        String msg = "GAME OVER";
        FontMetrics fm = getFontMetrics(g.getFont());
        int msgWidth = fm.stringWidth(msg);
        g.drawString(msg, (BOARD_WIDTH_CELLS * CELL_SIZE - msgWidth) / 2, (BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE) / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        String scoreMsg = "Final Score: " + score;
        int scoreMsgWidth = fm.stringWidth(scoreMsg);
        g.drawString(scoreMsg, (BOARD_WIDTH_CELLS * CELL_SIZE - scoreMsgWidth) / 2 + 20, (BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE) / 2 + 10); // Adjust font metrics for this string

        String restartMsg = "Press 'S' to Restart";
        int restartMsgWidth = fm.stringWidth(restartMsg);
        g.drawString(restartMsg, (BOARD_WIDTH_CELLS * CELL_SIZE - restartMsgWidth) / 2 + 15, (BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE) / 2 + 40);
    }


    @Override
    public void actionPerformed(ActionEvent e) { // Called by the Timer
        if (isFallingFinished) { // This flag is mostly for game over scenarios
            // isFallingFinished = false; // Handled by spawnNewPiece or game over logic
            // spawnNewPiece();
            return; // If game over, timer might still fire once.
        }
        if (!isPaused && isStarted) {
            oneLineDown();
        }
        repaint(); // Always repaint to reflect changes or no-changes (if paused)
    }

    // Adapter for keyboard input
    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted) { // If game hasn't started
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    start();
                }
                return;
            }

            // If game is over and currentPiece is NoShape, only allow 'S' to restart
            if (currentPiece.getPieceShape() == PieceShape.NoShape && !isStarted) {
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    start(); // Restart the game
                }
                return;
            }


            if (isPaused && e.getKeyCode() != KeyEvent.VK_P) { // If paused, only 'P' works
                return;
            }

            int keycode = e.getKeyCode();

            switch (keycode) {
                case KeyEvent.VK_P:
                    pause();
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    tryMove(currentPiece, curPieceX - 1, curPieceY);
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    tryMove(currentPiece, curPieceX + 1, curPieceY);
                    break;
                case KeyEvent.VK_DOWN: // Soft drop
                case KeyEvent.VK_S:
                    if (!isPaused) oneLineDown(); // Avoid soft drop making timer run faster essentially
                    break;
                case KeyEvent.VK_UP: // Rotate Right
                case KeyEvent.VK_W:
                    tryMove(currentPiece.rotateRight(), curPieceX, curPieceY);
                    break;
                case KeyEvent.VK_Z: // Rotate Left (alternative)
                    // case KeyEvent.VK_CONTROL: // Some use CTRL for rotate left
                    tryMove(currentPiece.rotateLeft(), curPieceX, curPieceY);
                    break;
                case KeyEvent.VK_SPACE: // Hard drop
                    dropDownHard();
                    break;
            }
        }
    }
}