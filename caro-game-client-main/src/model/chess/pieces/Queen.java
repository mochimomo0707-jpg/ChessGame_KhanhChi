package model.chess.pieces;

import model.chess.ChessBoard;
import model.chess.pieces.ChessPiece;
import model.chess.Position;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class Queen extends ChessPiece {

    private static final int[][] QUEEN_DIRECTIONS = {
        {0, 1}, {1, 0}, {0, -1}, {-1, 0}, // rook
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // bishop
    };

    private static final ImageIcon WHITE_ICON = new ImageIcon(Queen.class.getResource("/Resources/queen_white.png"));
    private static final ImageIcon BLACK_ICON = new ImageIcon(Queen.class.getResource("/Resources/queen_black.png"));

    public Queen(boolean isWhite, int row, int col) {
        super(isWhite ? WHITE_ICON : BLACK_ICON, "queen", isWhite, row, col, false, false, new Position(row, col));
        setPosition(row, col);
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♕" : "♛";
    }

    @Override
    public Queen clone() {
        Queen clone = new Queen(isWhite(), getRow(), getCol());
        clone.setHasMoved(hasMoved());
        clone.setCaptured(isCaptured());
        clone.setPosition(getPosition());
        clone.setImage(getImage());
        return clone;
    }

    @Override
    public boolean isValidMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        if (!board.isValidPosition(fromRow, fromCol) || !board.isValidPosition(toRow, toCol))
            return false;

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (!(rowDiff == 0 || colDiff == 0 || rowDiff == colDiff))
            return false;

        int stepX = Integer.compare(toRow, fromRow);
        int stepY = Integer.compare(toCol, fromCol);

        int x = fromRow + stepX;
        int y = fromCol + stepY;

        while (x != toRow || y != toCol) {
            if (!board.isValidPosition(x, y)) return false;
            if (board.getPieceAt(x, y) != null)
                return false;
            x += stepX;
            y += stepY;
        }

        ChessPiece target = board.getPieceAt(toRow, toCol);
        return target == null || target.isWhite() != isWhite();
    }

    @Override
    public List<Position> calculateValidMoves(ChessBoard board) {
        List<Position> validMoves = new ArrayList<>();
        for (int[] dir : QUEEN_DIRECTIONS) {
            int row = getRow() + dir[0];
            int col = getCol() + dir[1];
            while (board.isValidPosition(row, col)) {
                ChessPiece target = board.getPieceAt(row, col);
                if (target == null) {
                    validMoves.add(new Position(row, col));
                } else {
                    if (target.isWhite() != isWhite())
                        validMoves.add(new Position(row, col));
                    break;
                }
                row += dir[0];
                col += dir[1];
            }
        }
        return validMoves;
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] dummy) {
        throw new UnsupportedOperationException("Not used");
    }
}
