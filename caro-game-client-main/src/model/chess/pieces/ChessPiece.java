package model.chess.pieces;

import model.chess.ChessBoard;
import model.chess.Position;
import model.chess.GameState;
import java.util.List;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import java.awt.Image;

public abstract class ChessPiece {

    protected Icon icon;
    protected String imagePath;
    protected boolean isWhite;
    protected int row;
    protected int col;
    protected boolean hasMoved;
    protected boolean isCaptured;
    protected Position position;
    private Image image;

    public ChessPiece(Icon icon, String imagePath, boolean isWhite, int row, int col, boolean hasMoved, boolean isCaptured, Position position) {
        this.icon = icon;
        this.imagePath = imagePath;
        this.isWhite = isWhite;
        this.row = row;
        this.col = col;
        this.hasMoved = hasMoved;
        this.isCaptured = isCaptured;
        this.position = (position != null) ? position : new Position(row, col);
    }

    public ChessPiece(Icon icon, boolean isWhite, int row, int col) {
        this(icon, "", isWhite, row, col, false, false, new Position(row, col));
    }

    public ChessPiece(boolean isWhite, int row, int col) {
        this(null, isWhite, row, col);
    }

    public Image getImage() {
        return image;
    }
    public void setImage(Image image) {
        this.image = image;
    }

    public void loadImage(String imagePath) {
        try {
            image = new ImageIcon(getClass().getResource(imagePath)).getImage();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Không tìm thấy ảnh: " + imagePath);
        }
    }

    // Các phương thức trừu tượng bắt buộc
    public abstract List<Position> calculateValidMoves(ChessBoard board);
    public abstract String getSymbol();
    public abstract ChessPiece clone();

    // Các phương thức chung
    public List<Position> getValidMoves(ChessBoard board) {
        return calculateValidMoves(board);
    }

    public List<Position> getLegalMoves(ChessBoard board, GameState gameState) {
        List<Position> validMoves = getValidMoves(board);
        List<Position> legalMoves = new ArrayList<>();
        Position from = getPosition();
        if (from == null) {
            from = new Position(getRow(), getCol());
        }
        for (Position target : validMoves) {
            ChessBoard clonedBoard = board.clone();
            clonedBoard.movePiece(from, target);
            if (!gameState.isKingInCheck(clonedBoard, this.isWhite)) {
                legalMoves.add(target);
            }
        }
        return legalMoves;
    }

    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] board);

    public boolean isValidMove(Position to, ChessBoard board) {
        return calculateValidMoves(board).contains(to);
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.position = new Position(row, col);
        this.hasMoved = true;
    }

    public void setCaptured(boolean captured) {
        this.isCaptured = captured;
    }

    public boolean isSameColor(ChessPiece other) {
        return other != null && this.isWhite == other.isWhite;
    }

    public List<Position> getPossibleMoves(ChessBoard board) {
        return getValidMoves(board);
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean canAttack(Position target, ChessBoard board) {
        return isValidMove(target, board);
    }

    public abstract boolean isValidMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol);

    public Icon getIcon() {
        return icon;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public boolean hasMoved() {
        return hasMoved;
    }
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    public boolean isCaptured() {
        return isCaptured;
    }
    public Position getPosition() {
        return new Position(row, col);
    }
    public String getImagePath() {
        return imagePath;
    }

    public boolean isKing() {
        return "K".equalsIgnoreCase(getSymbol());
    }
    public boolean isPawn() {
        return "P".equalsIgnoreCase(getSymbol());
    }
    public boolean canMoveTo(Position position, ChessBoard board) {
        return isValidMove(position, board);
    }
}
