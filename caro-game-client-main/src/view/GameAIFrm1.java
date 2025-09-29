package view;

import controller.Client;
import model.chess.*;
import model.chess.pieces.*;
import model.chess.ChessAI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;
import java.util.Random;
import model.User;

// Mô tả: Giao diện chính cho người chơi trong một ván cờ vua mạng
// Bao gồm: Bàn cờ, hiển thị người chơi, gửi tin nhắn, âm thanh, mic, đồng hồ, xử lý thắng/thua
// 🧱 IMPORT: Thư viện giao diện, mạng, âm thanh, dữ liệu
public class GameAIFrm1 extends javax.swing.JFrame {

    private final ChessBoard chessBoard;
    private final JButton[][] buttons = new JButton[8][8];
    private Position selectedPosition = null;
    private final boolean isWhitePlayer = true; // mặc định là trắng

    private final JLabel statusLabel = new JLabel("Lượt của bạn (Trắng)");

    private final JButton resetBtn = new JButton("Ván mới");

    public GameAIFrm1() {
        
        if (Client.user == null) {
    Client.user = new User();
    Client.user.setNickname("Người chơi");
    Client.user.setNumberOfGame(0);
    Client.user.setNumberOfWin(0);
    Client.user.setAvatar("default"); // nhớ có file default.jpg trong assets/game
}
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(resetBtn, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        initComponents();

        playerNicknameValue.setText(Client.user.getNickname());
        playerNumberOfGameValue.setText(String.valueOf(Client.user.getNumberOfGame()));
        playerNumberOfWinValue.setText(String.valueOf(Client.user.getNumberOfWin()));
        playerButtonImage.setIcon(new ImageIcon("assets/game/" + Client.user.getAvatar() + ".jpg"));

        chessBoard = new ChessBoard(true);
        this.setTitle("ChiChess");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setSize(1000, 700);
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(80, 80));
                btn.setBackground((i + j) % 2 == 0
                        ? new Color(240, 217, 181)
                        : new Color(118, 150, 86));
                final int row = i, col = j;
                btn.addActionListener(e -> handleClick(row, col));
                buttons[i][j] = btn;
                boardPanel.add(btn);
            }
        }
        gameBoardPanel.setLayout(new BorderLayout()); // 👈 đảm bảo có layout
        gameBoardPanel.add(boardPanel, BorderLayout.CENTER); // ✅ gắn vào gameBoardPanel
        updateBoard();
    }

    private void handleClick(int row, int col) {
        Position clicked = new Position(row, col);

        if (selectedPosition == null) {
            ChessPiece piece = chessBoard.getPiece(clicked);
            if (piece != null && piece.isWhite() == chessBoard.isWhiteTurn()) {
                selectedPosition = clicked;
                highlightMoves(clicked);
            }
        } else {
            if (chessBoard.getValidMovesFor(selectedPosition.getRow(), selectedPosition.getCol()).contains(clicked)) {
                // 🧠 Xử lý nước đi
                boolean moved = chessBoard.movePiece(selectedPosition, clicked);

                updateBoard();
                clearHighlights();
                selectedPosition = null;

                if (moved) {
                    // 📌 Kiểm tra phong tốt
                    if (chessBoard.isPromotionPending()) {
                        ChessPiece promoted = showPromotionDialog(true, clicked.getRow(), clicked.getCol());
                        chessBoard.promotePawn(clicked, promoted);
                        chessBoard.setPromotionPending(false);
                    }

                    processTurn();
                }
            } else {
                selectedPosition = null;
                clearHighlights();
            }
        }
    }

    private void aiMove() {
        ChessBoard.Move best = ChessAI.findBestMove(chessBoard, 3);
        if (best == null) {
            JOptionPane.showMessageDialog(this, "Máy hết nước đi!");
            return;
        }

        chessBoard.movePiece(best.from, best.to);

        // 👇 Nếu máy phong tốt
        if (chessBoard.isPromotionPending()) {
            ChessPiece queen = new Queen(false, best.to.getRow(), best.to.getCol());
            chessBoard.promotePawn(best.to, queen);
            chessBoard.setPromotionPending(false);
        }

        updateBoard();
        processTurn();
    }

    private void resetGame() {
        chessBoard.resetBoard();
        selectedPosition = null;
        updateBoard(); // 🧠 cập nhật UI ngay sau khi reset

        // 👇 Nếu máy cầm trắng, cho máy đi ngay
        if (chessBoard.isWhiteTurn() && !isWhitePlayer) {
            SwingUtilities.invokeLater(this::aiMove);
        } else {
            processTurn();
        }
    }

    private void updateBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = chessBoard.getPieceAt(i, j);
                buttons[i][j].setIcon(getPieceImage(piece)); // 👈 Dùng hàm mới gộp
            }
        }
    }
    
    
    private void highlightMoves(Position from) {
        clearHighlights();
        List<Position> moves = chessBoard.getValidMovesFor(from.getRow(), from.getCol());

        // 🌟 Viền vàng ô đang chọn
        buttons[from.getRow()][from.getCol()].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));

        // 💡 Highlight nước đi bằng xanh nhạt
        for (Position move : moves) {
            buttons[move.getRow()][move.getCol()].setBackground(new Color(144, 238, 144));
        }
    }

    private void clearHighlights() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                buttons[i][j].setBackground((i + j) % 2 == 0
                        ? new Color(240, 217, 181)
                        : new Color(181, 136, 99));
                buttons[i][j].setBorder(null); // 🧽 xoá viền nếu có
            }
        }
    }

    private void returnToMainMenu() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Trận đấu đã kết thúc. Bạn có muốn quay lại trang chủ không?",
                "Trở về Trang Chủ",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            // 👉 Ở đây bạn xử lý quay về trang chính
            this.dispose(); // đóng cửa sổ hiện tại
            // new MainMenuFrm().setVisible(true); // (nếu bạn có trang chủ riêng)
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // ==== XII. initComponents ====
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jFrame2 = new javax.swing.JFrame();
        jFrame3 = new javax.swing.JFrame();
        jFrame4 = new javax.swing.JFrame();
        jLabel7 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        competitorLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        playerNicknameValue = new javax.swing.JLabel();
        playerNumberOfGameValue = new javax.swing.JLabel();
        playerNumberOfWinValue = new javax.swing.JLabel();
        playerNumberOfWinLabel = new javax.swing.JLabel();
        playerNicknameLabel = new javax.swing.JLabel();
        playerNumberOfGameLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        playerLabel = new javax.swing.JLabel();
        vsIcon = new javax.swing.JLabel();
        competitorNicknameLabel1 = new javax.swing.JLabel();
        competitorNumberOfWinValue1 = new javax.swing.JLabel();
        competitorNumberOfGameLabel = new javax.swing.JLabel();
        competitorNumberOfWinLabel1 = new javax.swing.JLabel();
        competitorNumberOfGameValue = new javax.swing.JLabel();
        competitorNicknameValue1 = new javax.swing.JLabel();
        compretitorTurnJLabel = new javax.swing.JLabel();
        yourTurnJLabel = new javax.swing.JLabel();
        playerCurrentPosition = new javax.swing.JLabel();
        scoreLabel1 = new javax.swing.JLabel();
        competitorCurrentPosition = new javax.swing.JLabel();
        playerButtonImage = new javax.swing.JLabel();
        competitorButtonImage = new javax.swing.JButton();
        gameBoardPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mainMenu = new javax.swing.JMenu();
        newGameMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame3Layout = new javax.swing.GroupLayout(jFrame3.getContentPane());
        jFrame3.getContentPane().setLayout(jFrame3Layout);
        jFrame3Layout.setHorizontalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame3Layout.setVerticalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame4Layout = new javax.swing.GroupLayout(jFrame4.getContentPane());
        jFrame4.getContentPane().setLayout(jFrame4Layout);
        jFrame4Layout.setHorizontalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame4Layout.setVerticalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAutoRequestFocus(false);

        jPanel1.setBackground(new java.awt.Color(120, 149, 85));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setForeground(new java.awt.Color(102, 102, 102));

        competitorLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorLabel.setText("AI");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(competitorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(100, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(competitorLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel5.setBackground(new java.awt.Color(102, 102, 102));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 327, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        playerNicknameValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNicknameValue.setForeground(new java.awt.Color(255, 255, 255));
        playerNicknameValue.setText("{nickname}");

        playerNumberOfGameValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfGameValue.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfGameValue.setText("{sovanchoi}");

        playerNumberOfWinValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfWinValue.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfWinValue.setText("{sovanthang}");

        playerNumberOfWinLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfWinLabel.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfWinLabel.setText("Số ván thắng");

        playerNicknameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNicknameLabel.setForeground(new java.awt.Color(255, 255, 255));
        playerNicknameLabel.setText("Nickname");

        playerNumberOfGameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfGameLabel.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfGameLabel.setText("Số ván chơi");

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        playerLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerLabel.setText("Bạn");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(playerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(112, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(playerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        competitorNicknameLabel1.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNicknameLabel1.setForeground(new java.awt.Color(255, 255, 255));
        competitorNicknameLabel1.setText("Nickname");

        competitorNumberOfWinValue1.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNumberOfWinValue1.setForeground(new java.awt.Color(255, 255, 255));
        competitorNumberOfWinValue1.setText("Nhiều lắm");

        competitorNumberOfGameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNumberOfGameLabel.setForeground(new java.awt.Color(255, 255, 255));
        competitorNumberOfGameLabel.setText("Số ván chơi");

        competitorNumberOfWinLabel1.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNumberOfWinLabel1.setForeground(new java.awt.Color(255, 255, 255));
        competitorNumberOfWinLabel1.setText("Số ván thắng");

        competitorNumberOfGameValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNumberOfGameValue.setForeground(new java.awt.Color(255, 255, 255));
        competitorNumberOfGameValue.setText("Nhiều lắm");

        competitorNicknameValue1.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNicknameValue1.setForeground(new java.awt.Color(255, 255, 255));
        competitorNicknameValue1.setText("Máy");

        compretitorTurnJLabel.setForeground(new java.awt.Color(0, 0, 204));
        compretitorTurnJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        compretitorTurnJLabel.setText("Máy");

        yourTurnJLabel.setForeground(new java.awt.Color(255, 0, 0));
        yourTurnJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        yourTurnJLabel.setText("Bạn");

        playerCurrentPosition.setText("Trắng");

        scoreLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        scoreLabel1.setText("Tỉ số:  0-0");

        competitorCurrentPosition.setText("Đen");

        playerButtonImage.setBackground(new java.awt.Color(102, 102, 102));

        competitorButtonImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                competitorButtonImageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(playerButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(vsIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(playerNumberOfGameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(playerNicknameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(26, 26, 26)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(playerNicknameValue, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(playerNumberOfGameValue)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(playerNumberOfWinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(26, 26, 26)
                                        .addComponent(playerNumberOfWinValue, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(competitorButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(competitorNicknameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(39, 39, 39)
                                        .addComponent(competitorNicknameValue1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(competitorNumberOfGameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(competitorNumberOfWinLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(27, 27, 27)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(competitorNumberOfGameValue, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(competitorNumberOfWinValue1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(playerCurrentPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(scoreLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(competitorCurrentPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(yourTurnJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(compretitorTurnJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(playerNicknameLabel)
                            .addComponent(playerNicknameValue))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(playerNumberOfGameLabel)
                            .addComponent(playerNumberOfGameValue))
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(playerNumberOfWinLabel)
                            .addComponent(playerNumberOfWinValue)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(playerButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(competitorNicknameLabel1)
                            .addComponent(competitorNicknameValue1))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(competitorNumberOfGameLabel)
                            .addComponent(competitorNumberOfGameValue)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(vsIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(competitorButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(competitorNumberOfWinLabel1)
                    .addComponent(competitorNumberOfWinValue1))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(competitorCurrentPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scoreLabel1)
                    .addComponent(playerCurrentPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yourTurnJLabel)
                    .addComponent(compretitorTurnJLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        gameBoardPanel.setBackground(new java.awt.Color(102, 102, 102));
        gameBoardPanel.setMaximumSize(new java.awt.Dimension(512, 512));
        gameBoardPanel.setMinimumSize(new java.awt.Dimension(512, 512));
        gameBoardPanel.setPreferredSize(new java.awt.Dimension(512, 512));

        javax.swing.GroupLayout gameBoardPanelLayout = new javax.swing.GroupLayout(gameBoardPanel);
        gameBoardPanel.setLayout(gameBoardPanelLayout);
        gameBoardPanelLayout.setHorizontalGroup(
            gameBoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 646, Short.MAX_VALUE)
        );
        gameBoardPanelLayout.setVerticalGroup(
            gameBoardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 559, Short.MAX_VALUE)
        );

        mainMenu.setText("Menu");
        mainMenu.setToolTipText("");

        newGameMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        newGameMenuItem.setText("Game mới");
        newGameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newGameMenuItemActionPerformed(evt);
            }
        });
        mainMenu.add(newGameMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        exitMenuItem.setText("Thoát");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        mainMenu.add(exitMenuItem);

        jMenuBar1.add(mainMenu);

        helpMenu.setText("Help");

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        helpMenuItem.setText("Trợ giúp");
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gameBoardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 646, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gameBoardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7))
        );

        //for(int i=0; i<5; i++){
            //    for(int j=0;j<5;j++){
                //        gameBoardPanel.add(button[i][j]);
                //    }
            //}

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newGameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newGameMenuItemActionPerformed
        JOptionPane.showMessageDialog(rootPane, "Thông báo", "Tính năng đang được phát triển", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_newGameMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuItemActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(rootPane,
                "LUẬT CỜ VUA:\n"
                + "1. Mỗi loại quân có cách di chuyển riêng\n"
                + "2. Mục tiêu là chiếu bí Vua đối phương\n"
                + "3. Các nước đặc biệt: Nhập thành, Bắt tốt qua đường\n"
                + "4. Xếp hạng theo Elo rating\n"
                + "Chúc bạn chơi vui vẻ!");
    }//GEN-LAST:event_helpMenuItemActionPerformed

    private void competitorButtonImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_competitorButtonImageActionPerformed

    }//GEN-LAST:event_competitorButtonImageActionPerformed

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
        // 👂 You may add sound effect here
        Toolkit.getDefaultToolkit().beep();
    }

    int not(int i) {
        if (i == 1) {
            return 0;
        }
        if (i == 0) {
            return 1;
        }
        return 0;
    }

    void setupButton() {

    }

    public void setLose(String xx, String yy) {

    }
    private ChessPiece selectedPiece = null;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private ChessPiece showPromotionDialog(boolean isWhite, int row, int col) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Chọn quân để phong:",
                "Phong tốt",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case "Queen":
                return new Queen(isWhite, row, col);
            case "Rook":
                return new Rook(isWhite, row, col);
            case "Bishop":
                return new Bishop(isWhite, row, col);
            case "Knight":
                return new Knight(isWhite, row, col);
            default:
                return new Queen(isWhite, row, col); // Mặc định nếu không chọn
        }
    }

    private ImageIcon getPieceImage(ChessPiece piece) {
        if (piece == null) {
            return null;
        }

        String color = piece.isWhite() ? "white" : "black";
        String name = piece.getClass().getSimpleName().toLowerCase();
        String path = "/Resources/" + name + "_" + color + ".png";

        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            ImageIcon originalIcon = new ImageIcon(imgURL);
            // 👇 Scale hình nhỏ lại: ví dụ 60x60 (có thể điều chỉnh xuống 48x48 nếu m thấy vẫn to)
            Image scaledImage = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } else {
            System.err.println("❌ Không tìm thấy hình ảnh: " + path);
            return null;
        }
    }

    private void processTurn() {
        updateBoard();

        if (chessBoard.isCheckmate(true)) {
            JOptionPane.showMessageDialog(this, "Bạn đã thua!");
            returnToMainMenu(); // 👈 thêm dòng này
            return;
        }

        if (chessBoard.isCheckmate(false)) {
            JOptionPane.showMessageDialog(this, "Bạn đã thắng!");
            returnToMainMenu(); // 👈 thêm dòng này
            return;
        }

        if (chessBoard.isWhiteTurn()) {
            statusLabel.setText("Lượt của bạn (Trắng)");
            yourTurnJLabel.setVisible(true);
            compretitorTurnJLabel.setVisible(false);
        } else {
            statusLabel.setText("Đang đợi máy...");
            yourTurnJLabel.setVisible(false);
            compretitorTurnJLabel.setVisible(true);
            SwingUtilities.invokeLater(this::aiMove);
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new view.GameAIFrm1().setVisible(true);
        });
    }

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton competitorButtonImage;
    private javax.swing.JLabel competitorCurrentPosition;
    private javax.swing.JLabel competitorLabel;
    private javax.swing.JLabel competitorNicknameLabel1;
    private javax.swing.JLabel competitorNicknameValue1;
    private javax.swing.JLabel competitorNumberOfGameLabel;
    private javax.swing.JLabel competitorNumberOfGameValue;
    private javax.swing.JLabel competitorNumberOfWinLabel1;
    private javax.swing.JLabel competitorNumberOfWinValue1;
    private javax.swing.JLabel compretitorTurnJLabel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPanel gameBoardPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    private javax.swing.JFrame jFrame3;
    private javax.swing.JFrame jFrame4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JMenu mainMenu;
    private javax.swing.JMenuItem newGameMenuItem;
    private javax.swing.JLabel playerButtonImage;
    private javax.swing.JLabel playerCurrentPosition;
    private javax.swing.JLabel playerLabel;
    private javax.swing.JLabel playerNicknameLabel;
    private javax.swing.JLabel playerNicknameValue;
    private javax.swing.JLabel playerNumberOfGameLabel;
    private javax.swing.JLabel playerNumberOfGameValue;
    private javax.swing.JLabel playerNumberOfWinLabel;
    private javax.swing.JLabel playerNumberOfWinValue;
    private javax.swing.JLabel scoreLabel1;
    private javax.swing.JLabel vsIcon;
    private javax.swing.JLabel yourTurnJLabel;
    // End of variables declaration//GEN-END:variables

}
