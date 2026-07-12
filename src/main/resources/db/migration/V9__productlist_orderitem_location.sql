CREATE TABLE Product_Listings(
                                 list_id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                 listing_status BIT NOT NULL DEFAULT 0,
                                 available_stock DECIMAL(18,2) NOT NULL DEFAULT 0.00,
                                 price_per_unit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
                                 minimum_order_quantity DECIMAL(18,2) NOT NULL DEFAULT 0.00,
                                 harvested_date DATETIME2,
                                 delivery_option NVARCHAR(255) NOT NULL,
                                 description NVARCHAR(500),
                                 unit_of_measurement NVARCHAR(10) NOT NULL,
                                 category NVARCHAR(255) NOT NULL,
                                 product_name NVARCHAR(255) NOT NULL,
                                 product_image NVARCHAR(255),
                                 created_at DATETIME2 DEFAULT GETDATE(),
                                 updated_at DATETIME2 DEFAULT GETDATE(),
                                 user_id BIGINT NOT NULL,
                                 CONSTRAINT FK_Product_Listings
                                     FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE Order_Items(
                            item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
                            quantity DECIMAL(18,2) NOT NULL,
                            unit_price DECIMAL(18,2) NOT NULL,
                            subtotal DECIMAL(18,2) NOT NULL DEFAULT 0,
                            created_at DATETIME2 DEFAULT GETDATE(),
                            updated_at DATETIME2 DEFAULT GETDATE(),
                            order_id BIGINT NOT NULL,
                            list_id BIGINT NOT NULL,
                            CONSTRAINT FK_Order_Items_orders
                                FOREIGN KEY (order_id) REFERENCES Orders (order_id),
                            CONSTRAINT FK_Order_Items_Product_Listings
                                FOREIGN KEY (list_id) REFERENCES Product_Listings(list_id)
);

CREATE TABLE Locations(
                          user_id BIGINT PRIMARY KEY ,
                          latitude DECIMAL(10,7) NOT NULL,
                          longitude DECIMAL(10,7) NOT NULL,
                          accuracy DECIMAL(18,2) NOT NULL,
                          created_at DATETIME2 DEFAULT GETDATE(),
                          updated_at DATETIME2 DEFAULT GETDATE(),
                          CONSTRAINT FK_Locations
                              FOREIGN KEY (user_id) REFERENCES users (user_id)
);