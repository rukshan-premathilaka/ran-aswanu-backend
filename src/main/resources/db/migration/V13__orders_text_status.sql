-- order_status / payment_status: BOOLEAN -> text ('PENDING', 'UNPAID', ...)
ALTER TABLE orders ALTER COLUMN order_status DROP DEFAULT;
ALTER TABLE orders ALTER COLUMN payment_status DROP DEFAULT;

ALTER TABLE orders ALTER COLUMN order_status TYPE VARCHAR(20)
    USING (CASE WHEN order_status THEN '1' ELSE '0' END);
ALTER TABLE orders ALTER COLUMN payment_status TYPE VARCHAR(20)
    USING (CASE WHEN payment_status THEN '1' ELSE '0' END);

ALTER TABLE orders ALTER COLUMN delivery_id DROP NOT NULL;

ALTER TABLE orders ALTER COLUMN order_status SET DEFAULT 'PENDING';
ALTER TABLE orders ALTER COLUMN payment_status SET DEFAULT 'UNPAID';

UPDATE orders SET order_status = 'PENDING' WHERE order_status IN ('0', '1');
UPDATE orders SET payment_status = 'UNPAID' WHERE payment_status IN ('0', '1');
