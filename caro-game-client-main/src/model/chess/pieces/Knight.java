package model.chess.pieces;

import model.chess.ChessBoard;
import model.chess.pieces.ChessPiece;
import model.chess.Position;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class Knight extends ChessPiece {

    private static final int[][] KNIGHT_MOVES = {
        {-2, -1}, {-2, 1},
        {-1, -2}, {-1, 2},
        {1, -2},  {1, 2},
        {2, -1},  {2, 1}
    };

    private static final ImageIcon WHITE_ICON = new ImageIcon(Knight.class.getResource("/Resources/knight_white.png"));
    private static final ImageIcon BLACK_ICON = new ImageIcon(Knight.class.getResource("/Resources/knight_black.png"));

    public Knight(boolean isWhite, int row, int col) {
        super(isWhite ? WHITE_ICON : BLACK_ICON, "knight", isWhite, row, col, false, false, new Position(row, col));
        setPosition(row, col);
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♘" : "♞";
    }

    @Override
    public Knight clone() {
        Knight clone = new Knight(isWhite(), getRow(), getCol());
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

        int dRow = Math.abs(toRow - fromRow);
        int dCol = Math.abs(toCol - fromCol);

        if ((dRow == 2 && dCol == 1) || (dRow == 1 && dCol == 2)) {
            ChessPiece target = board.getPieceAt(toRow, toCol);
            return target == null || target.isWhite() != isWhite();
        }
        return false;
    }

    @Override
    public List<Position> calculateValidMoves(ChessBoard board) {
        List<Position> validMoves = new ArrayList<>();
        for (int[] move : KNIGHT_MOVES) {
            int newRow = getRow() + move[0];
            int newCol = getCol() + move[1];
            if (board.isValidPosition(newRow, newCol)) {
                ChessPiece target = board.getPieceAt(newRow, newCol);
                if (target == null || target.isWhite() != isWhite()) {
                    validMoves.add(new Position(newRow, newCol));
                }
            }
        }
        return validMoves;
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] dummy) {
        throw new UnsupportedOperationException("Not used");
    }
}
