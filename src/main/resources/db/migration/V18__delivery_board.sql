ALTER TABLE transportation_requests
    ADD COLUMN request_type VARCHAR(20) NOT NULL DEFAULT 'FARMER_REQUEST';

ALTER TABLE product_listings ADD COLUMN location VARCHAR(100) NULL;
