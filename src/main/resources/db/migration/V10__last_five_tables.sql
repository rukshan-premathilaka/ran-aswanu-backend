CREATE TABLE chats
(
    chat_id     BIGINT IDENTITY (1,1) PRIMARY KEY,
    user_one_id BIGINT NOT NULL,
    user_two_id BIGINT NOT NULL,
    created_at  DATETIME2 DEFAULT GETDATE(),
    updated_at  DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Chats_UserOne
        FOREIGN KEY (user_one_id) REFERENCES users (user_id),
    CONSTRAINT FK_Chats_UserTwo
        FOREIGN KEY (user_two_id) REFERENCES users (user_id),
    CONSTRAINT UQ_Chats_UserOne_UserTwo
        UNIQUE (user_one_id, user_two_id)
);

CREATE TABLE messages
(
    message_id BIGINT IDENTITY (1,1) PRIMARY KEY,
    content    NVARCHAR(1000) NOT NULL,
    is_read    BIT            NOT NULL DEFAULT 0,
    sent_at    DATETIME2      NOT NULL DEFAULT GETDATE(),
    user_id    BIGINT         NOT NULL,
    chat_id    BIGINT         NOT NULL,
    CONSTRAINT FK_Messages_Users
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT FK_Messages_Chats
        FOREIGN KEY (chat_id) REFERENCES chats (chat_id) ON DELETE CASCADE
);

CREATE TABLE live_stocks
(
    live_stock_id BIGINT IDENTITY (1,1) PRIMARY KEY,
    category      NVARCHAR(100) NOT NULL,
    breed         NVARCHAR(100),
    amount        BIGINT        NOT NULL DEFAULT 0,
    created_at    DATETIME2              DEFAULT GETDATE(),
    updated_at    DATETIME2              DEFAULT GETDATE(),
    user_id       BIGINT        NOT NULL,
    CONSTRAINT FK_LiveStocks_Users
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE crops
(
    crop_id          BIGINT IDENTITY (1,1) PRIMARY KEY,
    crop_name        NVARCHAR(255)  NOT NULL,
    category         NVARCHAR(100)  NOT NULL,
    unit             NVARCHAR(50)   NOT NULL,
    harvest_quantity DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    harvest_date     DATETIME2      NOT NULL DEFAULT GETDATE(),
    notes            NVARCHAR(1000),
    created_at       DATETIME2               DEFAULT GETDATE(),
    updated_at       DATETIME2               DEFAULT GETDATE(),
    user_id          BIGINT         NOT NULL,
    CONSTRAINT FK_Crops_Users
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE field_plots
(
    field_plot_id    BIGINT IDENTITY (1,1) PRIMARY KEY,
    current_crop     NVARCHAR(255) NOT NULL,
    crop_variety     NVARCHAR(255) NOT NULL,
    area_unit        DECIMAL(18, 2),
    growth_stage     NVARCHAR(100) NOT NULL,
    health_condition NVARCHAR(100) NOT NULL,
    field_logs       NVARCHAR(MAX),
    inspection_date  DATE          NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    created_at       DATETIME2              DEFAULT GETDATE(),
    updated_at       DATETIME2              DEFAULT GETDATE(),
    user_id          BIGINT        NOT NULL,
    crop_id          BIGINT        NOT NULL,
    CONSTRAINT FK_FieldPlots_Users
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT FK_FieldPlots_Crops
        FOREIGN KEY (crop_id) REFERENCES crops (crop_id)
);