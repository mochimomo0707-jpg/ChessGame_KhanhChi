package model;

public class User {

    private int ID;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private int numberOfGame;  // Số ván chơi đã tham gia
    private int numberOfWin;   // Số ván thắng
    private int numberOfDraw;
    private boolean online;    // Trạng thái online
    private boolean playing;   // Trạng thái đang chơi
    private int rank;          // Xếp hạng người chơi

    // Constructor đầy đủ thông tin
    public User(int ID, String username, String password, String nickname, String avatar, int numberOfGame, int numberOfWin, int numberOfDraw, int rank) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.numberOfGame = numberOfGame;
        this.numberOfWin = numberOfWin;
        this.numberOfDraw = numberOfDraw;
        this.rank = rank;
    }

    // Constructor không có xếp hạng và trạng thái chơi
    public User(int ID, String username, String password, String nickname, String avatar, int numberOfGame, int numberOfWin, int numberOfDraw, boolean online, boolean playing) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.numberOfGame = numberOfGame;
        this.numberOfWin = numberOfWin;
        this.numberOfDraw = numberOfDraw;
        this.online = online;
        this.playing = playing;
    }

    public User() {
    }

    // Constructor không có thông tin xếp hạng
    public User(int ID, String username, String password, String nickname, String avatar, int numberOfGame, int numberOfWin, int numberOfDraw) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.numberOfGame = numberOfGame;
        this.numberOfWin = numberOfWin;
        this.numberOfDraw = numberOfDraw;
    }

    // Constructor chỉ với ID và nickname
    public User(int ID, String nickname) {
        this.ID = ID;
        this.nickname = nickname;
    }

    // Constructor với thông tin trạng thái người chơi
    public User(int ID, String nickname, boolean online, boolean playing) {
        this.ID = ID;
        this.nickname = nickname;
        this.online = online;
        this.playing = playing;
    }

    public int getNumberOfDraw() {
        return numberOfDraw;
    }

    public void setNumberOfDraw(int numberOfDraw) {
        this.numberOfDraw = numberOfDraw;
    }

    // Getter và Setter
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getNumberOfGame() {
        return numberOfGame;
    }

    public void setNumberOfGame(int numberOfGame) {
        this.numberOfGame = numberOfGame;
    }

    public int getNumberOfWin() {
        return numberOfWin;
    }

    public void setNumberOfWin(int numberOfWin) {
        this.numberOfWin = numberOfWin;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    // Tính tỷ lệ thắng
    public float getWinRatio() {
        if (numberOfGame == 0) {
            return 0;
        }
        return (float) numberOfWin / numberOfGame * 100;
    }

    // In thông tin người chơi
    public void log() {
        System.out.println("ID: " + this.ID + "| Nickname: " + this.nickname + "| Games Played: " + this.numberOfGame + "| Wins: " + this.numberOfWin + "| Draw: " + this.numberOfWin);
    }
}
