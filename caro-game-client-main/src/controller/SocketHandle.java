package controller;

import model.User;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import model.chess.pieces.Bishop;
import model.chess.pieces.ChessPiece;
import model.chess.pieces.King;
import model.chess.pieces.Knight;
import model.chess.pieces.Pawn;
import model.chess.pieces.Queen;
import model.chess.pieces.Rook;
import view.RoomListFrm;

public class SocketHandle implements Runnable {

    private BufferedWriter outputWriter;
    private BufferedReader inputReader;
    private Socket socketOfClient;

    public List<User> getListUser(String[] message) {
        List<User> friend = new ArrayList<>();
        for (int i = 1; i < message.length; i += 4) {
            friend.add(new User(
                    Integer.parseInt(message[i]),
                    message[i + 1],
                    message[i + 2].equals("1"),
                    message[i + 3].equals("1")));
        }
        return friend;
    }

    public User getUserFromString(int start, String[] message) {
        return new User(
                Integer.parseInt(message[start]),
                message[start + 1],
                message[start + 2],
                message[start + 3],
                message[start + 4],
                Integer.parseInt(message[start + 5]),
                Integer.parseInt(message[start + 6]),
                Integer.parseInt(message[start + 7]),
                Integer.parseInt(message[start + 8])
        );
    }

    @Override
    public void run() {
        try {
            socketOfClient = new Socket("127.0.0.1", 7777);
            System.out.println("Kết nối thành công!");

            outputWriter = new BufferedWriter(new OutputStreamWriter(socketOfClient.getOutputStream()));
            inputReader = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));

            String message;
            while ((message = inputReader.readLine()) != null) {
                handleMessage(message);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối tới server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMessage(String message) throws IOException, InterruptedException {
        String[] messageSplit = message.split(",", -1);

        switch (messageSplit[0]) {
            case "login-success" -> {
                System.out.println("Đăng nhập thành công");
                Client.closeAllViews();
                Client.user = getUserFromString(1, messageSplit);
                Client.openView(Client.View.HOMEPAGE);
            }
            case "wrong-user" -> {
                Client.closeView(Client.View.GAME_NOTICE);
                Client.openView(Client.View.LOGIN, messageSplit[1], messageSplit[2]);
                Client.loginFrm.showError("Tài khoản hoặc mật khẩu không chính xác");
            }
            case "dupplicate-login" -> {
                Client.closeView(Client.View.GAME_NOTICE);
                Client.openView(Client.View.LOGIN, messageSplit[1], messageSplit[2]);
                Client.loginFrm.showError("Tài khoản đã đăng nhập ở nơi khác");
            }
            case "banned-user" -> {
                Client.closeView(Client.View.GAME_NOTICE);
                Client.openView(Client.View.LOGIN, messageSplit[1], messageSplit[2]);
                Client.loginFrm.showError("Tài khoản đã bị ban");
            }
            case "duplicate-username" -> {
                Client.closeAllViews();
                Client.openView(Client.View.REGISTER);
                JOptionPane.showMessageDialog(Client.registerFrm, "Tên tài khoản đã được người khác sử dụng");
            }
            case "room-fully" -> {
                Client.closeAllViews();
                Client.openView(Client.View.HOMEPAGE);
                JOptionPane.showMessageDialog(Client.homePageFrm, "Phòng chơi đã đủ 2 người chơi");
            }
            case "go-to-room" -> {
                int roomID = Integer.parseInt(messageSplit[1]);
                String competitorIP = messageSplit[2];
                int isStart = Integer.parseInt(messageSplit[3]);
                User competitor = getUserFromString(4, messageSplit);
                boolean isWhitePlayer = messageSplit[13].equals("1"); // ✅ Góc nhìn đúng

                Client.closeAllViews();
                Client.openView(Client.View.GAME_CLIENT, competitor, roomID, isStart, competitorIP, isWhitePlayer);
                Client.gameClientFrm.newgame();
            }

            case "your-created-room" -> {
                Client.closeAllViews();
                Client.openView(Client.View.WAITING_ROOM);
                Client.waitingRoomFrm.setRoomName(messageSplit[1]);
                if (messageSplit.length == 3) {
                    Client.waitingRoomFrm.setRoomPassword("Mật khẩu phòng: " + messageSplit[2]);
                }
            }
            case "duel-notice" -> {
                int res = JOptionPane.showConfirmDialog(Client.getVisibleJFrame(),
                        "Bạn nhận được lời thách đấu của " + messageSplit[2] + " (ID=" + messageSplit[1] + ")",
                        "Xác nhận thách đấu", JOptionPane.YES_NO_OPTION);
                String response = res == JOptionPane.YES_OPTION ? "agree-duel," : "disagree-duel,";
                write(response + messageSplit[1]);
            }
            case "chess-move" -> {
                int fromX = Integer.parseInt(messageSplit[1]);
                int fromY = Integer.parseInt(messageSplit[2]);
                int toX = Integer.parseInt(messageSplit[3]);
                int toY = Integer.parseInt(messageSplit[4]);
                String pieceType = messageSplit[5];
                boolean isWhite = messageSplit[6].equals("1");

                // Tạo lại quân cờ từ loại và màu
                ChessPiece piece = switch (pieceType) {
                    case "Pawn" ->
                        new Pawn(isWhite, fromX, fromY);
                    case "Rook" ->
                        new Rook(isWhite, fromX, fromY);
                    case "Knight" ->
                        new Knight(isWhite, fromX, fromY);
                    case "Bishop" ->
                        new Bishop(isWhite, fromX, fromY);
                    case "Queen" ->
                        new Queen(isWhite, fromX, fromY);
                    case "King" ->
                        new King(isWhite, fromX, fromY);
                    default ->
                        null;
                };

                if (piece != null) {
                    // Đặt quân vào đúng vị trí trên bàn cờ của B
                    Client.gameClientFrm.getChessBoard().setPieceAt(toX, toY, piece);
                    Client.gameClientFrm.getChessBoard().setPieceAt(fromX, fromY, null);

                    Client.gameClientFrm.getChessBoard().toggleTurn();

                    Client.gameClientFrm.updateBoardUI(); // Cập nhật giao diện
                    Client.gameClientFrm.displayUserTurn();
                    Client.gameClientFrm.startTimer();
                }
            }
            case "draw-request" ->
                Client.gameClientFrm.showDrawRequest();
            case "draw-game" -> {
                Client.closeView(Client.View.GAME_NOTICE);
                Client.openView(Client.View.GAME_NOTICE, "Ván chơi hòa", "Ván chơi mới đang được thiết lập");
                Client.gameClientFrm.displayDrawGame();
                Thread.sleep(4000);
                Client.gameClientFrm.updateNumberOfGame();
                Client.closeView(Client.View.GAME_NOTICE);
                Client.gameClientFrm.newgame();
            }
            case "competitor-time-out" -> {
                Client.gameClientFrm.increaseWinMatchToUser();
                Client.openView(Client.View.GAME_NOTICE, "Bạn đã thắng do đối thủ quá thời gian", "Đang thiết lập ván chơi mới");
                Thread.sleep(4000);
                Client.closeView(Client.View.GAME_NOTICE);
                Client.gameClientFrm.updateNumberOfGame();
                Client.gameClientFrm.newgame();
            }
            case "left-room" -> {
                Client.gameClientFrm.stopTimer();
                Client.closeAllViews();
                Client.openView(Client.View.GAME_NOTICE, "Đối thủ đã thoát khỏi phòng", "Đang trở về trang chủ");
                Thread.sleep(3000);
                Client.closeAllViews();
                Client.openView(Client.View.HOMEPAGE);
            }
            case "chat" ->
                Client.gameClientFrm.addMessage(messageSplit[1]);
            case "voice-message" -> {
                String msg = switch (messageSplit[1]) {
                    case "close-mic" ->
                        "đã tắt mic";
                    case "open-mic" ->
                        "đã bật mic";
                    case "close-speaker" ->
                        "đã tắt âm thanh cuộc trò chuyện";
                    case "open-speaker" ->
                        "đã bật âm thanh cuộc trò chuyện";
                    default ->
                        "";
                };
                Client.gameClientFrm.addVoiceMessage(msg);
            }
            case "banned-notice" -> {
                write("offline," + Client.user.getID());
                Client.closeAllViews();
                Client.openView(Client.View.LOGIN);
                JOptionPane.showMessageDialog(Client.loginFrm, messageSplit[1], "Bạn đã bị BAN", JOptionPane.WARNING_MESSAGE);
            }
            case "promotion" -> {
                int row = Integer.parseInt(messageSplit[1]);
                int col = Integer.parseInt(messageSplit[2]);
                String type = messageSplit[3];
                boolean isWhite = messageSplit[4].equals("1");

                ChessPiece newPiece = switch (type) {
                    case "Rook" ->
                        new Rook(isWhite, row, col);
                    case "Bishop" ->
                        new Bishop(isWhite, row, col);
                    case "Knight" ->
                        new Knight(isWhite, row, col);
                    default ->
                        new Queen(isWhite, row, col);
                };

                Client.gameClientFrm.getChessBoard().setPieceAt(row, col, newPiece);

                Client.gameClientFrm.updateBoardUI();
            }

            case "view-room-list" -> {
                Vector<String> listRoom = new Vector<>();
                Vector<String> listPassword = new Vector<>();

                System.out.println("DỮ LIỆU NHẬN: " + message);
                System.out.println("Tổng phần tử tách ra: " + messageSplit.length);

                for (int i = 1; i + 2 < messageSplit.length; i += 3) {
                    String id = messageSplit[i];
                    String name = messageSplit[i + 1];
                    String password = messageSplit[i + 2];

                    listRoom.add("Phòng " + id + ": " + name);
                    listPassword.add(password.equals("") ? " " : password);
                }

                Client.roomListFrm.updateRoomList(listRoom, listPassword);
            }
            case "return-get-rank-charts" -> {
                List<User> userList = new ArrayList<>();
                for (int i = 1; i + 8 < messageSplit.length; i += 9) {
                    User user = new User(
                            Integer.parseInt(messageSplit[i]),
                            messageSplit[i + 1],
                            messageSplit[i + 2],
                            messageSplit[i + 3],
                            messageSplit[i + 4],
                            Integer.parseInt(messageSplit[i + 5]),
                            Integer.parseInt(messageSplit[i + 6]),
                            Integer.parseInt(messageSplit[i + 7]),
                            Integer.parseInt(messageSplit[i + 8])
                    );
                    userList.add(user);
                }

                // Gọi cập nhật bảng xếp hạng (nếu có view RankFrm)
                Client.rankFrm.updateRankList(userList);
            }

        }
    }

    public void write(String message) throws IOException {
        if (outputWriter != null) {
            outputWriter.write(message);
            outputWriter.newLine();
            outputWriter.flush();
        }
    }

    public Socket getSocketOfClient() {
        return socketOfClient;
    }
}
