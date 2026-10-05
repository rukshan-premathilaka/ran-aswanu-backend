
ALTER TABLE product_listings
    ADD admin_disabled BIT NOT NULL
        CONSTRAINT DF_product_listings_admin_disabled DEFAULT 0;

ALTER TABLE product_listings
    ADD admin_disabled_at DATETIME2 NULL;


CREATE INDEX IX_users_created_at ON users (created_at);
CREATE INDEX IX_product_listings_created_at ON product_listings (created_at);
CREATE INDEX IX_support_messages_created_at ON support_messages (created_at);
