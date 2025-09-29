package model.chess.pieces;

import model.chess.ChessBoard;
import model.chess.pieces.ChessPiece;
import model.chess.Position;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class Rook extends ChessPiece {

    private static final int[][] ROOK_DIRECTIONS = {
        {0, 1}, // phải
        {1, 0}, // xuống
        {0, -1}, // trái
        {-1, 0} // lên
    };

    private static final ImageIcon WHITE_ICON = new ImageIcon(Rook.class.getResource("/Resources/rook_white.png"));
    private static final ImageIcon BLACK_ICON = new ImageIcon(Rook.class.getResource("/Resources/rook_black.png"));

    public Rook(boolean isWhite, int row, int col) {
        super(isWhite ? WHITE_ICON : BLACK_ICON, "rook", isWhite, row, col, false, false, new Position(row, col));
        setPosition(row, col);
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♖" : "♜";
    }

    @Override
    public Rook clone() {
        Rook clone = new Rook(isWhite(), getRow(), getCol());
        clone.setHasMoved(hasMoved());
        clone.setCaptured(isCaptured());
        clone.setPosition(getPosition());
        clone.setImage(getImage());
        return clone;
    }

    @Override
    public boolean isValidMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        if (fromRow == toRow) {
            int step = (toCol > fromCol) ? 1 : -1;
            for (int col = fromCol + step; col != toCol; col += step) {
                if (board.getPieceAt(fromRow, col) != null) {
                    return false;
                }
            }
        } else {
            int step = (toRow > fromRow) ? 1 : -1;
            for (int row = fromRow + step; row != toRow; row += step) {
                if (board.getPieceAt(row, fromCol) != null) {
                    return false;
                }
            }
        }

        ChessPiece target = board.getPieceAt(toRow, toCol);
        return target == null || target.isWhite() != isWhite();
    }

    @Override
    public List<Position> calculateValidMoves(ChessBoard board) {
        List<Position> validMoves = new ArrayList<>();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            int x = getRow() + dir[0];
            int y = getCol() + dir[1];

            while (board.isValidPosition(x, y)) {
                ChessPiece target = board.getPieceAt(x, y);
                Position pos = new Position(x, y);
                if (target == null) {
                    validMoves.add(pos);
                } else {
                    if (target.isWhite() != isWhite()) {
                        validMoves.add(pos);
                    }
                    break;
                }
                x += dir[0];
                y += dir[1];
            }
        }
        return validMoves;
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] dummy) {
        throw new UnsupportedOperationException("Not used");
    }
}
