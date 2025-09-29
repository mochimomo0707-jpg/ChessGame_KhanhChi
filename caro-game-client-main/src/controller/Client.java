package controller;

import controller.SocketHandle;
import model.User;
import view.*;

import javax.swing.*;

public class Client {

    public static User user;
    public static LoginFrm loginFrm;
    public static RegisterFrm registerFrm;
    public static HomePageFrm homePageFrm;
    public static RoomListFrm roomListFrm;
    public static FriendListFrm friendListFrm;
    public static FindRoomFrm findRoomFrm;
    public static WaitingRoomFrm waitingRoomFrm;
    public static GameClientFrm1 gameClientFrm;
    public static CreateRoomPasswordFrm createRoomPasswordFrm;
    public static JoinRoomPasswordFrm joinRoomPasswordFrm;
    public static CompetitorInfoFrm competitorInfoFrm;
    public static RankFrm rankFrm;
    public static GameNoticeFrm gameNoticeFrm;
    public static FriendRequestFrm friendRequestFrm;
    public static GameAIFrm1 gameAIFrm;
    public static RoomNameFrm roomNameFrm;
    public static SocketHandle socketHandle;

    public Client() {
    }

    public static JFrame getVisibleJFrame() {
        if (roomListFrm != null && roomListFrm.isVisible()) {
            return roomListFrm;
        }
        if (friendListFrm != null && friendListFrm.isVisible()) {
            return friendListFrm;
        }
        if (createRoomPasswordFrm != null && createRoomPasswordFrm.isVisible()) {
            return createRoomPasswordFrm;
        }
        if (joinRoomPasswordFrm != null && joinRoomPasswordFrm.isVisible()) {
            return joinRoomPasswordFrm;
        }
        if (rankFrm != null && rankFrm.isVisible()) {
            return rankFrm;
        }
        return homePageFrm;
    }

    public static void openView(View viewName) {
        if (viewName != null) {
            switch (viewName) {
                case LOGIN -> {
                    loginFrm = new LoginFrm();
                    loginFrm.setVisible(true);
                }
                case REGISTER -> {
                    registerFrm = new RegisterFrm();
                    registerFrm.setVisible(true);
                }
                case HOMEPAGE -> {
                    homePageFrm = new HomePageFrm();
                    homePageFrm.setVisible(true);
                }
                case ROOM_LIST -> {
                    roomListFrm = new RoomListFrm();
                    roomListFrm.setVisible(true);
                }
                case FRIEND_LIST -> {
                    friendListFrm = new FriendListFrm();
                    friendListFrm.setVisible(true);
                }
                case FIND_ROOM -> {
                    findRoomFrm = new FindRoomFrm();
                    findRoomFrm.setVisible(true);
                }
                case WAITING_ROOM -> {
                    waitingRoomFrm = new WaitingRoomFrm();
                    waitingRoomFrm.setVisible(true);
                }
                case CREATE_ROOM_PASSWORD -> {
                    createRoomPasswordFrm = new CreateRoomPasswordFrm();
                    createRoomPasswordFrm.setVisible(true);
                }
                case RANK -> {
                    rankFrm = new RankFrm();
                    rankFrm.setVisible(true);
                }
                case GAME_AI -> {
                    gameAIFrm = new GameAIFrm1();
                    gameAIFrm.setVisible(true);
                }
                case ROOM_NAME_FRM -> {
                    roomNameFrm = new RoomNameFrm();
                    roomNameFrm.setVisible(true);
                }
            }
        }
    }

    public static void openView(View viewName, int arg1, String arg2) {
        if (viewName == View.JOIN_ROOM_PASSWORD) {
            joinRoomPasswordFrm = new JoinRoomPasswordFrm(arg1, arg2);
            joinRoomPasswordFrm.setVisible(true);
        } else if (viewName == View.FRIEND_REQUEST) {
            friendRequestFrm = new FriendRequestFrm(arg1, arg2);
            friendRequestFrm.setVisible(true);
        }
    }

    public static void openView(View viewName, User competitor, int room_ID, int isStart, String competitorIP, boolean isWhitePlayer) {
        if (viewName == View.GAME_CLIENT) {
            gameClientFrm = new GameClientFrm1(competitor, room_ID, isStart, competitorIP, isWhitePlayer);
            gameClientFrm.setVisible(true);
        }
    }

    public static void openView(View viewName, User user) {
        if (viewName == View.COMPETITOR_INFO) {
            competitorInfoFrm = new CompetitorInfoFrm(user);
            competitorInfoFrm.setVisible(true);
        }
    }

    public static void openView(View viewName, String arg1, String arg2) {
        if (viewName == View.GAME_NOTICE) {
            gameNoticeFrm = new GameNoticeFrm(arg1, arg2);
            gameNoticeFrm.setVisible(true);
        } else if (viewName == View.LOGIN) {
            loginFrm = new LoginFrm(arg1, arg2);
            loginFrm.setVisible(true);
        }
    }

    public static void closeView(View viewName) {
        if (viewName != null) {
            switch (viewName) {
                case LOGIN ->
                    loginFrm.dispose();
                case REGISTER ->
                    registerFrm.dispose();
                case HOMEPAGE ->
                    homePageFrm.dispose();
                case ROOM_LIST ->
                    roomListFrm.dispose();
                case FRIEND_LIST -> {
                    friendListFrm.stopAllThread();
                    friendListFrm.dispose();
                }
                case FIND_ROOM -> {
                    findRoomFrm.stopAllThread();
                    findRoomFrm.dispose();
                }
                case WAITING_ROOM ->
                    waitingRoomFrm.dispose();
                case GAME_CLIENT -> {
                    gameClientFrm.stopAllThread();
                    gameClientFrm.dispose();
                }
                case CREATE_ROOM_PASSWORD ->
                    createRoomPasswordFrm.dispose();
                case JOIN_ROOM_PASSWORD ->
                    joinRoomPasswordFrm.dispose();
                case COMPETITOR_INFO ->
                    competitorInfoFrm.dispose();
                case RANK ->
                    rankFrm.dispose();
                case GAME_NOTICE ->
                    gameNoticeFrm.dispose();
                case FRIEND_REQUEST ->
                    friendRequestFrm.dispose();
                case GAME_AI ->
                    gameAIFrm.dispose();
                case ROOM_NAME_FRM ->
                    roomNameFrm.dispose();
            }
        }
    }

    public static void closeAllViews() {
        if (loginFrm != null) {
            loginFrm.dispose();
        }
        if (registerFrm != null) {
            registerFrm.dispose();
        }
        if (homePageFrm != null) {
            homePageFrm.dispose();
        }
        if (roomListFrm != null) {
            roomListFrm.dispose();
        }
        if (friendListFrm != null) {
            friendListFrm.stopAllThread();
            friendListFrm.dispose();
        }
        if (findRoomFrm != null) {
            findRoomFrm.stopAllThread();
            findRoomFrm.dispose();
        }
        if (waitingRoomFrm != null) {
            waitingRoomFrm.dispose();
        }
        if (gameClientFrm != null) {
            gameClientFrm.stopAllThread();
            gameClientFrm.dispose();
        }
        if (createRoomPasswordFrm != null) {
            createRoomPasswordFrm.dispose();
        }
        if (joinRoomPasswordFrm != null) {
            joinRoomPasswordFrm.dispose();
        }
        if (competitorInfoFrm != null) {
            competitorInfoFrm.dispose();
        }
        if (rankFrm != null) {
            rankFrm.dispose();
        }
        if (gameNoticeFrm != null) {
            gameNoticeFrm.dispose();
        }
        if (friendRequestFrm != null) {
            friendRequestFrm.dispose();
        }
        if (gameAIFrm != null) {
            gameAIFrm.dispose();
        }
        if (roomNameFrm != null) {
            roomNameFrm.dispose();
        }
    }

    public static void main(String[] args) {
        new Client().initView();
    }

    public void initView() {
        loginFrm = new LoginFrm();
        loginFrm.setVisible(true);

        socketHandle = new SocketHandle();
        Thread socketThread = new Thread(socketHandle);
        socketThread.start(); // CHẠY ĐÚNG CÁCH ở đây
    }

    public enum View {
        LOGIN,
        REGISTER,
        HOMEPAGE,
        ROOM_LIST,
        FRIEND_LIST,
        FIND_ROOM,
        WAITING_ROOM,
        GAME_CLIENT,
        CREATE_ROOM_PASSWORD,
        JOIN_ROOM_PASSWORD,
        COMPETITOR_INFO,
        RANK,
        GAME_NOTICE,
        FRIEND_REQUEST,
        GAME_AI,
        ROOM_NAME_FRM
    }
}
