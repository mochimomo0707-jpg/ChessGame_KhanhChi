package model.chess;

import model.chess.pieces.*;
import java.util.*;
import javax.swing.*;

public class ChessBoard {

    private ChessPiece[][] board;
    private boolean isWhiteTurn;
    private Position enPassantTarget;
    private int halfMoveClock;
    private Move lastMove;
    private Map<String, Integer> positionHistory = new HashMap<>();
    private GameState gameState;
    private boolean promotionPending = false;

    public ChessBoard(boolean whiteStarts) {
        board = new ChessPiece[8][8];
        initializePieces();
        isWhiteTurn = whiteStarts;
        halfMoveClock = 0;
        this.gameState = new GameState();
    }

    private ChessBoard(boolean isWhiteTurn, boolean skipInitialization) {
        this.board = new ChessPiece[8][8];
        this.isWhiteTurn = isWhiteTurn;
        if (!skipInitialization) {
            initializePieces();
        }
    }

    public ChessBoard() {
        this(true); // mặc định bên trắng đi trước
    }

    public Move getLastMove() {
        return lastMove;
    }

    public void setLastMove(Move lastMove) {
        this.lastMove = lastMove;
    }

    public boolean isPromotionPending() {
        return promotionPending;
    }

    public void setPromotionPending(boolean promotionPending) {
        this.promotionPending = promotionPending;
    }

    public List<Position> getValidMovesFor(int row, int col) {
        ChessPiece piece = getPieceAt(row, col);
        if (piece != null && piece.isWhite() == isWhiteTurn) {
            return piece.getLegalMoves(this, gameState);
        }
        return Collections.emptyList();
    }

    public List<Position> getLegalMoves(int row, int col) {
        return getValidMovesFor(row, col);
    }

    public List<Position> getValidMovesFor(int[] from) {
        return getValidMovesFor(from[0], from[1]);
    }

    public void showPromotionDialog(Position pos) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Chọn quân để phong tốt:",
                "Phong Tốt",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        boolean isWhite = getPiece(pos).isWhite();
        ChessPiece newPiece = switch (choice) {
            case 1 ->
                new Rook(isWhite, pos.getRow(), pos.getCol());
            case 2 ->
                new Bishop(isWhite, pos.getRow(), pos.getCol());
            case 3 ->
                new Knight(isWhite, pos.getRow(), pos.getCol());
            default ->
                new Queen(isWhite, pos.getRow(), pos.getCol());
        };
        promotePawn(pos, newPiece);
        setPromotionPending(false);
    }

    public ChessPiece createPromotedPiece(String type, boolean isWhite, int row, int col) {
        return switch (type) {
            case "Rook" ->
                new Rook(isWhite, row, col);
            case "Bishop" ->
                new Bishop(isWhite, row, col);
            case "Knight" ->
                new Knight(isWhite, row, col);
            default ->
                new Queen(isWhite, row, col);
        };
    }

    public GameState getGameState() {
        return gameState;
    }

    public void resetJustMovedTwoStepsExcept(ChessPiece excludePawn) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece p = board[row][col];
                if (p instanceof Pawn && p != excludePawn) {
                    ((Pawn) p).setJustMovedTwoSteps(false);
                }
            }
        }
    }

    public void movePieceUnsafe(Position from, Position to) {
        ChessPiece piece = getPiece(from);
        if (piece != null) {
            board[to.getRow()][to.getCol()] = piece;
            board[from.getRow()][from.getCol()] = null;
            piece.setPosition(to.getRow(), to.getCol());
        }
    }

    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);
        ChessPiece piece = getPiece(from);
        ChessPiece captured = getPiece(to);

        // Nếu không có quân hoặc không đúng lượt hoặc nước đi không hợp lệ
        if (piece == null || piece.isWhite() != isWhiteTurn || !isValidMove(from, to)) {
            return false;
        }

        lastMove = new Move(from, to, piece, captured, enPassantTarget, halfMoveClock);

        // Xử lý nhập thành (castling)
        if (piece instanceof King && isCastlingMove(from, to)) {
            doCastle(from, to);
            piece.setHasMoved(true);
        } // Bắt tốt qua đường (en passant)
        else if (piece instanceof Pawn && to.equals(enPassantTarget)) {
            board[toRow + (isWhiteTurn ? 1 : -1)][toCol] = null;
            movePieceInternal(from, to);
            piece.setHasMoved(true);
        } // Nước đi bình thường
        else {
            movePieceInternal(from, to);
            piece.setHasMoved(true);
        }

        // Kiểm tra phong tốt
        if (piece instanceof Pawn && (toRow == 0 || toRow == 7)) {
            promotionPending = true;
        }

        // Xử lý en passant
        if (piece instanceof Pawn) {
            Pawn movingPawn = (Pawn) piece;
            if (Math.abs(toRow - fromRow) == 2) {
                enPassantTarget = new Position((fromRow + toRow) / 2, fromCol);
                movingPawn.setJustMovedTwoSteps(true);
            } else {
                enPassantTarget = null;
                movingPawn.setJustMovedTwoSteps(false);
            }
            resetJustMovedTwoStepsExcept(movingPawn);
        } else {
            enPassantTarget = null;
            resetJustMovedTwoStepsExcept(null);
        }

        // Cập nhật đồng hồ bán nước đi
        halfMoveClock = (captured != null || piece instanceof Pawn) ? 0 : halfMoveClock + 1;

        // Đổi lượt
        isWhiteTurn = !isWhiteTurn;
        updatePositionHistory();
        return true;
    }

    public boolean isValidMove(Position from, Position to) {
        ChessPiece piece = getPiece(from);

        // Kiểm tra nếu không có quân cờ ở vị trí xuất phát
        if (piece == null) {
            return false;
        }

        // Kiểm tra lượt đi của quân cờ
        if (piece.isWhite() != isWhiteTurn) {
            return false;
        }

        // Sử dụng hàm isValidMove của từng quân
        return piece.isValidMove(this, from.getRow(), from.getCol(), to.getRow(), to.getCol());
    }

    public void movePieceInternal(Position from, Position to) {
        ChessPiece piece = getPiece(from);
        board[to.getRow()][to.getCol()] = piece;
        board[from.getRow()][from.getCol()] = null;
        piece.setPosition(to.getRow(), to.getCol());
        // KHÔNG gọi piece.setHasMoved(true) ở đây
    }

    public void doCastle(Position kingFrom, Position kingTo) {

        int row = kingFrom.getRow();
        int fromCol = kingFrom.getCol();
        int toCol = kingTo.getCol();
        int dir = toCol - fromCol;

        // Di chuyển vua
        movePieceInternal(kingFrom, kingTo);

        // Di chuyển xe theo hướng vua
        if (dir > 0) { // Nhập thành bên vua
            Position rookFrom = new Position(row, 7);
            Position rookTo = new Position(row, 5);
            movePieceInternal(rookFrom, rookTo);
        } else { // Nhập thành bên hậu
            Position rookFrom = new Position(row, 0);
            Position rookTo = new Position(row, 3);
            movePieceInternal(rookFrom, rookTo);
        }
    }

    private boolean wouldBeInCheckAfterMove(Position from, Position to) {
        ChessBoard simulated = this.clone();
        simulated.movePieceInternal(from, to);
        return simulated.isInCheck(isWhiteTurn);
    }

    public boolean isEnPassantMove(Position from, Position to) {
        ChessPiece piece = getPiece(from);
        return piece instanceof Pawn && to.equals(enPassantTarget);
    }

    public void performEnPassant(Position from, Position to) {
        ChessPiece pawn = getPiece(from);
        if (!(pawn instanceof Pawn)) {
            return;
        }
        board[from.getRow()][from.getCol()] = null;
        int dir = pawn.isWhite() ? -1 : 1;
        board[to.getRow()][to.getCol()] = pawn;
        board[to.getRow() + dir][to.getCol()] = null;
        pawn.setPosition(to.getRow(), to.getCol());
        pawn.setHasMoved(true);
        isWhiteTurn = !isWhiteTurn;
    }

    public boolean shouldPromotePawn(Position pos) {
        ChessPiece piece = getPiece(pos);
        return piece instanceof Pawn && ((Pawn) piece).canPromote();
    }

    public void promotePawn(Position pos, ChessPiece newPiece) {
        board[pos.getRow()][pos.getCol()] = newPiece;
    }

    public boolean isCastlingMove(Position from, Position to) {
        ChessPiece piece = getPiece(from);

        if (!(piece instanceof King)) {
            return false;
        }
        if (piece.isWhite() != isWhiteTurn) {
            return false;
        }
        if (piece.hasMoved()) {
            return false;
        }

        int row = from.getRow();
        int colFrom = from.getCol();
        int colTo = to.getCol();

        // Nhập thành bên vua
        if (colFrom == 4 && colTo == 6) {
            ChessPiece rook = getPiece(new Position(row, 7));
            if (!(rook instanceof Rook)) {
                return false;
            }
            if (rook.hasMoved()) {

                return false;
            }

            if (getPieceAt(row, 5) != null || getPieceAt(row, 6) != null) {
                return false;
            }
            if (wouldBeInCheckAfterMove(from, new Position(row, 5))
                    || wouldBeInCheckAfterMove(from, to)) {
                return false;
            }
            if (isInCheck(isWhiteTurn)) {
                return false;
            }
            return true;
        }

        // Nhập thành bên hậu
        if (colFrom == 4 && colTo == 2) {
            ChessPiece rook = getPiece(new Position(row, 0));
            if (!(rook instanceof Rook)) {
                return false;
            }
            if (rook.hasMoved()) {
                return false;
            }
            if (getPieceAt(row, 1) != null || getPieceAt(row, 2) != null || getPieceAt(row, 3) != null) {

                return false;
            }
            if (wouldBeInCheckAfterMove(from, new Position(row, 3))
                    || wouldBeInCheckAfterMove(from, to)) {

                return false;
            }
            if (isInCheck(isWhiteTurn)) {
                return false;
            }

            return true;
        }
        return false;
    }

    public void performCastling(Position from, Position to) {
        ChessPiece king = getPiece(from);
        if (!(king instanceof King)) {
            return;
        }

        int row = from.getRow();
        int colDiff = to.getCol() - from.getCol();

        // Di chuyển vua
        movePieceInternal(from, to);
        king.setHasMoved(true);

        // Di chuyển xe
        if (colDiff == 2) { // Nhập thành bên vua (king-side)
            Position rookFrom = new Position(row, 7);
            Position rookTo = new Position(row, 5);
            movePieceInternal(rookFrom, rookTo);
            getPiece(rookTo).setHasMoved(true);
        } else if (colDiff == -2) { // Nhập thành bên hậu (queen-side)
            Position rookFrom = new Position(row, 0);
            Position rookTo = new Position(row, 3);
            movePieceInternal(rookFrom, rookTo);
            getPiece(rookTo).setHasMoved(true);
        }

        isWhiteTurn = !isWhiteTurn;
    }

    public void toggleTurn() {
        isWhiteTurn = !isWhiteTurn;
    }

    public boolean isInCheck(boolean forWhite) {
        Position kingPos = findKing(forWhite);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.isWhite() != forWhite) {
                    if (piece.canAttack(kingPos, this)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isCheckmate(boolean forWhite) {
        if (!isInCheck(forWhite)) {
            return false;
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.isWhite() == forWhite) {
                    for (Position move : piece.getValidMoves(this)) {
                        ChessBoard clone = this.clone();
                        clone.movePieceInternal(new Position(row, col), move);
                        if (!clone.isInCheck(forWhite)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isStalemate(boolean forWhite) {
        if (isInCheck(forWhite)) {
            return false;
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.isWhite() == forWhite) {
                    for (Position move : piece.getValidMoves(this)) {
                        ChessBoard clone = this.clone();
                        clone.movePieceInternal(new Position(row, col), move);
                        if (!clone.isInCheck(forWhite)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isDrawBy50MoveRule() {
        return halfMoveClock >= 100;
    }

    public boolean isThreefoldRepetition() {
        for (int count : positionHistory.values()) {
            if (count >= 3) {
                return true;
            }
        }
        return false;
    }

    public void updatePositionHistory() {
        String state = getBoardState();
        positionHistory.put(state, positionHistory.getOrDefault(state, 0) + 1);
        gameState.updatePositionHistory(state);
    }

    private String getBoardState() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece p = board[row][col];
                if (p != null) {
                    sb.append(p.getSymbol())
                            .append(p.isWhite() ? "W" : "B")
                            .append(row)
                            .append(col);
                }
            }
        }
        sb.append(isWhiteTurn ? "W" : "B");
        return sb.toString();
    }

    public ChessPiece getPiece(Position pos) {
        return board[pos.getRow()][pos.getCol()];
    }

    public ChessPiece getPieceAt(int row, int col) {
        if (!isValidPosition(row, col)) {
            return null;
        }
        return board[row][col];
    }

    public void setPieceAt(int row, int col, ChessPiece piece) {
        board[row][col] = piece;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public Position findKing(boolean isWhite) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece p = board[row][col];
                if (p instanceof King && p.isWhite() == isWhite) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    public void initializePieces() {
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Pawn(true, 6, col);
            board[1][col] = new Pawn(false, 1, col);
        }
        board[7][0] = new Rook(true, 7, 0);
        board[7][7] = new Rook(true, 7, 7);
        board[7][1] = new Knight(true, 7, 1);
        board[7][6] = new Knight(true, 7, 6);
        board[7][2] = new Bishop(true, 7, 2);
        board[7][5] = new Bishop(true, 7, 5);
        board[7][3] = new Queen(true, 7, 3);
        board[7][4] = new King(true, 7, 4);
        board[0][0] = new Rook(false, 0, 0);
        board[0][7] = new Rook(false, 0, 7);
        board[0][1] = new Knight(false, 0, 1);
        board[0][6] = new Knight(false, 0, 6);
        board[0][2] = new Bishop(false, 0, 2);
        board[0][5] = new Bishop(false, 0, 5);
        board[0][3] = new Queen(false, 0, 3);
        board[0][4] = new King(false, 0, 4);
        board[7][0] = new Rook(true, 7, 0);
        board[7][0].setHasMoved(false);
        board[7][7] = new Rook(true, 7, 7);
        board[7][7].setHasMoved(false);
        board[7][4] = new King(true, 7, 4);
        board[7][4].setHasMoved(false);

        board[0][0] = new Rook(false, 0, 0);
        board[0][0].setHasMoved(false);
        board[0][7] = new Rook(false, 0, 7);
        board[0][7].setHasMoved(false);
        board[0][4] = new King(false, 0, 4);
        board[0][4].setHasMoved(false);
        // Đảm bảo khi khởi tạo KHÔNG gán hasMoved = true
        board[7][4].setHasMoved(false);
        board[0][4].setHasMoved(false);

    }

    public ChessBoard clone() {
        ChessBoard clone = new ChessBoard(isWhiteTurn);
        clone.board = new ChessPiece[8][8];
        clone.isWhiteTurn = this.isWhiteTurn;
        clone.halfMoveClock = this.halfMoveClock;
        clone.promotionPending = this.promotionPending;
        clone.enPassantTarget = (enPassantTarget == null) ? null : new Position(enPassantTarget.getRow(), enPassantTarget.getCol());
        clone.lastMove = this.lastMove;
        try {
            clone.gameState = this.gameState.clone();
        } catch (Exception e) {
            e.printStackTrace();
            clone.gameState = new GameState();
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    clone.board[row][col] = piece.clone();
                }
            }
        }
        return clone;
    }

    public static class Move {

        public Position from, to;
        public ChessPiece pieceMoved, pieceCaptured;
        public Position enPassantTarget;
        public int halfMoveClock;

        public Move(Position from, Position to, ChessPiece pieceMoved, ChessPiece pieceCaptured,
                Position enPassantTarget, int halfMoveClock) {
            this.from = from;
            this.to = to;
            this.pieceMoved = pieceMoved;
            this.pieceCaptured = pieceCaptured;
            this.enPassantTarget = enPassantTarget;
            this.halfMoveClock = halfMoveClock;
        }
    }

    // Sau mỗi lượt đi của người chơi, gọi phương thức này để chuyển lượt:
    public void nextTurn() {
        isWhiteTurn = !isWhiteTurn; // Đổi lượt chơi
    }

    public void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        movePiece(fromRow, fromCol, toRow, toCol);
        nextTurn();
    }

    public void resetBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }
        initializePieces();
        isWhiteTurn = true;
        halfMoveClock = 0;
        enPassantTarget = null;
        positionHistory.clear();
    }

    public boolean movePiece(Position from, Position to) {
        return movePiece(from.getRow(), from.getCol(), to.getRow(), to.getCol());
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public boolean isValidCastling(King king, Position to) {
        return isCastlingMove(new Position(king.getRow(), king.getCol()), to);
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public List<Move> getAllValidMoves(boolean forWhite) {
        List<Move> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = getPieceAt(row, col);
                if (piece != null && piece.isWhite() == forWhite) {
                    List<Position> validMoves = piece.getLegalMoves(this, gameState);
                    for (Position to : validMoves) {
                        Position from = new Position(row, col);
                        ChessPiece captured = getPiece(to);
                        moves.add(new Move(from, to, piece, captured, getEnPassantTarget(), 0));
                    }
                }
            }
        }
        return moves;
    }

    public int evaluate() {
        int score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    int value = getPieceValue(piece);
                    score += piece.isWhite() ? value : -value;
                }
            }
        }
        return score;
    }

    private int getPieceValue(ChessPiece piece) {
        if (piece instanceof Queen) {
            return 9;
        }
        if (piece instanceof Rook) {
            return 5;
        }
        if (piece instanceof Bishop || piece instanceof Knight) {
            return 3;
        }
        if (piece instanceof Pawn) {
            return 1;
        }
        if (piece instanceof King) {
            return 1000;
        }
        return 0;
    }

    public ChessBoard cloneWithoutValidation() {
        ChessBoard clone = new ChessBoard(this.isWhiteTurn, true);
        clone.halfMoveClock = this.halfMoveClock;
        clone.promotionPending = this.promotionPending;
        clone.enPassantTarget = (enPassantTarget == null) ? null : new Position(enPassantTarget.getRow(), enPassantTarget.getCol());
        clone.lastMove = this.lastMove;
        try {
            clone.gameState = this.gameState.clone();
        } catch (Exception e) {
            e.printStackTrace();
            clone.gameState = new GameState();
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = this.board[row][col];
                if (piece != null) {
                    clone.board[row][col] = piece.clone();
                }
            }
        }
        return clone;
    }

    public void setWhiteTurn(boolean isWhiteTurn) {
        this.isWhiteTurn = isWhiteTurn;
    }

}
