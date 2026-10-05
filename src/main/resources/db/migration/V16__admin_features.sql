ALTER TABLE product_listings
    ADD COLUMN admin_disabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE product_listings
    ADD COLUMN admin_disabled_at TIMESTAMP NULL;

CREATE INDEX ix_users_created_at ON users (created_at);
CREATE INDEX ix_product_listings_created_at ON product_listings (created_at);
CREATE INDEX ix_support_messages_created_at ON support_messages (created_at);
