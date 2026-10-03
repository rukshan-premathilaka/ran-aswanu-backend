
IF NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@example.com'
)
BEGIN
    INSERT INTO users
        (username, email, password_hash, created_at, is_active, role)
    VALUES
        (
            'admin',
            'admin@example.com',
            '$2a$10$u1vAK/HQogiMDUqhf4x/qOkv6yOMJCoBXMaJzFgC3yfp1elDJypiO',
            GETDATE(),
            1,
            'ADMIN'
        );
END;
GO
