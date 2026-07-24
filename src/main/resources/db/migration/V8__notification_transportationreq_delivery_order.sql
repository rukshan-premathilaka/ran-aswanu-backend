CREATE TABLE notifications
(
    notification_id BIGINT IDENTITY (1,1) PRIMARY KEY,
    message         NVARCHAR(255)       NOT NULL,
    title           NVARCHAR(50)        NOT NULL,
    is_read         BIT       DEFAULT 0 NOT NULL,
    created_at      DATETIME2 DEFAULT GETDATE(),
    user_id         BIGINT              NOT NULL,
    CONSTRAINT FK_Notifications
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE deliveries
(
    delivery_id             BIGINT IDENTITY (1,1) PRIMARY KEY,
    delivery_status         BIT NOT NULL DEFAULT 0,
    assigned_date           DATETIME2    DEFAULT GETDATE(),
    estimated_delivery_date DATETIME2,
    actual_delivery_date    DATETIME2,
    delivery_note           NVARCHAR(500),
    created_at              DATETIME2    DEFAULT GETDATE(),
    updated_at              DATETIME2    DEFAULT GETDATE()
);

CREATE TABLE transportation_requests
(
    transportation_request_id BIGINT IDENTITY (1,1) PRIMARY KEY,
    description               NVARCHAR(500),
    pickup_location           NVARCHAR(255) NOT NULL,-- LOOK AFTER it says to store latitude and longitude
    delivery_location         NVARCHAR(255) NOT NULL,-- LOOK AFTER it says to store latitude and longitude
    requested_date_time       DATETIME2     NOT NULL,
    vehicle_type              NVARCHAR(50)  NOT NULL,
    estimated_weight          BIGINT        NOT NULL,
    request_status            BIT           NOT NULL DEFAULT 0,
    special_instructions      NVARCHAR(500),
    size                      NVARCHAR(50)  NOT NULL,
    created_at                DATETIME2              DEFAULT GETDATE(),
    updated_at                DATETIME2              DEFAULT GETDATE(),
    user_id                   BIGINT        NOT NULL,
    delivery_id               BIGINT        NOT NULL,
    CONSTRAINT FK_Transportation_Requests_users
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT FK_Transportation_Requests_Deliveries
        FOREIGN KEY (delivery_id) REFERENCES deliveries (delivery_id)
);

CREATE TABLE orders
(
    order_id         BIGINT IDENTITY (1,1) PRIMARY KEY,
    order_date_time  DATETIME2      NOT NULL DEFAULT GETDATE(),
    order_status     BIT            NOT NULL DEFAULT 0,
    notes            NVARCHAR(255),
    total_amount     DECIMAL(18, 2) NOT NULL DEFAULT 0.00,--should store the total amount of the order in Rs
    contact_number   NVARCHAR(50)   NOT NULL,
    delivery_address NVARCHAR(255)  NOT NULL,
    payment_method   NVARCHAR(100)  NOT NULL,
    payment_status   BIT            NOT NULL DEFAULT 0,
    created_at       DATETIME2               DEFAULT GETDATE(),
    updated_at       DATETIME2               DEFAULT GETDATE(),
    user_id          BIGINT         NOT NULL,
    delivery_id      BIGINT         NOT NULL,
    CONSTRAINT FK_Orders_users
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT FK_Orders_Deliveries
        FOREIGN KEY (delivery_id) REFERENCES deliveries (delivery_id)
);