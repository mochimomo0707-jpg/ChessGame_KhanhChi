Create database DATN;
use DATN;
CREATE TABLE users (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    nickname VARCHAR(255),
    avatar VARCHAR(255),
    numberOfGame INT DEFAULT 0,
    numberOfWin INT DEFAULT 0,
    numberOfDraw INT DEFAULT 0,
    IsOnline INT DEFAULT 0,
    IsPlaying INT DEFAULT 0
);

CREATE TABLE friend(
    ID_User1 int NOT NULL,
    ID_User2 int NOT NULL,
    FOREIGN KEY (ID_User1) REFERENCES users(ID),
    FOREIGN KEY (ID_User2) REFERENCES users(ID),
    CONSTRAINT PK_friend PRIMARY KEY (ID_User1,ID_User2)
);

CREATE TABLE BANNED_USER (
    ID_User INT PRIMARY KEY NOT NULL,
    FOREIGN KEY (ID_User) REFERENCES users(ID) ON DELETE CASCADE
);

select * from users;
