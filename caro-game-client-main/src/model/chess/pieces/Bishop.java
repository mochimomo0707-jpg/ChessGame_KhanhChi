package model.chess.pieces;

import model.chess.ChessBoard;
import model.chess.pieces.ChessPiece;
import model.chess.Position;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class Bishop extends ChessPiece {

    private static final int[][] DIRECTIONS = {
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final ImageIcon WHITE_ICON = new ImageIcon(Bishop.class.getResource("/Resources/bishop_white.png"));
    private static final ImageIcon BLACK_ICON = new ImageIcon(Bishop.class.getResource("/Resources/bishop_black.png"));

    public Bishop(boolean isWhite, int row, int col) {
        super(isWhite ? WHITE_ICON : BLACK_ICON, "bishop", isWhite, row, col, false, false, new Position(row, col));
        setPosition(row, col);
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♗" : "♝";
    }

    @Override
    public Bishop clone() {
        Bishop clone = new Bishop(isWhite(), getRow(), getCol());
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

        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol))
            return false;

        int rowDir = Integer.compare(toRow, fromRow);
        int colDir = Integer.compare(toCol, fromCol);
        int steps = Math.abs(toRow - fromRow);

        for (int i = 1; i < steps; i++) {
            int r = fromRow + i * rowDir;
            int c = fromCol + i * colDir;
            if (!board.isValidPosition(r, c)) return false;
            if (board.getPieceAt(r, c) != null)
                return false;
        }

        ChessPiece target = board.getPieceAt(toRow, toCol);
        return target == null || target.isWhite() != isWhite();
    }

    @Override
    public List<Position> calculateValidMoves(ChessBoard board) {
        List<Position> validMoves = new ArrayList<>();
        for (int[] dir : DIRECTIONS) {
            int row = getRow() + dir[0];
            int col = getCol() + dir[1];
            while (board.isValidPosition(row, col)) {
                ChessPiece target = board.getPieceAt(row, col);
                if (target == null) {
                    validMoves.add(new Position(row, col));
                } else {
                    if (target.isWhite() != isWhite()) {
                        validMoves.add(new Position(row, col));
                    }
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
