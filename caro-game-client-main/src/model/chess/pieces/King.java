package model.chess.pieces;

import model.chess.ChessBoard;
import model.chess.Position;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class King extends ChessPiece {

    private static final int[][] KING_MOVES = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1}, {0, 1},
        {1, -1}, {1, 0}, {1, 1}
    };

    private static final ImageIcon WHITE_ICON = new ImageIcon(King.class.getResource("/Resources/king_white.png"));
    private static final ImageIcon BLACK_ICON = new ImageIcon(King.class.getResource("/Resources/king_black.png"));

    public King(boolean isWhite, int row, int col) {
        super(isWhite ? WHITE_ICON : BLACK_ICON, "king", isWhite, row, col, false, false, new Position(row, col));
        setPosition(row, col);
    }

    @Override
    public String getSymbol() {
        return isWhite() ? "♔" : "♚";
    }

    @Override
    public King clone() {
        King clone = new King(isWhite(), getRow(), getCol());
        clone.setHasMoved(hasMoved());
        clone.setCaptured(isCaptured());
        clone.setPosition(getPosition());
        clone.setImage(getImage());
        return clone;
    }

    @Override
    public boolean isValidMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        if (!board.isValidPosition(fromRow, fromCol) || !board.isValidPosition(toRow, toCol)) {
            return false;
        }

        // Nếu là nước nhập thành, cho phép
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);
        if (board.isCastlingMove(from, to)) {
            return true;
        }

        // Nước đi thường: chỉ 1 ô
        int dRow = Math.abs(toRow - fromRow);
        int dCol = Math.abs(toCol - fromCol);
        if (dRow > 1 || dCol > 1) {
            return false;
        }

        ChessPiece target = board.getPieceAt(toRow, toCol);
        if (target != null && target.isWhite() == isWhite()) {
            return false;
        }

        // Mô phỏng xem có bị chiếu không
        ChessBoard cloneBoard = board.cloneWithoutValidation();
        cloneBoard.movePieceInternal(new Position(fromRow, fromCol), new Position(toRow, toCol));

        return !isInCheck(cloneBoard, toRow, toCol, isWhite());
    }

    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] dummy) {
        int dRow = Math.abs(toRow - fromRow);
        int dCol = Math.abs(toCol - fromCol);
        return dRow <= 1 && dCol <= 1;
    }

    @Override
    public List<Position> calculateValidMoves(ChessBoard board) {
        List<Position> validMoves = new ArrayList<>();

        // Nước đi cơ bản của vua
        for (int[] delta : KING_MOVES) {
            int newRow = getRow() + delta[0];
            int newCol = getCol() + delta[1];
            if (board.isValidPosition(newRow, newCol)) {
                ChessPiece target = board.getPieceAt(newRow, newCol);
                if (target == null || target.isWhite() != isWhite()) {
                    ChessBoard cloneBoard = board.cloneWithoutValidation();
                    cloneBoard.movePieceInternal(getPosition(), new Position(newRow, newCol));

                    if (!isInCheck(cloneBoard, newRow, newCol, isWhite())) {
                        validMoves.add(new Position(newRow, newCol));
                    }
                }
            }
        }

        // Nhập thành
        Position from = new Position(getRow(), getCol());
        Position kingSide = new Position(getRow(), 6);
        Position queenSide = new Position(getRow(), 2);
        if (board.isCastlingMove(from, kingSide)) {
            validMoves.add(kingSide);
        }
        if (board.isCastlingMove(from, queenSide)) {
            validMoves.add(queenSide);
        }

        return validMoves;
    }

    /**
     * Hàm kiểm tra xem sau khi vua di chuyển đến (kingRow, kingCol) sẽ bị chiếu
     * hay không.
     */
    private boolean isInCheck(ChessBoard board, int kingRow, int kingCol, boolean isWhite) {
        Position kingPos = new Position(kingRow, kingCol);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece enemy = board.getPieceAt(row, col);
                if (enemy != null && enemy.isWhite() != isWhite) {
                    if (enemy instanceof King) {
                        // Nếu vua địch đứng kề bên, coi như bị chiếu
                        if (kingPos.isAdjacent(new Position(row, col))) {
                            return true;
                        }
                    } else if (enemy.isValidMove(board, row, col, kingRow, kingCol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
