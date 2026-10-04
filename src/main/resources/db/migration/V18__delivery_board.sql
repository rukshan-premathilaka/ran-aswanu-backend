-- V18: delivery board (farmer requests and vehicle offers) + product location

-- Existing rows get 'FARMER_REQUEST', so the current shared-delivery data stays valid.
ALTER TABLE transportation_requests
    ADD request_type NVARCHAR(20) NOT NULL
        CONSTRAINT DF_transport_request_type DEFAULT 'FARMER_REQUEST';
GO

-- Town / district shown on the product page (the farmer's full address stays private)
ALTER TABLE product_listings ADD location NVARCHAR(100) NULL;
GO
