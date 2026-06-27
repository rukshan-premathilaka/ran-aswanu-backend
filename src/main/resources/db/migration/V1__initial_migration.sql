CREATE TABLE users
(
    user_id       BIGINT IDENTITY (1,1) PRIMARY KEY,
    username      NVARCHAR(50)  NOT NULL UNIQUE,
    email         NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(256) NOT NULL,
    created_at    DATETIME2 DEFAULT GETDATE(),
    is_active     BIT       DEFAULT 1
);