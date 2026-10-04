DECLARE @sql NVARCHAR(MAX);

SELECT @sql = 'ALTER TABLE orders DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('orders') AND c.name = 'order_status';
IF @sql IS NOT NULL EXEC sp_executesql @sql;
SET @sql = NULL;

SELECT @sql = 'ALTER TABLE orders DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('orders') AND c.name = 'payment_status';
IF @sql IS NOT NULL EXEC sp_executesql @sql;
GO

ALTER TABLE orders ALTER COLUMN order_status   NVARCHAR(20) NOT NULL;
ALTER TABLE orders ALTER COLUMN payment_status NVARCHAR(20) NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_id    BIGINT NULL;
GO

ALTER TABLE orders ADD CONSTRAINT DF_orders_order_status   DEFAULT 'PENDING' FOR order_status;
ALTER TABLE orders ADD CONSTRAINT DF_orders_payment_status DEFAULT 'UNPAID'  FOR payment_status;
UPDATE orders SET order_status   = 'PENDING' WHERE order_status   IN ('0', '1');
UPDATE orders SET payment_status = 'UNPAID'  WHERE payment_status IN ('0', '1');
GO
