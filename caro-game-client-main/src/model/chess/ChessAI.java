package model.chess;

import model.chess.pieces.*;

import java.util.List;

public class ChessAI {

    private static final long TIMEOUT_MS = 6000;
    private static long startTime;

    public static ChessBoard.Move findBestMove(ChessBoard board, int depth) {
        List<ChessBoard.Move> moves = board.getAllValidMoves(false);
        if (moves.isEmpty()) return null;

        startTime = System.currentTimeMillis();
        return minimaxRoot(board, depth, false);
    }

    private static ChessBoard.Move minimaxRoot(ChessBoard board, int depth, boolean isMaximizing) {
        List<ChessBoard.Move> moves = board.getAllValidMoves(isMaximizing);
        if (moves.isEmpty()) return null;

        ChessBoard.Move bestMove = null;
        int bestValue = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (ChessBoard.Move move : moves) {
            if (System.currentTimeMillis() - startTime >= TIMEOUT_MS) break;

            ChessBoard tempBoard = board.cloneWithoutValidation();
            ChessPiece piece = tempBoard.getPiece(move.from);
            if (piece instanceof King && tempBoard.isCastlingMove(move.from, move.to)) {
                tempBoard.doCastle(move.from, move.to);
            } else {
                tempBoard.movePiece(move.from, move.to);
            }

            int value = minimax(tempBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !isMaximizing);

            if (bestMove == null) {
                bestMove = move;
                bestValue = value;
            }

            if (isMaximizing && value > bestValue) {
                bestValue = value;
                bestMove = move;
            } else if (!isMaximizing && value < bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private static int minimax(ChessBoard board, int depth, int alpha, int beta, boolean isMaximizing) {
        if (System.currentTimeMillis() - startTime >= TIMEOUT_MS) {
            return board.evaluate();
        }

        if (depth == 0 || isTerminal(board)) {
            return evaluateWithCheck(board);
        }

        List<ChessBoard.Move> moves = board.getAllValidMoves(isMaximizing);
        if (moves.isEmpty()) {
            return evaluateWithCheck(board);
        }

        int bestValue = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (ChessBoard.Move move : moves) {
            if (System.currentTimeMillis() - startTime >= TIMEOUT_MS) break;

            ChessBoard tempBoard = board.cloneWithoutValidation();
            ChessPiece piece = tempBoard.getPiece(move.from);
            if (piece instanceof King && tempBoard.isCastlingMove(move.from, move.to)) {
                tempBoard.doCastle(move.from, move.to);
            } else {
                tempBoard.movePiece(move.from, move.to);
            }

            int value = minimax(tempBoard, depth - 1, alpha, beta, !isMaximizing);

            if (isMaximizing) {
                bestValue = Math.max(bestValue, value);
                alpha = Math.max(alpha, bestValue);
            } else {
                bestValue = Math.min(bestValue, value);
                beta = Math.min(beta, bestValue);
            }

            if (beta <= alpha) break;
        }

        return bestValue;
    }

    private static boolean isTerminal(ChessBoard board) {
        return board.isCheckmate(true) || board.isCheckmate(false)
                || board.isStalemate(true) || board.isStalemate(false)
                || board.isDrawBy50MoveRule() || board.isThreefoldRepetition();
    }

    private static int evaluateWithCheck(ChessBoard board) {
        int base = board.evaluate();
        if (board.isInCheck(true)) base -= 50;
        if (board.isInCheck(false)) base += 50;
        return base;
    }
}
