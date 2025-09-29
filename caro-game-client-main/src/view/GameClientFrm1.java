package view;

import controller.Client;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.JLabel;
import java.util.List;


import model.User;
import model.chess.ChessBoard;
import model.chess.Position;
import model.chess.pieces.*;

// Mô tả: Giao diện chính cho người chơi trong một ván cờ vua mạng
// Bao gồm: Bàn cờ, hiển thị người chơi, gửi tin nhắn, âm thanh, mic, đồng hồ, xử lý thắng/thua
// 🧱 IMPORT: Thư viện giao diện, mạng, âm thanh, dữ liệu
public class GameClientFrm1 extends javax.swing.JFrame {

    // ==== I. BIẾN THÀNH PHẦN ====
    // ♟️ Bàn cờ và quân cờ
    private ChessPiece[][] board;
    boolean whiteStarts = new Random().nextBoolean();
    ChessBoard chessBoard = new ChessBoard(whiteStarts);
    private JButton[][] chessBoardButtons;
    private JButton[][] squares = new JButton[8][8];
    private Position selectedPosition = null;

    // 🔄 Luật chơi
    private boolean isWhiteTurn = true;
    private boolean isWhitePlayer;

    // 🧑‍🤝‍🧑 Thông tin người chơi
    private final User competitor;
    private final String competitorIP;

    // ⏲️ Đồng hồ đếm giờ
    private Timer whiteTimer, blackTimer;
    private int whiteTimeLeft, blackTimeLeft;
    private JLabel whiteClockLabel, blackClockLabel;
    private Timer timer;
    private Integer second;
    private Integer minute;

    // 🏁 Tỉ số
    private int gameNumber = 0;
    private int userWin = 0;
    private int competitorWin = 0;
    private int numberOfMatch;
    private int opponentWin = 0;

    // 🔊 Âm thanh & thoại
    private boolean isSending;
    private boolean isListening;

    // 🎮 Giao diện điều khiển
    private javax.swing.JProgressBar jProgressBar1;
    private JButton preButton;
    private JPanel chessBoardPanel;

    public static GameClientFrm1 currentInstance;

    // ==== II. HÀM KHỞI TẠO ====
    public GameClientFrm1(User competitor, int room_ID, int isStart, String competitorIP, boolean isWhitePlayer) {
        board = new ChessPiece[8][8];

        this.chessBoardButtons = new JButton[8][8];
        initComponents();
        initChessBoard();

        this.setTitle("ChiChess");
        this.setIconImage(new ImageIcon("assets/image/logo.png").getImage());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(null);

        this.isWhitePlayer = isWhitePlayer;
        this.chessBoard = new ChessBoard(isWhitePlayer); // Bên trắng đi trước nếu người chơi là trắng

        this.setSize(1000, 700);
        numberOfMatch = isStart;
        this.competitor = competitor;
        this.competitorIP = competitorIP;

        isSending = false;
        isListening = false;
        microphoneStatusButton.setIcon(new ImageIcon("assets/game/mute.png"));
        speakerStatusButton.setIcon(new ImageIcon("assets/game/mutespeaker.png"));

        gameNumber++;

        playerLabel.setFont(new Font("Arial", Font.BOLD, 15));
        competitorLabel.setFont(new Font("Arial", Font.BOLD, 15));
        roomNameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        roomNameLabel.setAlignmentX(JLabel.CENTER);
        sendButton.setBackground(Color.white);
        sendButton.setIcon(new ImageIcon("assets/image/send2.png"));
        playerNicknameValue.setText(Client.user.getNickname());
        playerNumberOfGameValue.setText(Integer.toString(Client.user.getNumberOfGame()));
        playerNumberOfWinValue.setText(Integer.toString(Client.user.getNumberOfWin()));
        playerButtonImage.setIcon(new ImageIcon("assets/game/" + Client.user.getAvatar() + ".jpg"));
        roomNameLabel.setText("Phòng: " + room_ID);
        vsIcon.setIcon(new ImageIcon("assets/game/swords-1.png"));
        competitorNicknameValue.setText(competitor.getNickname());
        competotorNumberOfGameValue.setText(Integer.toString(competitor.getNumberOfGame()));
        competitorNumberOfWinValue.setText(Integer.toString(competitor.getNumberOfWin()));
        competotorButtonImage.setIcon(new ImageIcon("assets/game/" + competitor.getAvatar() + ".jpg"));
        competotorButtonImage.setToolTipText("Xem thông tin đối thủ");
        playerCurrentPositionLabel.setVisible(false);
        competitorPositionLabel.setVisible(false);
        drawRequestButton.setVisible(false);
        playerTurnLabel.setVisible(false);
        competitorTurnLabel.setVisible(false);
        countDownLabel.setVisible(false);
        messageTextArea.setEditable(false);
        scoreLabel.setText("Tỉ số: 0-0");

        second = 400;
        minute = 4;
        timer = new Timer(1000, e -> {
            String timeStr = String.format("%02d:%02d", minute, second);
            countDownLabel.setText("Thời Gian: " + timeStr);
            if (second == 0) {
                second = 400;
                minute = 4;
                showMessage("Bạn đã thua do quá thời gian!");
                try {
                    Client.socketHandle.write("lose,");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                second--;
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitGame();
            }
        });
    }

    private boolean isUserWhite() {
        return isWhitePlayer;
    }

    // ==== III. XỬ LÝ BÀN CỜ ====
    private void initChessBoard() {
        gamePanel.setLayout(new GridLayout(8, 8));
        for (int displayRow = 0; displayRow < 8; displayRow++) {
            for (int displayCol = 0; displayCol < 8; displayCol++) {
                int actualRow = isWhitePlayer ? 7 - displayRow : displayRow;
                int actualCol = isWhitePlayer ? displayCol : 7 - displayCol;

                JButton square = new JButton();
                square.setPreferredSize(new Dimension(50, 50));
                square.setBackground((displayRow + displayCol) % 2 == 0
                        ? new Color(240, 217, 181)
                        : new Color(118, 150, 86));
                ChessPiece piece = chessBoard.getPieceAt(actualRow, actualCol);
                square.setIcon(getChessPieceIcon(piece));

                // Pass displayRow and displayCol to properly reverse later
                final int finalDisplayRow = displayRow;
                final int finalDisplayCol = displayCol;

                square.addActionListener(e -> handleBoardClick(finalDisplayRow, finalDisplayCol));
                chessBoardButtons[displayRow][displayCol] = square;
                gamePanel.add(square);
            }
        }
    }

    private void handleBoardClick(int displayRow, int displayCol) {
        int actualRow = isWhitePlayer ? 7 - displayRow : displayRow;
        int actualCol = isWhitePlayer ? displayCol : 7 - displayCol;

        Position clicked = new Position(actualRow, actualCol);
        ChessPiece clickedPiece = chessBoard.getPiece(clicked);
        boolean isMyTurn = (isWhitePlayer == chessBoard.isWhiteTurn());

        if (selectedPosition == null) {
            if (clickedPiece != null && clickedPiece.isWhite() == isWhitePlayer && isMyTurn) {
                selectedPosition = clicked;
                highlightSquare(displayRow, displayCol);
                highlightValidMoves(clicked);
            }
            return;
        }

        if (!isMyTurn) {
            int selectedDisplayRow = isWhitePlayer ? 7 - selectedPosition.getRow() : selectedPosition.getRow();
            int selectedDisplayCol = isWhitePlayer ? selectedPosition.getCol() : 7 - selectedPosition.getCol();
            unhighlightSquare(selectedDisplayRow, selectedDisplayCol);
            selectedPosition = null;
            return;
        }

        // Xử lý đi quân
        Position from = selectedPosition;
        Position to = clicked;

        if (chessBoard.isValidMove(from, to)) {
            boolean moved = chessBoard.movePiece(from, to);
            if (moved) {
                updateBoardUI();
                playSound();

                // Gửi nước đi cho đối thủ
                try {
                    ChessPiece movingPiece = chessBoard.getPiece(to);
                    String type = movingPiece.getClass().getSimpleName();
                    String msg = "chess-move," + from.getRow() + "," + from.getCol() + "," + to.getRow() + "," + to.getCol() + "," + type + "," + (movingPiece.isWhite() ? "1" : "0");
                    Client.socketHandle.write(msg);
                } catch (IOException e) {
                    showMessage("Lỗi gửi nước đi!");
                }

                displayCompetitorTurn();
                stopTimer();
            }
        }

        int selectedDisplayRow = isWhitePlayer ? 7 - selectedPosition.getRow() : selectedPosition.getRow();
        int selectedDisplayCol = isWhitePlayer ? selectedPosition.getCol() : 7 - selectedPosition.getCol();
        unhighlightSquare(selectedDisplayRow, selectedDisplayCol);
        selectedPosition = null;
    }

    private void resetHighlights() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessBoardButtons[i][j].setBackground((i + j) % 2 == 0 ? new Color(240, 217, 181) : new Color(118, 150, 86));
            }
        }
    }

    // ==== IV. NƯỚC ĐI & LUẬT CHƠI ====
    public void movePiece(Position from, Position to) {
        if (chessBoard.isValidMove(from, to)) {
            chessBoard.movePiece(from, to);
            updateBoard();

            if (chessBoard.isCheckmate(!isWhiteTurn)) {
                showMessage(isWhiteTurn ? "Trắng thắng!" : "Đen thắng!");
                newgame();
            } else {
                isWhiteTurn = !isWhiteTurn; // Đổi lượt
            }
        }
    }

    public void newgame() {
        chessBoard.resetBoard();
        updateBoard();
        isWhiteTurn = true;
    }

    public void updateNumberOfGame() {
        // Nếu cần đếm số ván thắng giữa 2 người, thêm ở đây
    }

    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = chessBoard.getPieceAt(fromRow, fromCol);
        if (piece != null) {
            if (piece.isValidMove(chessBoard, fromRow, fromCol, toRow, toCol)) {
                chessBoard.setPieceAt(toRow, toCol, piece);
                chessBoard.setPieceAt(fromRow, fromCol, null);
                updateBoard(); // Cập nhật lại bàn cờ
                return true;
            }
        }
        return false;
    }

    // ==== V. CHAT - NHẮN TIN ====
    public void addMessage(String message) {
        String temp = messageTextArea.getText();
        temp += competitor.getNickname() + ": " + message + "\n";
        messageTextArea.setText(temp);
        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
    }

    public void addVoiceMessage(String message) {
        String temp = messageTextArea.getText();
        temp += competitor.getNickname() + " " + message + "\n";
        messageTextArea.setText(temp);
        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
    }

    // ==== VI. THOẠI - MIC/LOA ====
    public void voiceOpenMic() {

        Thread sendThread = new Thread() {

            @Override
            public void run() {
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
                TargetDataLine microphone;
                try {
                    microphone = AudioSystem.getTargetDataLine(format);

                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                    microphone = (TargetDataLine) AudioSystem.getLine(info);
                    microphone.open(format);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int numBytesRead;
                    int CHUNK_SIZE = 1024;
                    byte[] data = new byte[microphone.getBufferSize() / 5];
                    microphone.start();

                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);

                    int port = 5555;

                    InetAddress address = InetAddress.getByName(competitorIP);
                    DatagramSocket socket = new DatagramSocket();
                    byte[] buffer = new byte[1024];
                    isSending = true;
                    while (isSending) {
                        numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                        out.write(data, 0, numBytesRead);
                        DatagramPacket request = new DatagramPacket(data, numBytesRead, address, port);
                        socket.send(request);

                    }
                    out.close();
                    socket.close();
                    microphone.close();
                } catch (LineUnavailableException | IOException e) {
                    e.printStackTrace();
                }
            }

        };
        sendThread.start();

    }

    public void voiceCloseMic() {
        isSending = false;
    }

    public void voiceListening() {
        //                    microphone = AudioSystem.getTargetDataLine(format);
        //                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        //                    microphone = (TargetDataLine) AudioSystem.getLine(info);
        //                    microphone.open(format);
        //                    microphone.start();
        Thread listenThread = new Thread() {
            @Override
            public void run() {
                try {
                    AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
                    SourceDataLine speakers;
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                    speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    speakers.open(format);
                    speakers.start();
                    try {
                        DatagramSocket serverSocket = new DatagramSocket(5555);
                        isListening = true;
                        while (isListening) {
                            byte[] buffer = new byte[1024];
                            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                            serverSocket.receive(response);
                            speakers.write(response.getData(), 0, response.getData().length);
                            jProgressBar1.setValue((int) volumeRMS(response.getData()));
                        }
                        speakers.close();
                        serverSocket.close();
                    } catch (SocketTimeoutException ex) {
                        System.out.println("Timeout error: " + ex.getMessage());
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        System.out.println("Client error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } catch (LineUnavailableException ex) {
                    ex.printStackTrace();
                }
            }

        };
        listenThread.start();
    }

    public void voiceStopListening() {
        isListening = false;
    }

    public double volumeRMS(byte[] raw) {
        double sum = 0d;
        if (raw.length == 0) {
            return sum;
        } else {
            for (byte b : raw) {
                sum += b;
            }
        }
        double average = sum / raw.length;

        double sumMeanSquare = 0d;
        for (byte b : raw) {
            sumMeanSquare += Math.pow(b - average, 2d);
        }
        double averageMeanSquare = sumMeanSquare / raw.length;
        return Math.sqrt(averageMeanSquare);
    }

    // ==== VII. ĐỒNG HỒ ====
    public void startTimer() {
        countDownLabel.setVisible(true);
        second = 60;
        minute = 0;
        timer.start();
    }

    public void stopTimer() {
        timer.stop();
    }

    // ==== VIII. TỈ SỐ & KẾT THÚC GAME ====
    public void increaseWinMatchToUser() {
        Client.user.setNumberOfWin(Client.user.getNumberOfWin() + 1);
        playerNumberOfWinValue.setText("" + Client.user.getNumberOfWin());
        userWin++;
        scoreLabel.setText("Tỉ số: " + userWin + "-" + competitorWin);
        String tmp = messageTextArea.getText();
        tmp += "--Bạn đã thắng, tỉ số hiện tại là " + userWin + "-" + competitorWin + "--\n";
        messageTextArea.setText(tmp);
        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
    }

    public void increaseWinMatchToCompetitor() {
        competitor.setNumberOfWin(competitor.getNumberOfWin() + 1);
        competitorNumberOfWinValue.setText("" + competitor.getNumberOfWin());
        competitorWin++;
        scoreLabel.setText("Tỉ số: " + userWin + "-" + competitorWin);
        String tmp = messageTextArea.getText();
        tmp += "--Bạn đã thua, tỉ số hiện tại là " + userWin + "-" + competitorWin + "--\n";
        messageTextArea.setText(tmp);
        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
    }

    public void displayDrawGame() {
        String tmp = messageTextArea.getText();
        tmp += "--Ván chơi hòa--\n";
        messageTextArea.setText(tmp);
        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
    }

    public void displayUserTurn() {
        countDownLabel.setVisible(false);
        competitorTurnLabel.setVisible(false);
        competitorPositionLabel.setVisible(false);
        playerTurnLabel.setVisible(true);
        drawRequestButton.setVisible(true);
        playerCurrentPositionLabel.setVisible(true);
    }

    public void displayCompetitorTurn() {
        countDownLabel.setVisible(false);
        competitorTurnLabel.setVisible(true);
        competitorPositionLabel.setVisible(true);
        playerTurnLabel.setVisible(false);
        drawRequestButton.setVisible(false);
        playerCurrentPositionLabel.setVisible(false);
    }

    public void displayDrawRefuse() {
        JOptionPane.showMessageDialog(rootPane, "Đối thủ không chấp nhận hòa, mời bạn chơi tiếp");
        timer.start();
    }

    public void showDrawRequest() {
        int res = JOptionPane.showConfirmDialog(rootPane, "Đối thử muốn cầu hóa ván này, bạn đồng ý chứ", "Yêu cầu cầu hòa", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            try {
                timer.stop();
                Client.socketHandle.write("draw-confirm,");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rootPane, ex.getMessage());
            }
        } else {
            try {
                Client.socketHandle.write("draw-refuse,");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rootPane, ex.getMessage());
            }
        }
    }

    public void addCompetitorChessMove(int fromX, int fromY, int toX, int toY) {
        ChessPiece piece = chessBoard.getPieceAt(fromX, fromY);
        if (piece != null) {
            chessBoard.setPieceAt(toX, toY, piece);
            chessBoard.setPieceAt(fromX, fromY, null);
        }
        updateBoardUI();
        displayUserTurn(); // Hiển thị lượt người chơi
        startTimer();      // Bắt đầu đếm ngược
    }

    private void updateScore(String winner) {
        if (winner.equals("Trắng")) {
            userWin++;
        } else {
            opponentWin++;
        }

        scoreLabel.setText("Tỉ số: " + userWin + "-" + opponentWin);
    }

    // ==== IX. THOÁT GAME ====
    public void exitGame() {
        try {
            timer.stop();
            voiceCloseMic();
            voiceStopListening();
            Client.socketHandle.write("left-room,");
            Client.closeAllViews();
            Client.openView(Client.View.HOMEPAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
        }
        Client.closeAllViews();
        Client.openView(Client.View.HOMEPAGE);
    }

    public void stopAllThread() {
        timer.stop();
        voiceCloseMic();
        voiceStopListening();
    }

    // ==== X. ÂM THANH ====
    public void playSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("assets/sound/click.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace(); //*
        }
    }

    public void playSound1() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("assets/sound/1click.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace(); //*
        }
    }

    public void playSound2() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("assets/sound/win.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }

    // ==== XI. UI HỖ TRỢ ====
    private ImageIcon getChessPieceIcon(ChessPiece piece) {
        if (piece == null) {
            return null;
        }

        // Luôn dùng màu thật của quân cờ
        String color = piece.isWhite() ? "white" : "black";

        String name = piece.getClass().getSimpleName().toLowerCase();
        String path = "src/Resources/" + name + "_" + color + ".png";

        try {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
                Image scaledImage = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } else {
                System.err.println("❌ Không tìm thấy hình ảnh: " + path);
            }
        } catch (Exception e) {
            System.err.println("❗ Lỗi khi tải ảnh: " + e.getMessage());
        }
        return null;
    }

    private String getPieceImageFileName(ChessPiece piece) {
        String pieceType = piece.getClass().getSimpleName().toLowerCase();
        String color = piece.isWhite() ? "white" : "black";
        return "/Resources/" + pieceType + "_" + color + ".png"; // Đường dẫn đến hình ảnh quân cờ
    }

    private void resetSquareColor(int row, int col) {
        squares[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
    }

    private void highlightSelectedSquare(int row, int col) {
        if (row == selectedRow && col == selectedCol) {
            squares[row][col].setBackground(Color.CYAN);
        }
    }

    private void updateChessBoard() {
        Component[] squares = gamePanel.getComponents();
        for (int i = 0; i < squares.length; i++) {
            JButton square = (JButton) squares[i];
            int row = i / 8;
            int col = i % 8;

            ChessPiece piece = chessBoard.getPieceAt(row, col);
            square.setIcon(piece != null
                    ? new ImageIcon("Resources/" + piece.getClass().getSimpleName()
                            + (piece.isWhite() ? "_white.png" : "_black.png")) : null);
        }
    }

    private void handleButtonClick(JButton button) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (chessBoardButtons[i][j] == button) {
                    Position clickedPosition = new Position(i, j);
                    ChessPiece clickedPiece = chessBoard.getPieceAt(i, j);

                    if (selectedPosition == null) {
                        // Chưa chọn quân cờ: chọn quân của người chơi hiện tại
                        boolean isMyTurn = isUserWhite() == chessBoard.isWhiteTurn();
                        if (clickedPiece != null && clickedPiece.isWhite() == isUserWhite() && isMyTurn) {
                            selectedPosition = clickedPosition;
                            highlightSelectedButton(i, j);
                        }
                    } else {
                        // Đã chọn quân cờ: thử di chuyển
                        movePiece(selectedPosition, clickedPosition);
                        selectedPosition = null;
                        clearHighlights();
                    }
                    return;
                }
            }
        }
    }

    private void initializePieces() {
        // Đặt quân Trắng
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Pawn(true, 6, col);
        }
        board[7][0] = new Rook(true, 7, 0);
        board[7][7] = new Rook(true, 7, 7);
        board[7][1] = new Knight(true, 7, 1);
        board[7][6] = new Knight(true, 7, 6);
        board[7][2] = new Bishop(true, 7, 2);
        board[7][5] = new Bishop(true, 7, 5);
        board[7][3] = new Queen(true, 7, 3);
        board[7][4] = new King(true, 7, 4);

        // Đặt quân Đen
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn(false, 1, col);
        }
        board[0][0] = new Rook(false, 0, 0);
        board[0][7] = new Rook(false, 0, 7);
        board[0][1] = new Knight(false, 0, 1);
        board[0][6] = new Knight(false, 0, 6);
        board[0][2] = new Bishop(false, 0, 2);
        board[0][5] = new Bishop(false, 0, 5);
        board[0][3] = new Queen(false, 0, 3);
        board[0][4] = new King(false, 0, 4);
    }

    public ChessBoard getChessBoard() {
        return chessBoard;
    }

    private void resetGame() {
        chessBoard = new ChessBoard(whiteStarts);
        selectedRow = -1;
        selectedCol = -1;
        initChessBoard();
        updateBoard();
    }

    private int getMax(byte[] bytes) {
        int max = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            if (bytes[i] > max) {
                max = bytes[i];
            }
        }
        return max;
    }

    // ==== XII. initComponents ====
    private void onSquareClick(int row, int col) {
        ChessPiece clicked = chessBoard.getPieceAt(row, col);
        System.out.println("🟡 Click: " + row + "," + col + " | piece = " + (clicked == null ? "null" : clicked.getClass().getSimpleName()));

        if (selectedPosition == null) {
            if (clicked != null && clicked.isWhite() == isWhiteTurn) {
                System.out.println("✅ Chọn quân cờ: " + clicked.getClass().getSimpleName());
                selectedPosition = new Position(row, col);
                highlightSquare(row, col);
            } else {
                System.out.println("❌ Không chọn được quân: " + (clicked == null ? "null" : clicked.getClass().getSimpleName()));
            }
        } else {
            boolean moved = chessBoard.movePiece(
                    selectedPosition.getRow(), selectedPosition.getCol(), row, col
            );
            if (moved) {
                isWhiteTurn = !isWhiteTurn;
                updateBoardUI();
                System.out.println("✅ Di chuyển thành công!");
            } else {
                System.out.println("❌ Di chuyển KHÔNG hợp lệ!");
            }
            unhighlightSquare(selectedPosition.getRow(), selectedPosition.getCol());
            selectedPosition = null;
        }
    }

    private void highlightValidMoves(Position pos) {
        resetHighlights();
        List<Position> moves = chessBoard.getValidMovesFor(pos.getRow(), pos.getCol());
        for (Position move : moves) {
            int displayRow = isWhitePlayer ? 7 - move.getRow() : move.getRow();
            int displayCol = isWhitePlayer ? move.getCol() : 7 - move.getCol();
            chessBoardButtons[displayRow][displayCol].setBackground(Color.YELLOW);
        }
    }

    private void highlightSquare(int row, int col) {
        int displayRow = isWhitePlayer ? 7 - row : row;
        int displayCol = isWhitePlayer ? col : 7 - col;
        chessBoardButtons[displayRow][displayCol].setBackground(new Color(173, 216, 230)); // xanh nhạt
    }

    private void unhighlightSquare(int displayRow, int displayCol) {
        chessBoardButtons[displayRow][displayCol].setBackground((displayRow + displayCol) % 2 == 0
                ? new Color(240, 217, 181)
                : new Color(118, 150, 86));
    }

    public void updateBoardUI() {
        for (int displayRow = 0; displayRow < 8; displayRow++) {
            for (int displayCol = 0; displayCol < 8; displayCol++) {
                int actualRow = isWhitePlayer ? 7 - displayRow : displayRow;
                int actualCol = isWhitePlayer ? displayCol : 7 - displayCol;
                ChessPiece piece = chessBoard.getPieceAt(actualRow, actualCol);
                chessBoardButtons[displayRow][displayCol].setIcon(getChessPieceIcon(piece));
            }
        }
        
//        if (selectedPosition != null) {
//        int selRow = selectedPosition.getRow();
//        int selCol = selectedPosition.getCol();
//        int displaySelRow = 7 - selRow;
//
//        chessBoardButtons[displaySelRow][selCol].setBackground(Color.YELLOW);
//
//        List<Position> moves = chessBoard.getLegalMoves(selRow, selCol);
//        for (Position move : moves) {
//            int moveRow = move.getRow();
//            int moveCol = move.getCol();
//            int displayMoveRow = 7 - moveRow;
//            chessBoardButtons[displayMoveRow][moveCol].setBackground(new Color(144, 238, 144));
//        }
//    }
    }

    public void updateBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int displayRow = isWhitePlayer ? row : 7 - row;
                int displayCol = isWhitePlayer ? col : 7 - col;
                squares[displayRow][displayCol] = new JButton();
                squares[displayRow][displayCol].setIcon(getPieceImage(board[row][col]));
                resetSquareColor(displayRow, displayCol);
                highlightSelectedSquare(displayRow, displayCol);
            }
        }
    }

    private int toDisplayRow(int boardRow) {
        return isWhitePlayer ? 7 - boardRow : boardRow;
    }

    private int toDisplayCol(int boardCol) {
        return isWhitePlayer ? boardCol : 7 - boardCol;
    }

    private int toBoardRow(int displayRow) {
        return isWhitePlayer ? 7 - displayRow : displayRow;
    }

    private int toBoardCol(int displayCol) {
        return isWhitePlayer ? displayCol : 7 - displayCol;
    }

    private void highlightSelectedButton(int i, int j) {
        chessBoardButtons[i][j].setBackground(java.awt.Color.YELLOW);
    }

    private void clearHighlights() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessBoardButtons[i][j].setBackground((i + j) % 2 == 0 ? new Color(240, 217, 181) : new Color(118, 150, 86));
            }
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
        scoreLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        drawRequestButton = new javax.swing.JButton();
        competitorTurnLabel = new javax.swing.JLabel();
        playerCurrentPositionLabel = new javax.swing.JLabel();
        playerNicknameValue = new javax.swing.JLabel();
        competitorPositionLabel = new javax.swing.JLabel();
        playerNumberOfGameValue = new javax.swing.JLabel();
        playerNumberOfWinValue = new javax.swing.JLabel();
        competitorNicknameValue = new javax.swing.JLabel();
        competotorNumberOfGameValue = new javax.swing.JLabel();
        competitorNumberOfWinValue = new javax.swing.JLabel();
        countDownLabel = new javax.swing.JLabel();
        playerNumberOfWinLabel = new javax.swing.JLabel();
        playerTurnLabel = new javax.swing.JLabel();
        playerNicknameLabel = new javax.swing.JLabel();
        playerNumberOfGameLabel = new javax.swing.JLabel();
        competitorNumberOfWinLabel = new javax.swing.JLabel();
        competitorNicknameLabel = new javax.swing.JLabel();
        competotorNumberOfGameLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        playerLabel = new javax.swing.JLabel();
        playerButtonImage = new javax.swing.JLabel();
        competotorButtonImage = new javax.swing.JButton();
        vsIcon = new javax.swing.JLabel();
        gamePanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        roomNameLabel = new javax.swing.JLabel();
        microphoneStatusButton = new javax.swing.JButton();
        speakerStatusButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageTextArea = new javax.swing.JTextArea();
        messageTextField = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
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
        competitorLabel.setText("Đối thủ");

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

        scoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        scoreLabel.setText("Tỉ số:  0-0");

        jPanel5.setBackground(new java.awt.Color(102, 102, 102));

        drawRequestButton.setBackground(new java.awt.Color(102, 102, 102));
        drawRequestButton.setForeground(new java.awt.Color(255, 255, 255));
        drawRequestButton.setText("Cầu hòa");
        drawRequestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawRequestButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(112, 112, 112)
                .addComponent(drawRequestButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(119, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(drawRequestButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        competitorTurnLabel.setForeground(new java.awt.Color(0, 0, 204));
        competitorTurnLabel.setText("Đến lượt đối thủ");

        playerCurrentPositionLabel.setText("x/o");

        playerNicknameValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNicknameValue.setForeground(new java.awt.Color(255, 255, 255));
        playerNicknameValue.setText("{nickname}");

        competitorPositionLabel.setText("x/o");

        playerNumberOfGameValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfGameValue.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfGameValue.setText("{sovanchoi}");

        playerNumberOfWinValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfWinValue.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfWinValue.setText("{sovanthang}");

        competitorNicknameValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNicknameValue.setForeground(new java.awt.Color(255, 255, 255));
        competitorNicknameValue.setText("{nickname}");

        competotorNumberOfGameValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competotorNumberOfGameValue.setForeground(new java.awt.Color(255, 255, 255));
        competotorNumberOfGameValue.setText("{sovanchoi}");

        competitorNumberOfWinValue.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNumberOfWinValue.setForeground(new java.awt.Color(255, 255, 255));
        competitorNumberOfWinValue.setText("{sovanthang}");

        countDownLabel.setForeground(new java.awt.Color(255, 0, 0));
        countDownLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        countDownLabel.setText("Thời gian:00:20");

        playerNumberOfWinLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfWinLabel.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfWinLabel.setText("Số ván thắng");

        playerTurnLabel.setForeground(new java.awt.Color(255, 0, 0));
        playerTurnLabel.setText("Đến lượt bạn");

        playerNicknameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNicknameLabel.setForeground(new java.awt.Color(255, 255, 255));
        playerNicknameLabel.setText("Nickname");

        playerNumberOfGameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        playerNumberOfGameLabel.setForeground(new java.awt.Color(255, 255, 255));
        playerNumberOfGameLabel.setText("Số ván chơi");

        competitorNumberOfWinLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNumberOfWinLabel.setForeground(new java.awt.Color(255, 255, 255));
        competitorNumberOfWinLabel.setText("Số ván thắng");

        competitorNicknameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competitorNicknameLabel.setForeground(new java.awt.Color(255, 255, 255));
        competitorNicknameLabel.setText("Nickname");

        competotorNumberOfGameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        competotorNumberOfGameLabel.setForeground(new java.awt.Color(255, 255, 255));
        competotorNumberOfGameLabel.setText("Số ván chơi");

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
                .addContainerGap(99, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(playerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        playerButtonImage.setBackground(new java.awt.Color(102, 102, 102));

        competotorButtonImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                competotorButtonImageActionPerformed(evt);
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
                        .addComponent(playerTurnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(countDownLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(competitorTurnLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(playerCurrentPositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(scoreLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addComponent(competitorPositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(playerButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
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
                        .addComponent(competotorButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(competitorNicknameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(competitorNicknameValue, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(competotorNumberOfGameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(competitorNumberOfWinLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(27, 27, 27)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(competotorNumberOfGameValue, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(competitorNumberOfWinValue, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                            .addComponent(playerNumberOfGameValue)))
                    .addComponent(playerButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(playerNumberOfWinLabel)
                        .addComponent(playerNumberOfWinValue))
                    .addComponent(vsIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(competitorNicknameLabel)
                            .addComponent(competitorNicknameValue))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(competotorNumberOfGameLabel)
                            .addComponent(competotorNumberOfGameValue)))
                    .addComponent(competotorButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(competitorNumberOfWinLabel)
                    .addComponent(competitorNumberOfWinValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(competitorPositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scoreLabel)
                    .addComponent(playerCurrentPositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(countDownLabel)
                    .addComponent(competitorTurnLabel)
                    .addComponent(playerTurnLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        gamePanel.setBackground(new java.awt.Color(102, 102, 102));
        gamePanel.setMaximumSize(new java.awt.Dimension(512, 512));
        gamePanel.setMinimumSize(new java.awt.Dimension(512, 512));
        gamePanel.setPreferredSize(new java.awt.Dimension(512, 512));

        javax.swing.GroupLayout gamePanelLayout = new javax.swing.GroupLayout(gamePanel);
        gamePanel.setLayout(gamePanelLayout);
        gamePanelLayout.setHorizontalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        gamePanelLayout.setVerticalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        roomNameLabel.setFont(new java.awt.Font("Candara", 1, 14)); // NOI18N
        roomNameLabel.setText("{Tên Phòng}");

        microphoneStatusButton.setToolTipText("Bật mic để nói chuyện cùng nhau");
        microphoneStatusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microphoneStatusButtonActionPerformed(evt);
            }
        });

        speakerStatusButton.setToolTipText("Âm thanh trò chuyện đang tắt");
        speakerStatusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speakerStatusButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(roomNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 353, Short.MAX_VALUE)
                .addComponent(microphoneStatusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(speakerStatusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(roomNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(speakerStatusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(microphoneStatusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        messageTextArea.setColumns(20);
        messageTextArea.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        messageTextArea.setRows(5);
        jScrollPane1.setViewportView(messageTextArea);

        messageTextField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        messageTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                messageTextFieldKeyPressed(evt);
            }
        });

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(messageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(messageTextField)
                            .addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7))
        );

        //for(int i=0; i<5; i++){
            //    for(int j=0;j<5;j++){
                //        gamePanel.add(button[i][j]);
                //    }
            //}

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newGameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newGameMenuItemActionPerformed
        JOptionPane.showMessageDialog(rootPane, "Thông báo", "Tính năng đang được phát triển", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_newGameMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exitGame();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void drawRequestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawRequestButtonActionPerformed

        try {
            int res = JOptionPane.showConfirmDialog(rootPane, "Bạn có thực sự muốn cầu hòa ván chơi này", "Yêu cầu cầu hòa", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                Client.socketHandle.write("draw-request,");
                timer.stop();
                Client.openView(Client.View.GAME_NOTICE, "Yêu cầu hòa", "Đang chờ phản hồi từ đối thủ");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
        } //*
    }//GEN-LAST:event_drawRequestButtonActionPerformed

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

    private void competotorButtonImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_competotorButtonImageActionPerformed

        Client.openView(Client.View.COMPETITOR_INFO, competitor); //*

    }//GEN-LAST:event_competotorButtonImageActionPerformed

    private void microphoneStatusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_microphoneStatusButtonActionPerformed
        if (isSending) {
            try {
                Client.socketHandle.write("voice-message,close-mic");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rootPane, "Có lỗi xảy ra");
            }
            microphoneStatusButton.setIcon(new ImageIcon("assets/game/mute.png"));
            voiceCloseMic();
            microphoneStatusButton.setToolTipText("Mic đang tắt");

        } else {
            try {
                Client.socketHandle.write("voice-message,open-mic");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rootPane, "Có lỗi xảy ra");
            }
            microphoneStatusButton.setIcon(new ImageIcon("assets/game/88634.png"));
            voiceOpenMic();
            microphoneStatusButton.setToolTipText("Mic đang bật");
        } //*
    }//GEN-LAST:event_microphoneStatusButtonActionPerformed

    private void speakerStatusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerStatusButtonActionPerformed
        if (isListening) {
            try {
                Client.socketHandle.write("voice-message,close-speaker");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rootPane, "Có lỗi xảy ra");
            }
            speakerStatusButton.setIcon(new ImageIcon("assets/game/mutespeaker.png"));
            voiceStopListening();
            speakerStatusButton.setToolTipText("Âm thanh trò chuyện đang tắt");
        } else {
            try {
                Client.socketHandle.write("voice-message,open-speaker");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rootPane, "Có lỗi xảy ra");
            }
            voiceListening();
            speakerStatusButton.setIcon(new ImageIcon("assets/game/speaker.png"));
            speakerStatusButton.setToolTipText("Âm thanh trò chuyện đang bật");
        } //*
    }//GEN-LAST:event_speakerStatusButtonActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        try {
            if (messageTextField.getText().isEmpty()) {
                throw new Exception("Vui lòng nhập nội dung tin nhắn");
            }
            String temp = messageTextArea.getText();
            temp += "Tôi: " + messageTextField.getText() + "\n";
            messageTextArea.setText(temp);
            Client.socketHandle.write("chat," + messageTextField.getText());
            messageTextField.setText("");
            messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
        }
    }//GEN-LAST:event_sendButtonActionPerformed


    private void messageTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageTextFieldKeyPressed
        if (evt.getKeyCode() == 10) {
            try {
                if (messageTextField.getText().isEmpty()) {
                    return;
                }
                String temp = messageTextArea.getText();
                temp += "Tôi: " + messageTextField.getText() + "\n";
                messageTextArea.setText(temp);
                Client.socketHandle.write("chat," + messageTextField.getText());
                messageTextField.setText("");
                messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rootPane, ex.getMessage());
            }
        } //*
    }//GEN-LAST:event_messageTextFieldKeyPressed

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
        // 👂 You may add sound effect here
        Toolkit.getDefaultToolkit().beep();
    }

    public void playWinSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("assets/sound/win.wav").getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error with playing win sound.");
            ex.printStackTrace();
        }
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
        String pieceType = piece.getClass().getSimpleName().toLowerCase();
        String path = "/Resources/" + "_" + pieceType + color + ".png";

        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.out.println("Không tìm thấy hình ảnh: " + path);
            return null;
        }
    }
    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel competitorLabel;
    private javax.swing.JLabel competitorNicknameLabel;
    private javax.swing.JLabel competitorNicknameValue;
    private javax.swing.JLabel competitorNumberOfWinLabel;
    private javax.swing.JLabel competitorNumberOfWinValue;
    private javax.swing.JLabel competitorPositionLabel;
    private javax.swing.JLabel competitorTurnLabel;
    private javax.swing.JButton competotorButtonImage;
    private javax.swing.JLabel competotorNumberOfGameLabel;
    private javax.swing.JLabel competotorNumberOfGameValue;
    private javax.swing.JLabel countDownLabel;
    private javax.swing.JButton drawRequestButton;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPanel gamePanel;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenu mainMenu;
    private javax.swing.JTextArea messageTextArea;
    private javax.swing.JTextField messageTextField;
    private javax.swing.JButton microphoneStatusButton;
    private javax.swing.JMenuItem newGameMenuItem;
    private javax.swing.JLabel playerButtonImage;
    private javax.swing.JLabel playerCurrentPositionLabel;
    private javax.swing.JLabel playerLabel;
    private javax.swing.JLabel playerNicknameLabel;
    private javax.swing.JLabel playerNicknameValue;
    private javax.swing.JLabel playerNumberOfGameLabel;
    private javax.swing.JLabel playerNumberOfGameValue;
    private javax.swing.JLabel playerNumberOfWinLabel;
    private javax.swing.JLabel playerNumberOfWinValue;
    private javax.swing.JLabel playerTurnLabel;
    private javax.swing.JLabel roomNameLabel;
    private javax.swing.JLabel scoreLabel;
    private javax.swing.JButton sendButton;
    private javax.swing.JButton speakerStatusButton;
    private javax.swing.JLabel vsIcon;
    // End of variables declaration//GEN-END:variables

}
