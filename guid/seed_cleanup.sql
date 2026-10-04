-- Removes ONLY the fake data created by seed_data.sql (users with e-mail @ranaswanu.test and their rows).
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

SELECT user_id INTO #seed_users FROM users WHERE email LIKE '%@ranaswanu.test';
SELECT DISTINCT delivery_id INTO #seed_deliveries FROM transportation_requests
 WHERE delivery_id IS NOT NULL AND user_id IN (SELECT user_id FROM #seed_users);
SELECT order_id INTO #seed_orders FROM orders WHERE user_id IN (SELECT user_id FROM #seed_users);

DELETE FROM messages WHERE user_id IN (SELECT user_id FROM #seed_users)
   OR chat_id IN (SELECT chat_id FROM chats WHERE user_one_id IN (SELECT user_id FROM #seed_users) OR user_two_id IN (SELECT user_id FROM #seed_users));
DELETE FROM chats WHERE user_one_id IN (SELECT user_id FROM #seed_users) OR user_two_id IN (SELECT user_id FROM #seed_users);
DELETE FROM customer_reviews WHERE user_id IN (SELECT user_id FROM #seed_users) OR reviewer_id IN (SELECT user_id FROM #seed_users)
   OR order_id IN (SELECT order_id FROM #seed_orders);
DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM #seed_orders)
   OR list_id IN (SELECT list_id FROM product_listings WHERE user_id IN (SELECT user_id FROM #seed_users));
DELETE FROM orders WHERE order_id IN (SELECT order_id FROM #seed_orders);
DELETE FROM transportation_requests WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM deliveries WHERE delivery_id IN (SELECT delivery_id FROM #seed_deliveries);
DELETE FROM delivery_vehicles WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM product_listings WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM field_plots WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM crops WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM expenses WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM farm_activities WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM live_stocks WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM reminders WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM notifications WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM support_messages WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM password_reset_tokens WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM locations WHERE user_id IN (SELECT user_id FROM #seed_users);
DELETE FROM users WHERE user_id IN (SELECT user_id FROM #seed_users);

DROP TABLE #seed_orders; DROP TABLE #seed_deliveries; DROP TABLE #seed_users;
COMMIT TRANSACTION;
SELECT 'Seed data removed' AS result;
