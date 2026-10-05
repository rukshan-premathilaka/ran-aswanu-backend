CREATE TABLE password_reset_tokens
(
    id          BIGINT IDENTITY (1,1) PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date DATETIME2    NOT NULL,
    used        BIT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);