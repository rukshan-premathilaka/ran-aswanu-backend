-- deliveries.delivery_status: BOOLEAN -> text
ALTER TABLE deliveries ALTER COLUMN delivery_status DROP DEFAULT;
ALTER TABLE deliveries ALTER COLUMN delivery_status TYPE VARCHAR(20)
    USING (CASE WHEN delivery_status THEN '1' ELSE '0' END);
ALTER TABLE deliveries ALTER COLUMN delivery_status SET DEFAULT 'PENDING';
UPDATE deliveries SET delivery_status = 'PENDING' WHERE delivery_status IN ('0', '1');

-- transportation_requests.request_status: BOOLEAN -> text, delivery_id optional
ALTER TABLE transportation_requests ALTER COLUMN request_status DROP DEFAULT;
ALTER TABLE transportation_requests ALTER COLUMN request_status TYPE VARCHAR(20)
    USING (CASE WHEN request_status THEN '1' ELSE '0' END);
ALTER TABLE transportation_requests ALTER COLUMN request_status SET DEFAULT 'OPEN';
UPDATE transportation_requests SET request_status = 'OPEN' WHERE request_status IN ('0', '1');
ALTER TABLE transportation_requests ALTER COLUMN delivery_id DROP NOT NULL;
