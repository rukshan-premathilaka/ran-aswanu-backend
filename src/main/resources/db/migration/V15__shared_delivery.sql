DECLARE @sql NVARCHAR(MAX);

SELECT @sql = 'ALTER TABLE deliveries DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('deliveries') AND c.name = 'delivery_status';
IF @sql IS NOT NULL EXEC sp_executesql @sql;
SET @sql = NULL;

ALTER TABLE deliveries ALTER COLUMN delivery_status NVARCHAR(20) NOT NULL;
ALTER TABLE deliveries ADD CONSTRAINT DF_deliveries_status DEFAULT 'PENDING' FOR delivery_status;
UPDATE deliveries SET delivery_status = 'PENDING' WHERE delivery_status IN ('0', '1');
GO

DECLARE @sql2 NVARCHAR(MAX);

SELECT @sql2 = 'ALTER TABLE transportation_requests DROP CONSTRAINT ' + dc.name
FROM sys.default_constraints dc
JOIN sys.columns c ON c.default_object_id = dc.object_id
WHERE dc.parent_object_id = OBJECT_ID('transportation_requests') AND c.name = 'request_status';
IF @sql2 IS NOT NULL EXEC sp_executesql @sql2;
SET @sql2 = NULL;

ALTER TABLE transportation_requests ALTER COLUMN request_status NVARCHAR(20) NOT NULL;
ALTER TABLE transportation_requests ADD CONSTRAINT DF_transport_status DEFAULT 'OPEN' FOR request_status;
UPDATE transportation_requests SET request_status = 'OPEN' WHERE request_status IN ('0', '1');
ALTER TABLE transportation_requests ALTER COLUMN delivery_id BIGINT NULL;
GO
