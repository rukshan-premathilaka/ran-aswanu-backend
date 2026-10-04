CREATE TABLE delivery_vehicles
(
    vehicle_id          BIGINT IDENTITY (1,1) PRIMARY KEY,
    vehicle_name        NVARCHAR(100) NOT NULL,
    vehicle_type        NVARCHAR(50)  NOT NULL,
    registration_number NVARCHAR(50)  NOT NULL,
    capacity_kg         BIGINT        NOT NULL,
    is_active           BIT           NOT NULL DEFAULT 1,
    created_at          DATETIME2     DEFAULT GETDATE(),
    updated_at          DATETIME2     DEFAULT GETDATE(),
    user_id             BIGINT        NOT NULL,
    CONSTRAINT FK_Delivery_Vehicles_Users
        FOREIGN KEY (user_id) REFERENCES users (user_id)
);
GO

CREATE UNIQUE INDEX UX_delivery_vehicles_registration_number
    ON delivery_vehicles (registration_number);
GO

ALTER TABLE deliveries
    ADD vehicle_id BIGINT NULL;
GO

ALTER TABLE deliveries
    ADD CONSTRAINT FK_Deliveries_Vehicles
        FOREIGN KEY (vehicle_id) REFERENCES delivery_vehicles (vehicle_id)
        ON DELETE SET NULL;
GO

ALTER TABLE transportation_requests
    ADD order_id BIGINT NULL;
GO

ALTER TABLE transportation_requests
    ADD CONSTRAINT FK_Transportation_Requests_Orders
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON DELETE SET NULL;
GO
