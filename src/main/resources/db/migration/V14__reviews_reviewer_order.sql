ALTER TABLE customer_reviews ADD COLUMN reviewer_id BIGINT NULL;
ALTER TABLE customer_reviews ADD COLUMN order_id BIGINT NULL;

ALTER TABLE customer_reviews ADD CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (user_id);
ALTER TABLE customer_reviews ADD CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders (order_id);

-- One rating per order (old rows have no order, so only non-null values must be unique)
CREATE UNIQUE INDEX ux_reviews_order ON customer_reviews (order_id) WHERE order_id IS NOT NULL;
