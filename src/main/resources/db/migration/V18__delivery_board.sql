ALTER TABLE transportation_requests
    ADD request_type NVARCHAR(20) NOT NULL
        CONSTRAINT DF_transport_request_type DEFAULT 'FARMER_REQUEST';
GO

ALTER TABLE product_listings ADD location NVARCHAR(100) NULL;
GO
