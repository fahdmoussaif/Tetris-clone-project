import java.awt.Color;
import java.util.Random;

public class Shape {

    public enum PieceShape {
        NoShape(new int[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}}, new Color(0, 0, 0)),
        ZShape(new int[][]{{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, new Color(204, 102, 102)),
        SShape(new int[][]{{0, -1}, {0, 0}, {1, 0}, {1, 1}}, new Color(102, 204, 102)),
        LineShape(new int[][]{{0, -1}, {0, 0}, {0, 1}, {0, 2}}, new Color(102, 102, 204)),
        TShape(new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, new Color(204, 204, 102)),
        SquareShape(new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}, new Color(204, 102, 204)),
        LShape(new int[][]{{-1, 1}, {0, 1}, {0, 0}, {0, -1}}, new Color(102, 204, 204)),
        JShape(new int[][]{{1, 1}, {0, 1}, {0, 0}, {0, -1}}, new Color(204, 170, 102));

        public final int[][] coords;
        public final Color color;

        PieceShape(int[][] coords, Color color) {
            this.coords = coords;
            this.color = color;
        }

        public static PieceShape getRandomShape() {
            Random r = new Random();
            PieceShape[] values = PieceShape.values();
            return values[r.nextInt(values.length - 1) + 1];
        }
    }

    protected PieceShape pieceShape;
    protected int[][] coords;

    public Shape() {
        coords = new int[4][2];
        setPieceShape(PieceShape.NoShape);
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

    public int getTopmostRelativeY() {
        int minY = coords[0][1];
        for (int i = 1; i < 4; i++) {
            if (coords[i][1] < minY) minY = coords[i][1];
        }
        return minY;
    }


    public Shape rotateRight() {
        if (pieceShape == PieceShape.SquareShape) return this;

        Shape newShape = new Shape();
        newShape.setPieceShape(this.pieceShape);

        for (int i = 0; i < 4; i++) {
            newShape.setX(i, -getY(i));
            newShape.setY(i, getX(i));
        }
        return newShape;
    }

    public Shape rotateLeft() {
        if (pieceShape == PieceShape.SquareShape) return this;

        Shape newShape = new Shape();
        newShape.setPieceShape(this.pieceShape);

        for (int i = 0; i < 4; i++) {
            newShape.setX(i, getY(i));
            newShape.setY(i, -getX(i));
        }
        return newShape;
    }
}

