SET NOCOUNT ON;
SET XACT_ABORT ON;

IF EXISTS (SELECT 1 FROM users WHERE email LIKE '%@ranaswanu.test')
BEGIN
    RAISERROR('Seed data already exists. Run seed_cleanup.sql first.', 16, 1);
    RETURN;
END

BEGIN TRANSACTION;

DECLARE @now DATETIME2 = SYSUTCDATETIME();

DECLARE @admin BIGINT, @nimal BIGINT, @kamala BIGINT, @sunil BIGINT, @saman BIGINT, @dilini BIGINT, @ruwan BIGINT, @kasun BIGINT, @mala BIGINT;

-- ---------- USERS (password hashes are real BCrypt hashes) ----------
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'admin_ranaswanu', N'admin@ranaswanu.test', '$2a$10$u7Mjzdk1yjerOFWvm9x80uygKaP8h.43qIMy9PQETKhFDBb3iMEiG', DATEADD(day, -60, @now), 1, 'ADMIN', 'profile-pics/seed-admin_ranaswanu.jpg', N'Head Office, Badulla', '0711000001');
SET @admin = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'farmer_nimal', N'nimal@ranaswanu.test', '$2a$10$xhV1Wg4U9KcMMwlbGtT3l.IRhnTcEOe3xXMQQUcFgank9itg2r/nq', DATEADD(day, -60, @now), 1, 'FARMER', 'profile-pics/seed-farmer_nimal.jpg', N'12 Hill Road, Nuwara Eliya', '0712000002');
SET @nimal = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'farmer_kamala', N'kamala@ranaswanu.test', '$2a$10$4IBfHo7dMebjBeF1s7N2keU2Kh0/z97uhJle7HMTMkH60QKgHkwhi', DATEADD(day, -60, @now), 1, 'FARMER', 'profile-pics/seed-farmer_kamala.jpg', N'45 Tank Road, Dambulla', '0712000003');
SET @kamala = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'farmer_sunil', N'sunil@ranaswanu.test', '$2a$10$K0SMqxfI3GkUj9Br2enEeemiE5Yd9J1bdYyJ67e52q7Pv.cUPkOse', DATEADD(day, -60, @now), 1, 'FARMER', 'profile-pics/seed-farmer_sunil.jpg', N'8 Temple Lane, Kandy', '0712000004');
SET @sunil = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'buyer_saman', N'saman@ranaswanu.test', '$2a$10$0lHLDNP47oBZovOVT4oiMuo8.PESAAY9xQGAG07j5dDpVng6Yu66W', DATEADD(day, -60, @now), 1, 'BUYER', 'profile-pics/seed-buyer_saman.jpg', N'21 Galle Road, Colombo 03', '0773000005');
SET @saman = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'buyer_dilini', N'dilini@ranaswanu.test', '$2a$10$js.1.xU84lLx8NjzemjzAuc1Q8qS94FN50lLTnnwt7o3c6zDHRXJq', DATEADD(day, -60, @now), 1, 'BUYER', 'profile-pics/seed-buyer_dilini.jpg', N'77 Lake Drive, Kurunegala', '0773000006');
SET @dilini = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'buyer_ruwan', N'ruwan@ranaswanu.test', '$2a$10$OWEF3a0.6SN1Z65m8brL2.Zq762kJUPkKyTQI1svJF6ukOcRIMZv6', DATEADD(day, -60, @now), 1, 'BUYER', 'profile-pics/seed-buyer_ruwan.jpg', N'5 Beach Road, Galle', '0773000007');
SET @ruwan = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'transport_kasun', N'kasun@ranaswanu.test', '$2a$10$cEFUN/RQ3QmGBPV8fBg9Ae2W6dJHi/epdIT58.Ayx3DxCAdLDpitu', DATEADD(day, -60, @now), 1, 'TRANSPORT', 'profile-pics/seed-transport_kasun.jpg', N'30 Depot Road, Matale', '0754000008');
SET @kasun = SCOPE_IDENTITY();
INSERT INTO users (username, email, password_hash, created_at, is_active, role, profile_picture, address, phone_number)
VALUES (N'transport_mala', N'mala@ranaswanu.test', '$2a$10$5wwFkyQP6./6q1AaUkhhPO/iq4gYHu9LHY4x4/tNCbLQQsA8CgFWK', DATEADD(day, -60, @now), 1, 'TRANSPORT', 'profile-pics/seed-transport_mala.jpg', N'9 Market Street, Kandy', '0754000009');
SET @mala = SCOPE_IDENTITY();

-- ---------- LOCATIONS ----------
INSERT INTO locations (user_id, latitude, longitude, accuracy) VALUES (@nimal, 6.9497, 80.7891, 15.00);
INSERT INTO locations (user_id, latitude, longitude, accuracy) VALUES (@kamala, 7.8731, 80.6518, 15.00);
INSERT INTO locations (user_id, latitude, longitude, accuracy) VALUES (@sunil, 7.2906, 80.6337, 15.00);
INSERT INTO locations (user_id, latitude, longitude, accuracy) VALUES (@kasun, 7.4675, 80.6234, 15.00);
INSERT INTO locations (user_id, latitude, longitude, accuracy) VALUES (@mala, 7.2931, 80.635, 15.00);

-- ---------- FARM MANAGEMENT (crops, plots, expenses, activities, livestock, calendar notes) ----------
-- nimal
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Carrots', N'Vegetables', N'kg', 450, DATEADD(day, -10, @now), N'Harvest from block A', @nimal);
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Leeks', N'Vegetables', N'kg', 280, DATEADD(day, -10, @now), N'Good quality this season', @nimal);
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Cabbage', N'Vegetables', N'kg', 600, DATEADD(day, -10, @now), NULL, @nimal);
INSERT INTO field_plots (current_crop, crop_variety, area_unit, growth_stage, health_condition, field_logs, inspection_date, user_id) VALUES (N'Carrots', N'Nantes', 1.5, N'Mature', N'Healthy', N'Watered twice this week.', CONVERT(date, @now), @nimal);
INSERT INTO field_plots (current_crop, crop_variety, area_unit, growth_stage, health_condition, field_logs, inspection_date, user_id) VALUES (N'Leeks', N'Musselburgh', 0.75, N'Growing', N'Needs attention', N'Some yellow leaf tips noticed.', CONVERT(date, @now), @nimal);
INSERT INTO expenses (expense_date, title, category, amount, user_id) VALUES (DATEADD(day, -5, @now), N'Seeds', N'Inputs', 12500, @nimal);
INSERT INTO expenses (expense_date, title, category, amount, user_id) VALUES (DATEADD(day, -5, @now), N'Fertilizer', N'Inputs', 18000, @nimal);
INSERT INTO expenses (expense_date, title, category, amount, user_id) VALUES (DATEADD(day, -5, @now), N'Labour', N'Wages', 30000, @nimal);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Apply fertilizer to carrot block', 0, @nimal);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Repair irrigation pipe', 1, @nimal);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Prepare cabbage beds', 0, @nimal);
INSERT INTO live_stocks (category, breed, amount, user_id) VALUES (N'Cattle', N'Friesian', 4, @nimal);
INSERT INTO live_stocks (category, breed, amount, user_id) VALUES (N'Poultry', N'Layer hens', 25, @nimal);
INSERT INTO reminders (reminder_date, description, user_id) VALUES (CAST(CAST(DATEADD(day, -2, @now) AS date) AS datetime2), N'Sprayed leeks with organic pesticide.', @nimal);
INSERT INTO reminders (reminder_date, description, user_id) VALUES (CAST(CAST(DATEADD(day, 1, @now) AS date) AS datetime2), N'Buyer pickup at 10am.', @nimal);
INSERT INTO reminders (reminder_date, description, user_id) VALUES (CAST(CAST(DATEADD(day, 3, @now) AS date) AS datetime2), N'Order seeds.', @nimal);
-- kamala
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Tomatoes', N'Vegetables', N'kg', 800, DATEADD(day, -10, @now), N'Two harvests so far', @kamala);
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Red Onions', N'Vegetables', N'kg', 950, DATEADD(day, -10, @now), NULL, @kamala);
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Green Chilli', N'Vegetables', N'kg', 150, DATEADD(day, -10, @now), N'Dry weather helped', @kamala);
INSERT INTO field_plots (current_crop, crop_variety, area_unit, growth_stage, health_condition, field_logs, inspection_date, user_id) VALUES (N'Tomatoes', N'Thilina', 2.0, N'Fruiting', N'Healthy', N'Staking completed.', CONVERT(date, @now), @kamala);
INSERT INTO field_plots (current_crop, crop_variety, area_unit, growth_stage, health_condition, field_logs, inspection_date, user_id) VALUES (N'Red Onions', N'Bombay Red', 1.25, N'Mature', N'Healthy', NULL, CONVERT(date, @now), @kamala);
INSERT INTO expenses (expense_date, title, category, amount, user_id) VALUES (DATEADD(day, -5, @now), N'Drip pipes', N'Equipment', 42000, @kamala);
INSERT INTO expenses (expense_date, title, category, amount, user_id) VALUES (DATEADD(day, -5, @now), N'Pesticide', N'Inputs', 9500, @kamala);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Pick ripe tomatoes', 0, @kamala);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Dry onions in shade', 0, @kamala);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Pay labour', 1, @kamala);
INSERT INTO live_stocks (category, breed, amount, user_id) VALUES (N'Goat', N'Jamnapari', 6, @kamala);
INSERT INTO reminders (reminder_date, description, user_id) VALUES (CAST(CAST(DATEADD(day, 0, @now) AS date) AS datetime2), N'Check water pump.', @kamala);
INSERT INTO reminders (reminder_date, description, user_id) VALUES (CAST(CAST(DATEADD(day, 2, @now) AS date) AS datetime2), N'Transport booking for onions.', @kamala);
-- sunil
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Cinnamon', N'Spices', N'kg', 120, DATEADD(day, -10, @now), N'Peeling season', @sunil);
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Black Pepper', N'Spices', N'kg', 70, DATEADD(day, -10, @now), NULL, @sunil);
INSERT INTO crops (crop_name, category, unit, harvest_quantity, harvest_date, notes, user_id) VALUES (N'Red Rice', N'Grains', N'kg', 1200, DATEADD(day, -10, @now), N'Yala season', @sunil);
INSERT INTO field_plots (current_crop, crop_variety, area_unit, growth_stage, health_condition, field_logs, inspection_date, user_id) VALUES (N'Cinnamon', N'Sri Gemunu', 3.0, N'Mature', N'Healthy', N'Ready for peeling.', CONVERT(date, @now), @sunil);
INSERT INTO field_plots (current_crop, crop_variety, area_unit, growth_stage, health_condition, field_logs, inspection_date, user_id) VALUES (N'Red Rice', N'Pachchaperumal', 2.5, N'Harvest', N'Healthy', N'Harvest finished.', CONVERT(date, @now), @sunil);
INSERT INTO expenses (expense_date, title, category, amount, user_id) VALUES (DATEADD(day, -5, @now), N'Labour', N'Wages', 52000, @sunil);
INSERT INTO expenses (expense_date, title, category, amount, user_id) VALUES (DATEADD(day, -5, @now), N'Transport', N'Logistics', 15000, @sunil);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Peel cinnamon', 0, @sunil);
INSERT INTO farm_activities (activity, activity_status, user_id) VALUES (N'Dry pepper on mats', 0, @sunil);
INSERT INTO live_stocks (category, breed, amount, user_id) VALUES (N'Poultry', N'Country chicken', 18, @sunil);
INSERT INTO reminders (reminder_date, description, user_id) VALUES (CAST(CAST(DATEADD(day, 1, @now) AS date) AS datetime2), N'Delivery to Colombo buyer.', @sunil);

-- ---------- PRODUCT LISTINGS (images are files in uploads/product-images) ----------
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 500, 180, 2, DATEADD(day, -3, @now), N'Both', N'Fresh hill-country carrots, washed and sorted.', 'kg', N'Vegetables', N'Carrots', N'product-images/seed-carrot.jpg', @nimal, 0, NULL, N'Nuwara Eliya');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 300, 220, 1, DATEADD(day, -3, @now), N'Delivery', N'Tender leeks harvested this week.', 'kg', N'Vegetables', N'Leeks', N'product-images/seed-leeks.jpg', @nimal, 0, NULL, N'Nuwara Eliya');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 400, 120, 2, DATEADD(day, -3, @now), N'Pickup', N'Large compact cabbage heads.', 'kg', N'Vegetables', N'Cabbage', N'product-images/seed-cabbage.jpg', @nimal, 0, NULL, N'Nuwara Eliya');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 80, 900, 0.5, DATEADD(day, -3, @now), N'Both', N'Sweet strawberries from Nuwara Eliya farms.', 'kg', N'Fruits', N'Strawberries', N'product-images/seed-strawberry.jpg', @nimal, 0, NULL, N'Nuwara Eliya');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 600, 150, 2, DATEADD(day, -3, @now), N'Both', N'Ripe red tomatoes, good for curry and salad.', 'kg', N'Vegetables', N'Tomatoes', N'product-images/seed-tomato.jpg', @kamala, 0, NULL, N'Dambulla');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 700, 260, 5, DATEADD(day, -3, @now), N'Delivery', N'Dry red onions, long shelf life.', 'kg', N'Vegetables', N'Red Onions', N'product-images/seed-onion.jpg', @kamala, 0, NULL, N'Dambulla');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (0, 200, 140, 1, DATEADD(day, -3, @now), N'Pickup', N'Draft listing: not published yet.', 'kg', N'Vegetables', N'Brinjal', NULL, @kamala, 0, NULL, N'Dambulla');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 120, 320, 0.5, DATEADD(day, -3, @now), N'Both', N'Hot green chillies, picked fresh.', 'kg', N'Vegetables', N'Green Chilli', N'product-images/seed-chilli.jpg', @kamala, 0, NULL, N'Dambulla');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 60, 1800, 0.25, DATEADD(day, -3, @now), N'Both', N'True Ceylon cinnamon quills.', 'kg', N'Spices', N'Cinnamon Sticks', N'product-images/seed-cinnamon.jpg', @sunil, 0, NULL, N'Kandy');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 45, 2400, 0.25, DATEADD(day, -3, @now), N'Delivery', N'Sun-dried black pepper.', 'kg', N'Spices', N'Black Pepper', N'product-images/seed-pepper.jpg', @sunil, 0, NULL, N'Kandy');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 900, 280, 5, DATEADD(day, -3, @now), N'Pickup', N'Traditional red raw rice.', 'kg', N'Grains', N'Red Rice', N'product-images/seed-rice.jpg', @sunil, 0, NULL, N'Kandy');
INSERT INTO product_listings (listing_status, available_stock, price_per_unit, minimum_order_quantity, harvested_date, delivery_option, description, unit_of_measurement, category, product_name, product_image, user_id, admin_disabled, admin_disabled_at, location)
VALUES (1, 250, 200, 2, DATEADD(day, -3, @now), N'Both', N'Disabled by admin (for testing admin features).', 'kg', N'Fruits', N'Bananas', N'product-images/seed-banana.jpg', @sunil, 1, @now, N'Kandy');

-- ---------- ORDERS (all items of one order belong to ONE farmer) ----------
DECLARE @o1 BIGINT, @o2 BIGINT, @o3 BIGINT, @o4 BIGINT, @o5 BIGINT, @o6 BIGINT, @o7 BIGINT;
INSERT INTO orders (order_date_time, order_status, notes, total_amount, contact_number, delivery_address, payment_method, payment_status, created_at, updated_at, user_id)
VALUES (DATEADD(day, -14, @now), 'COMPLETED', N'Please pack carefully.', 0, '0773000005', N'21 Galle Road, Colombo 03', 'BANK_TRANSFER', 'PAID', DATEADD(day, -14, @now), DATEADD(day, -14, @now), @saman);
SET @o1 = SCOPE_IDENTITY();
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 5, price_per_unit, 5 * price_per_unit, @o1, list_id FROM product_listings WHERE user_id = @nimal AND product_name = N'Carrots';
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 3, price_per_unit, 3 * price_per_unit, @o1, list_id FROM product_listings WHERE user_id = @nimal AND product_name = N'Leeks';
INSERT INTO orders (order_date_time, order_status, notes, total_amount, contact_number, delivery_address, payment_method, payment_status, created_at, updated_at, user_id)
VALUES (DATEADD(day, -1, @now), 'PENDING', NULL, 0, '0773000006', N'77 Lake Drive, Kurunegala', 'CASH_ON_DELIVERY', 'UNPAID', DATEADD(day, -1, @now), DATEADD(day, -1, @now), @dilini);
SET @o2 = SCOPE_IDENTITY();
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 4, price_per_unit, 4 * price_per_unit, @o2, list_id FROM product_listings WHERE user_id = @nimal AND product_name = N'Cabbage';
INSERT INTO orders (order_date_time, order_status, notes, total_amount, contact_number, delivery_address, payment_method, payment_status, created_at, updated_at, user_id)
VALUES (DATEADD(day, -3, @now), 'ACCEPTED', NULL, 0, '0773000005', N'21 Galle Road, Colombo 03', 'CASH_ON_DELIVERY', 'UNPAID', DATEADD(day, -3, @now), DATEADD(day, -3, @now), @saman);
SET @o3 = SCOPE_IDENTITY();
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 6, price_per_unit, 6 * price_per_unit, @o3, list_id FROM product_listings WHERE user_id = @kamala AND product_name = N'Tomatoes';
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 10, price_per_unit, 10 * price_per_unit, @o3, list_id FROM product_listings WHERE user_id = @kamala AND product_name = N'Red Onions';
INSERT INTO orders (order_date_time, order_status, notes, total_amount, contact_number, delivery_address, payment_method, payment_status, created_at, updated_at, user_id)
VALUES (DATEADD(day, -5, @now), 'SHIPPED', N'Call before delivery.', 0, '0773000007', N'5 Beach Road, Galle', 'CASH_ON_DELIVERY', 'UNPAID', DATEADD(day, -5, @now), DATEADD(day, -5, @now), @ruwan);
SET @o4 = SCOPE_IDENTITY();
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 1.5, price_per_unit, 1.5 * price_per_unit, @o4, list_id FROM product_listings WHERE user_id = @kamala AND product_name = N'Green Chilli';
INSERT INTO orders (order_date_time, order_status, notes, total_amount, contact_number, delivery_address, payment_method, payment_status, created_at, updated_at, user_id)
VALUES (DATEADD(day, -20, @now), 'COMPLETED', NULL, 0, '0773000006', N'77 Lake Drive, Kurunegala', 'BANK_TRANSFER', 'PAID', DATEADD(day, -20, @now), DATEADD(day, -20, @now), @dilini);
SET @o5 = SCOPE_IDENTITY();
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 0.5, price_per_unit, 0.5 * price_per_unit, @o5, list_id FROM product_listings WHERE user_id = @sunil AND product_name = N'Cinnamon Sticks';
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 10, price_per_unit, 10 * price_per_unit, @o5, list_id FROM product_listings WHERE user_id = @sunil AND product_name = N'Red Rice';
INSERT INTO orders (order_date_time, order_status, notes, total_amount, contact_number, delivery_address, payment_method, payment_status, created_at, updated_at, user_id)
VALUES (DATEADD(day, -8, @now), 'REJECTED', NULL, 0, '0773000007', N'5 Beach Road, Galle', 'CASH_ON_DELIVERY', 'UNPAID', DATEADD(day, -8, @now), DATEADD(day, -8, @now), @ruwan);
SET @o6 = SCOPE_IDENTITY();
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 0.5, price_per_unit, 0.5 * price_per_unit, @o6, list_id FROM product_listings WHERE user_id = @sunil AND product_name = N'Black Pepper';
INSERT INTO orders (order_date_time, order_status, notes, total_amount, contact_number, delivery_address, payment_method, payment_status, created_at, updated_at, user_id)
VALUES (DATEADD(day, -9, @now), 'COMPLETED', NULL, 0, '0773000005', N'21 Galle Road, Colombo 03', 'CASH_ON_DELIVERY', 'PAID', DATEADD(day, -9, @now), DATEADD(day, -9, @now), @saman);
SET @o7 = SCOPE_IDENTITY();
INSERT INTO order_items (quantity, unit_price, subtotal, order_id, list_id)
SELECT 5, price_per_unit, 5 * price_per_unit, @o7, list_id FROM product_listings WHERE user_id = @sunil AND product_name = N'Red Rice';
-- order total = sum of item subtotals
UPDATE o SET total_amount = (SELECT SUM(i.subtotal) FROM order_items i WHERE i.order_id = o.order_id)
FROM orders o WHERE o.user_id IN (@saman, @dilini, @ruwan);

-- ---------- REVIEWS (only for COMPLETED orders; o7 is left unrated so you can test rating) ----------
INSERT INTO customer_reviews (review_date, description, rating, user_id, reviewer_id, order_id) VALUES (DATEADD(day, -12, @now), N'Very fresh vegetables and good packing.', 5, @nimal, @saman, @o1);
INSERT INTO customer_reviews (review_date, description, rating, user_id, reviewer_id, order_id) VALUES (DATEADD(day, -18, @now), N'Great cinnamon, a little late delivery.', 4, @sunil, @dilini, @o5);

-- ---------- DELIVERY BOARD ----------
DECLARE @d1 BIGINT, @d2 BIGINT;
-- Open items (nobody has accepted them yet)
INSERT INTO transportation_requests (description, pickup_location, delivery_location, requested_date_time, vehicle_type, estimated_weight, request_status, size, user_id, request_type)
VALUES (N'Carrots 800 kg to Colombo', N'Nuwara Eliya', N'Colombo', DATEADD(day, 2, @now), N'Lorry', 800, 'OPEN', N'Large', @nimal, 'FARMER_REQUEST');
INSERT INTO transportation_requests (description, pickup_location, delivery_location, requested_date_time, vehicle_type, estimated_weight, request_status, size, user_id, request_type)
VALUES (N'Onions and tomatoes for Colombo market', N'Dambulla', N'Colombo', DATEADD(day, 2, @now), N'Lorry', 600, 'OPEN', N'Large', @kamala, 'FARMER_REQUEST');
INSERT INTO transportation_requests (description, pickup_location, delivery_location, requested_date_time, vehicle_type, estimated_weight, request_status, size, user_id, request_type)
VALUES (N'Dual-cab pickup, going Matale to Colombo', N'Matale', N'Colombo', DATEADD(day, 3, @now), N'Pickup', 600, 'OPEN', N'N/A', @kasun, 'VEHICLE_OFFER');
INSERT INTO transportation_requests (description, pickup_location, delivery_location, requested_date_time, vehicle_type, estimated_weight, request_status, size, user_id, request_type)
VALUES (N'Small lorry, Kandy to Colombo', N'Kandy', N'Colombo', DATEADD(day, 4, @now), N'Lorry', 1500, 'OPEN', N'N/A', @mala, 'VEHICLE_OFFER');
-- Two matched deliveries (both users of a delivery have a request row with the same delivery_id)
INSERT INTO deliveries (delivery_status, assigned_date, estimated_delivery_date) VALUES ('IN_TRANSIT', DATEADD(day, -1, @now), DATEADD(day, 1, @now));
SET @d1 = SCOPE_IDENTITY();
INSERT INTO transportation_requests (description, pickup_location, delivery_location, requested_date_time, vehicle_type, estimated_weight, request_status, size, user_id, delivery_id, request_type)
VALUES (N'Cinnamon and rice to Colombo', N'Kandy', N'Colombo', DATEADD(day, -1, @now), N'Lorry', 900, 'IN_TRANSIT', N'Medium', @sunil, @d1, 'FARMER_REQUEST'),
       (N'Cinnamon and rice to Colombo', N'Kandy', N'Colombo', DATEADD(day, -1, @now), N'Lorry', 900, 'IN_TRANSIT', N'Medium', @kasun, @d1, 'VEHICLE_OFFER');
INSERT INTO deliveries (delivery_status, estimated_delivery_date) VALUES ('PENDING', DATEADD(day, 5, @now));
SET @d2 = SCOPE_IDENTITY();
INSERT INTO transportation_requests (description, pickup_location, delivery_location, requested_date_time, vehicle_type, estimated_weight, request_status, size, user_id, delivery_id, request_type)
VALUES (N'Black pepper to Galle', N'Kandy', N'Galle', DATEADD(day, 5, @now), N'Van', 200, 'MATCHED', N'Small', @sunil, @d2, 'FARMER_REQUEST'),
       (N'Black pepper to Galle', N'Kandy', N'Galle', DATEADD(day, 5, @now), N'Van', 200, 'MATCHED', N'Small', @mala, @d2, 'VEHICLE_OFFER');

-- ---------- CHATS (user_one_id is always the smaller user id) ----------
DECLARE @c1 BIGINT, @c2 BIGINT;
INSERT INTO chats (user_one_id, user_two_id) VALUES (IIF(@saman < @nimal, @saman, @nimal), IIF(@saman < @nimal, @nimal, @saman));
SET @c1 = SCOPE_IDENTITY();
INSERT INTO chats (user_one_id, user_two_id) VALUES (IIF(@dilini < @sunil, @dilini, @sunil), IIF(@dilini < @sunil, @sunil, @dilini));
SET @c2 = SCOPE_IDENTITY();
INSERT INTO messages (content, is_read, sent_at, user_id, chat_id) VALUES (N'Hello, are the carrots still available?', 1, DATEADD(minute, -60, @now), @saman, @c1);
INSERT INTO messages (content, is_read, sent_at, user_id, chat_id) VALUES (N'Yes, fresh stock arrived today.', 1, DATEADD(minute, -55, @now), @nimal, @c1);
INSERT INTO messages (content, is_read, sent_at, user_id, chat_id) VALUES (N'Great, I will order 5 kg.', 1, DATEADD(minute, -50, @now), @saman, @c1);
INSERT INTO messages (content, is_read, sent_at, user_id, chat_id) VALUES (N'Thank you! I will pack them tomorrow.', 0, DATEADD(minute, -45, @now), @nimal, @c1);
INSERT INTO messages (content, is_read, sent_at, user_id, chat_id) VALUES (N'Can you deliver the cinnamon to Kurunegala?', 1, DATEADD(minute, -120, @now), @dilini, @c2);
INSERT INTO messages (content, is_read, sent_at, user_id, chat_id) VALUES (N'Yes, through a transport partner.', 0, DATEADD(minute, -100, @now), @sunil, @c2);
UPDATE chats SET updated_at = @now WHERE chat_id IN (@c1, @c2);

-- ---------- NOTIFICATIONS & SUPPORT ----------
INSERT INTO notifications (message, title, is_read, user_id) VALUES (N'You received order #1 from buyer_saman.', N'New order', 1, @nimal);
INSERT INTO notifications (message, title, is_read, user_id) VALUES (N'You received a new order from buyer_dilini.', N'New order', 0, @nimal);
INSERT INTO notifications (message, title, is_read, user_id) VALUES (N'buyer_saman rated order: 5/5', N'New rating', 0, @nimal);
INSERT INTO notifications (message, title, is_read, user_id) VALUES (N'Your order was marked completed. You can rate it now.', N'Order completed', 0, @saman);
INSERT INTO notifications (message, title, is_read, user_id) VALUES (N'Your delivery was matched with farmer_sunil.', N'Delivery matched', 0, @kasun);
INSERT INTO notifications (message, title, is_read, user_id) VALUES (N'You accepted an order from buyer_saman.', N'Order accepted', 1, @kamala);
INSERT INTO support_messages (subject, message, user_id) VALUES (N'Cannot upload image', N'The product image upload shows an error on my phone.', @kamala);
INSERT INTO support_messages (subject, message, user_id) VALUES (N'Question about delivery', N'How do I choose a vehicle for my cinnamon?', @sunil);

COMMIT TRANSACTION;

SELECT 'Seed data inserted' AS result, (SELECT COUNT(*) FROM users WHERE email LIKE '%@ranaswanu.test') AS seed_users;
