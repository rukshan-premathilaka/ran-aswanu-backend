ALTER TABLE customer_reviews ADD reviewer_id BIGINT NULL;
ALTER TABLE customer_reviews ADD order_id    BIGINT NULL;
GO

ALTER TABLE customer_reviews ADD CONSTRAINT FK_Reviews_Reviewer FOREIGN KEY (reviewer_id) REFERENCES users (user_id);
ALTER TABLE customer_reviews ADD CONSTRAINT FK_Reviews_Order    FOREIGN KEY (order_id)    REFERENCES orders (order_id);
GO

-- One rating per order (old rows have no order, so only non-null values must be unique)
CREATE UNIQUE INDEX UX_Reviews_Order ON customer_reviews (order_id) WHERE order_id IS NOT NULL;
GO
