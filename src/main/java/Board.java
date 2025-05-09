import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class Board extends JPanel implements ActionListener {

    private static final int BOARD_WIDTH_CELLS = 10;
    private static final int BOARD_HEIGHT_CELLS_VISIBLE = 20;
    private static final int HIDDEN_ROWS_ABOVE = 2; 
    private static final int TOTAL_BOARD_HEIGHT_CELLS = BOARD_HEIGHT_CELLS_VISIBLE + HIDDEN_ROWS_ABOVE;

    private final int CELL_SIZE;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;

    private int numLinesRemoved = 0;
    private int score = 0;


    private int curPieceX = 0;
    private int curPieceY = 0;

    private Shape currentPiece;
    private Shape nextPiece;


    private Color[][] grid;

    private JLabel statusBar;
    private Tetris parentFrame;

    public Board(Tetris parent) {
        this.parentFrame = parent;
        this.statusBar = parent.getStatusBar();
        this.CELL_SIZE = parent.getCellSize();
        initBoard();
    }

    private void initBoard() {
        setFocusable(true);
        addKeyListener(new TAdapter());
        setBackground(Color.BLACK); 

        grid = new Color[BOARD_WIDTH_CELLS][TOTAL_BOARD_HEIGHT_CELLS];
        clearBoardGrid();

        currentPiece = new Shape();
        nextPiece = new Shape();
        nextPiece.setPieceShape(Shape.PieceShape.getRandomShape()); 

        timer = new Timer(400, this); 
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
        if (!isStarted || isFallingFinished) return; 

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            statusBar.setText("Paused. Score: " + score);
        } else {
            timer.start();
            updateStatusBar(); 
        }
        repaint(); 
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


private void spawnNewPiece() {
    currentPiece.setPieceShape(nextPiece.getPieceShape());
    nextPiece.setPieceShape(Shape.PieceShape.getRandomShape());

    curPieceX = BOARD_WIDTH_CELLS / 2; 


    curPieceY = -currentPiece.getTopmostRelativeY();


    if (!tryMove(currentPiece, curPieceX, curPieceY)) {
        currentPiece.setPieceShape(Shape.PieceShape.NoShape);
        timer.stop();
        isStarted = false;
        isFallingFinished = true; 
        statusBar.setText("Game Over! Final Score: " + score + ". Press 'S' to Restart.");
    } else {
        isFallingFinished = false; 
    }
    updateStatusBar();
}



    private boolean tryMove(Shape pieceToTry, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int boardX = newX + pieceToTry.getX(i);
            int boardY = newY + pieceToTry.getY(i);

            if (boardX < 0 || boardX >= BOARD_WIDTH_CELLS || boardY < 0 || boardY >= TOTAL_BOARD_HEIGHT_CELLS) {
                return false;
            }
            if (grid[boardX][boardY] != null) { // null means empty
                return false;
            }
        }

        currentPiece = pieceToTry; 
        curPieceX = newX;
        curPieceY = newY;
        repaint();
        return true;
    }

    private void pieceLanded() {
        for (int i = 0; i < 4; i++) {
            int boardX = curPieceX + currentPiece.getX(i);
            int boardY = curPieceY + currentPiece.getY(i);
            if (boardX >= 0 && boardX < BOARD_WIDTH_CELLS && boardY >= 0 && boardY < TOTAL_BOARD_HEIGHT_CELLS) {
                grid[boardX][boardY] = currentPiece.getColor();
            }
        }

        removeFullLines();

        if (!isFallingFinished) { 
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
        for (int y = TOTAL_BOARD_HEIGHT_CELLS - 1; y >= 0; y--) { 
            boolean lineIsFull = true;
            for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
                if (grid[x][y] == null) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLinesInThisTurn++;
                for (int currentY = y; currentY > 0; currentY--) {
                    for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
                        grid[x][currentY] = grid[x][currentY - 1];
                    }
                }
                for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
                    grid[x][0] = null;
                }
                y++;
            }
        }

        if (numFullLinesInThisTurn > 0) {
            numLinesRemoved += numFullLinesInThisTurn;
            if (numFullLinesInThisTurn == 1) score += 100;
            else if (numFullLinesInThisTurn == 2) score += 300;
            else if (numFullLinesInThisTurn == 3) score += 500;
            else if (numFullLinesInThisTurn == 4) score += 800; 

            updateStatusBar();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGameArea(g); 
        drawLandedPieces(g);
        drawCurrentFallingPiece(g);
        drawNextPiecePreview(g);

        if (isPaused) {
            drawPauseScreen(g);
        }
        if (!isStarted && currentPiece.getPieceShape() == Shape.PieceShape.NoShape && score > 0) { 
            drawGameOverScreen(g);
        }
    }

    private void drawGameArea(Graphics g) {
        Dimension size = getSize();
        int boardPixelWidth = BOARD_WIDTH_CELLS * CELL_SIZE;
        int boardPixelHeight = BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE;

        g.setColor(getBackground()); 
        g.fillRect(0, 0, size.width, size.height);

        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_WIDTH_CELLS; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, boardPixelHeight);
        }
        for (int i = 0; i <= BOARD_HEIGHT_CELLS_VISIBLE; i++) { 
            g.drawLine(0, i * CELL_SIZE, boardPixelWidth, i * CELL_SIZE);
        }
    }


    private void drawLandedPieces(Graphics g) {
        for (int x = 0; x < BOARD_WIDTH_CELLS; x++) {
            for (int yGrid = HIDDEN_ROWS_ABOVE; yGrid < TOTAL_BOARD_HEIGHT_CELLS; yGrid++) {
                if (grid[x][yGrid] != null) {
                    int yScreen = yGrid - HIDDEN_ROWS_ABOVE; 
                    drawSquare(g, x * CELL_SIZE, yScreen * CELL_SIZE, grid[x][yGrid]);
                }
            }
        }
    }

    private void drawCurrentFallingPiece(Graphics g) {
        if (currentPiece.getPieceShape() != Shape.PieceShape.NoShape) {
            for (int i = 0; i < 4; i++) {
                int xGrid = curPieceX + currentPiece.getX(i);
                int yGrid = curPieceY + currentPiece.getY(i);

                if (yGrid >= HIDDEN_ROWS_ABOVE) {
                    int yScreen = yGrid - HIDDEN_ROWS_ABOVE;
                    drawSquare(g, xGrid * CELL_SIZE, yScreen * CELL_SIZE, currentPiece.getColor());
                }
            }
        }
    }

    private void drawNextPiecePreview(Graphics g) {
        if (nextPiece.getPieceShape() != Shape.PieceShape.NoShape && isStarted) {
            int previewAreaX = (BOARD_WIDTH_CELLS + 1) * CELL_SIZE;
            int previewAreaY = CELL_SIZE; 

            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Next:", previewAreaX, previewAreaY);

            int minPieceX = 0, maxPieceX = 0, minPieceY = 0, maxPieceY = 0;
            for(int i=0; i<4; i++){
                minPieceX = Math.min(minPieceX, nextPiece.getX(i));
                maxPieceX = Math.max(maxPieceX, nextPiece.getX(i));
                minPieceY = Math.min(minPieceY, nextPiece.getY(i));
                maxPieceY = Math.max(maxPieceY, nextPiece.getY(i));
            }
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

        g.setColor(color.brighter());
        g.drawLine(screenX, screenY + CELL_SIZE - 1, screenX, screenY); 
        g.drawLine(screenX, screenY, screenX + CELL_SIZE - 1, screenY); 

        g.setColor(color.darker());
        g.drawLine(screenX + 1, screenY + CELL_SIZE - 1, screenX + CELL_SIZE - 1, screenY + CELL_SIZE - 1); 
        g.drawLine(screenX + CELL_SIZE - 1, screenY + CELL_SIZE - 1, screenX + CELL_SIZE - 1, screenY + 1); 
    }

    private void drawPauseScreen(Graphics g) {
        g.setColor(new Color(50, 50, 50, 180)); 
        g.fillRect(0, 0, BOARD_WIDTH_CELLS * CELL_SIZE, BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Helvetica", Font.BOLD, 20));
        String msg = "PAUSED";
        FontMetrics fm = getFontMetrics(g.getFont());
        int msgWidth = fm.stringWidth(msg);
        g.drawString(msg, (BOARD_WIDTH_CELLS * CELL_SIZE - msgWidth) / 2, (BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE) / 2);
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(new Color(50, 50, 50, 200)); 
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
        g.drawString(scoreMsg, (BOARD_WIDTH_CELLS * CELL_SIZE - scoreMsgWidth) / 2 + 20, (BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE) / 2 + 10); 

        String restartMsg = "Press 'S' to Restart";
        int restartMsgWidth = fm.stringWidth(restartMsg);
        g.drawString(restartMsg, (BOARD_WIDTH_CELLS * CELL_SIZE - restartMsgWidth) / 2 + 15, (BOARD_HEIGHT_CELLS_VISIBLE * CELL_SIZE) / 2 + 40);
    }


    @Override
    public void actionPerformed(ActionEvent e) { 
        if (isFallingFinished) { 
            return; 
        }
        if (!isPaused && isStarted) {
            oneLineDown();
        }
        repaint(); 
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted) { 
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    start();
                }
                return;
            }

            if (currentPiece.getPieceShape() == Shape.PieceShape.NoShape && !isStarted) {
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    start(); 
                }
                return;
            }


            if (isPaused && e.getKeyCode() != KeyEvent.VK_P) { 
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
                case KeyEvent.VK_DOWN: 
                case KeyEvent.VK_S:
                    if (!isPaused) oneLineDown();
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    tryMove(currentPiece.rotateRight(), curPieceX, curPieceY);
                    break;
                case KeyEvent.VK_Z: 
                    tryMove(currentPiece.rotateLeft(), curPieceX, curPieceY);
                    break;
                case KeyEvent.VK_SPACE: 
                    dropDownHard();
                    break;
            }
        }
    }
}
