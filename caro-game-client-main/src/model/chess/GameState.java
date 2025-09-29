package model.chess;

import java.util.HashMap;
import java.util.Map;
import model.chess.pieces.Bishop;
import model.chess.pieces.Knight;

public class GameState implements Cloneable {

    private boolean whiteTurn;
    private Position enPassantTarget;
    private Map<String, Integer> positionHistory;
    private boolean whiteCanCastleKingside = true;
    private boolean whiteCanCastleQueenside = true;
    private boolean blackCanCastleKingside = true;
    private boolean blackCanCastleQueenside = true;

    public GameState() {
        this.whiteTurn = true;
        this.positionHistory = new HashMap<>();
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public void switchTurn() {
        whiteTurn = !whiteTurn;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public void setEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }

    public void updatePositionHistory(String position) {
        positionHistory.put(position, positionHistory.getOrDefault(position, 0) + 1);
    }

    public boolean isThreefoldRepetition() {
        for (int count : positionHistory.values()) {
            if (count >= 3) {
                return true;
            }
        }
        return false;
    }

    public boolean isKingInCheck(ChessBoard board, boolean isWhiteTurn) {
        Position kingPos = board.findKing(isWhiteTurn);
        if (kingPos == null) {
            return false;
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                var piece = board.getPieceAt(row, col);
                if (piece != null && piece.isWhite() != isWhiteTurn) {
                    if (piece.canAttack(kingPos, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public GameState clone() {
        GameState cloned = new GameState();
        cloned.enPassantTarget = (enPassantTarget != null)
                ? new Position(enPassantTarget.getRow(), enPassantTarget.getCol())
                : null;
        cloned.positionHistory = new HashMap<>(this.positionHistory);
        cloned.whiteCanCastleKingside = this.whiteCanCastleKingside;
        cloned.whiteCanCastleQueenside = this.whiteCanCastleQueenside;
        cloned.blackCanCastleKingside = this.blackCanCastleKingside;
        cloned.blackCanCastleQueenside = this.blackCanCastleQueenside;
        cloned.whiteTurn = this.whiteTurn;
        return cloned;
    }
}
