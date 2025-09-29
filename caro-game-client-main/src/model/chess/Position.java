package model.chess;

import java.util.Objects;

public class Position {

    private final int row;
    private final int col;

    public Position(int row, int col) {
        if (!isValidPosition(row, col)) {
            throw new IllegalArgumentException("Vị trí không hợp lệ trên bàn cờ. Vị trí phải nằm trong phạm vi 0-7.");
        }
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public static boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public boolean isInsideBoard() {
        return isValidPosition(this.row, this.col);
    }

    public static Position fromChessNotation(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Ký hiệu không hợp lệ.");
        }
        char file = Character.toLowerCase(notation.charAt(0));
        char rank = notation.charAt(1);

        int col = file - 'a'; // (a-h) -> (0-7)
        int row = rank - '1'; // (1-8) -> (0-7)

        return new Position(row, col);
    }

    public String toChessNotation() {
        char file = (char) (col + 'a'); // (0-7) -> (a-h)
        char rank = (char) (row + '1'); // (0-7) -> (1-8)
        return "" + file + rank;
    }

    public int[] directionTo(Position other) {
        int dx = Integer.compare(other.row, this.row);
        int dy = Integer.compare(other.col, this.col);
        return new int[]{dx, dy};
    }

    public int distance(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }

    public boolean isAdjacent(Position other) {
        int dRow = Math.abs(this.row - other.row);
        int dCol = Math.abs(this.col - other.col);
        return (dRow <= 1 && dCol <= 1) && !(dRow == 0 && dCol == 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position that = (Position) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
