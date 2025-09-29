package model.chess;

import model.chess.pieces.*;
import java.util.*;

public class MoveValidator {
    private static final int BOARD_SIZE = 8;

    public static List<Position> getValidMoves(ChessPiece piece, ChessBoard board) {
        List<Position> validMoves = new ArrayList<>();
        List<Position> candidateMoves = piece.getValidMoves(board);

        for (Position move : candidateMoves) {
            Position from = piece.getPosition();
            ChessBoard clone = board.clone();
            clone.movePieceInternal(from, move);

            if (!clone.isInCheck(piece.isWhite())) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    public static boolean isValidMove(ChessBoard board, Position from, Position to) {
        if (!isPositionValid(from) || !isPositionValid(to)) return false;

        ChessPiece piece = board.getPiece(from);
        if (piece == null || piece.isWhite() != board.isWhiteTurn()) return false;
        if (!piece.isValidMove(to, board)) return false;

        // En Passant
        if (piece instanceof Pawn && to.equals(board.getEnPassantTarget())) {
            Position capture = new Position(from.getRow(), to.getCol());
            ChessPiece captured = board.getPiece(capture);
            if (captured instanceof Pawn && captured.isWhite() != piece.isWhite()) {
                return true;
            }
        }

        // Phong tốt
        if (piece instanceof Pawn) {
            if (to.getRow() == (piece.isWhite() ? 7 : 0)) {
                board.setPromotionPending(true);
            }
        }

        // Nhập thành
        if (piece instanceof King && Math.abs(from.getCol() - to.getCol()) == 2) {
            if (!canCastle(board, from, to)) return false;
        }

        // Clone để kiểm tra chiếu
        ChessBoard clone = board.clone();
        clone.movePieceInternal(from, to);

        return !clone.isInCheck(piece.isWhite());
    }

    private static boolean canCastle(ChessBoard board, Position from, Position to) {
        ChessPiece king = board.getPiece(from);
        if (!(king instanceof King) || king.hasMoved()) return false;

        int row = from.getRow();
        boolean isWhite = king.isWhite();

        if (to.getCol() == 6) { // kingside
            ChessPiece rook = board.getPiece(new Position(row, 7));
            if (!(rook instanceof Rook) || rook.hasMoved()) return false;
            if (!isPathClear(board, row, 5, 6)) return false;
        } else if (to.getCol() == 2) { // queenside
            ChessPiece rook = board.getPiece(new Position(row, 0));
            if (!(rook instanceof Rook) || rook.hasMoved()) return false;
            if (!isPathClear(board, row, 1, 3)) return false;
        } else {
            return false;
        }

        // Không được nhập thành nếu bị chiếu hoặc đi qua ô bị chiếu
        int step = to.getCol() > from.getCol() ? 1 : -1;
        Position intermediate1 = new Position(row, from.getCol() + step);
        Position intermediate2 = new Position(row, from.getCol() + 2 * step);

        ChessBoard clone1 = board.clone();
        clone1.movePieceInternal(from, intermediate1);
        if (clone1.isInCheck(isWhite)) return false;

        ChessBoard clone2 = board.clone();
        clone2.movePieceInternal(from, intermediate2);
        if (clone2.isInCheck(isWhite)) return false;

        return true;
    }

    private static boolean isPathClear(ChessBoard board, int row, int colStart, int colEnd) {
        for (int col = colStart; col <= colEnd; col++) {
            if (board.getPieceAt(row, col) != null) return false;
        }
        return true;
    }

    private static boolean isPositionValid(Position pos) {
        return pos.getRow() >= 0 && pos.getRow() < BOARD_SIZE &&
               pos.getCol() >= 0 && pos.getCol() < BOARD_SIZE;
    }

    public static boolean isCheckmate(ChessBoard board, boolean isWhite) {
        if (!board.isInCheck(isWhite)) return false;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position from = new Position(row, col);
                ChessPiece piece = board.getPieceAt(row, col);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (Position to : piece.getValidMoves(board)) {
                        ChessBoard clone = board.clone();
                        clone.movePieceInternal(from, to);
                        if (!clone.isInCheck(isWhite)) return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isStalemate(ChessBoard board, boolean isWhite) {
        if (board.isInCheck(isWhite)) return false;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board.getPieceAt(row, col);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (Position move : piece.getValidMoves(board)) {
                        ChessBoard clone = board.clone();
                        clone.movePieceInternal(new Position(row, col), move);
                        if (!clone.isInCheck(isWhite)) return false;
                    }
                }
            }
        }

        return true;
    }
}
