package model.chess.pieces;

import model.chess.ChessBoard;
import model.chess.pieces.ChessPiece;
import model.chess.Position;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class Pawn extends ChessPiece {

    private static final ImageIcon WHITE_ICON = new ImageIcon(Pawn.class.getResource("/Resources/pawn_white.png"));
    private static final ImageIcon BLACK_ICON = new ImageIcon(Pawn.class.getResource("/Resources/pawn_black.png"));

    private boolean justMovedTwoSteps = false;

    public Pawn(boolean isWhite, int row, int col) {
        super(isWhite ? WHITE_ICON : BLACK_ICON, "pawn", isWhite, row, col, false, false, new Position(row, col));
        setPosition(row, col);
    }

    public boolean hasJustMovedTwoSteps() {
        return justMovedTwoSteps;
    }

    public void setJustMovedTwoSteps(boolean justMovedTwoSteps) {
        this.justMovedTwoSteps = justMovedTwoSteps;
    }

    @Override
    public List<Position> calculateValidMoves(ChessBoard board) {
        List<Position> validMoves = new ArrayList<>();
        int direction = isWhite() ? -1 : 1;
        int row = getRow();
        int col = getCol();

        int nextRow = row + direction;

        // Đi thẳng 1 bước
        if (board.isValidPosition(nextRow, col) && board.getPieceAt(nextRow, col) == null) {
            validMoves.add(new Position(nextRow, col));

            // Đi 2 bước nếu đang ở hàng đầu
            int startRow = isWhite() ? 6 : 1;
            int twoStepsRow = row + 2 * direction;
            if (row == startRow && board.isValidPosition(twoStepsRow, col)
                    && board.getPieceAt(twoStepsRow, col) == null) {
                validMoves.add(new Position(twoStepsRow, col));
            }
        }

        // Ăn chéo và En Passant
        for (int dCol = -1; dCol <= 1; dCol += 2) {
            int newCol = col + dCol;
            if (board.isValidPosition(nextRow, newCol)) {
                ChessPiece target = board.getPieceAt(nextRow, newCol);
                if (target != null && target.isWhite() != isWhite()) {
                    validMoves.add(new Position(nextRow, newCol));
                }

                // En passant
                Position enPassantTarget = board.getEnPassantTarget();
                if (enPassantTarget != null && enPassantTarget.getRow() == nextRow && enPassantTarget.getCol() == newCol) {
                    validMoves.add(new Position(nextRow, newCol));
                }
            }
        }

        return validMoves;
    }

    @Override
    public boolean isValidMove(Position to, ChessBoard board) {
        if (board == null) return false;
        return calculateValidMoves(board).contains(to);
    }

    @Override
    public boolean isValidMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        return isValidMove(new Position(toRow, toCol), board);
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] dummy) {
        int dRow = toRow - fromRow;
        int dCol = Math.abs(toCol - fromCol);
        int dir = isWhite() ? -1 : 1;

        // Đi thẳng
        if (dCol == 0) {
            if (dRow == dir || (fromRow == (isWhite() ? 6 : 1) && dRow == 2 * dir)) {
                return true;
            }
        }

        // Ăn chéo
        if (dCol == 1 && dRow == dir) {
            return true;
        }

        return false;
    }

    public boolean canPromote() {
        return (isWhite() && getRow() == 0) || (!isWhite() && getRow() == 7);
    }

    public ChessPiece promoteToQueen() {
        return new Queen(isWhite(), getRow(), getCol());
    }

    @Override
    public void setPosition(int row, int col) {
        int oldRow = getRow();
        super.setPosition(row, col);

        // Kiểm tra có phải vừa đi 2 bước không
        int diff = Math.abs(row - oldRow);
        this.justMovedTwoSteps = (oldRow == (isWhite() ? 6 : 1)) && (diff == 2);
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♙" : "♟";
    }

    @Override
    public Pawn clone() {
        Pawn clone = new Pawn(isWhite(), getRow(), getCol());
        clone.setHasMoved(hasMoved());
        clone.setJustMovedTwoSteps(this.justMovedTwoSteps);
        clone.setCaptured(isCaptured());
        clone.setPosition(getPosition());
        clone.setImage(getImage());
        return clone;
    }
}
