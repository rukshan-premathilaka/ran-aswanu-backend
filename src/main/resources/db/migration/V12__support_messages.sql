-- Support messages sent by users from the Help & Support page
CREATE TABLE support_messages (
    message_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    subject    NVARCHAR(100)  NOT NULL,
    message    NVARCHAR(1000) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    user_id    BIGINT NOT NULL,
    CONSTRAINT FK_Support_Messages_users FOREIGN KEY (user_id) REFERENCES users (user_id)
);
