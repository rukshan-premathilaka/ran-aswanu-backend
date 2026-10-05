-- Replace the single-role model with a user <-> roles many-to-many model.
-- The legacy users.role column is intentionally retained for backward compatibility.

IF OBJECT_ID('roles', 'U') IS NULL
BEGIN
    CREATE TABLE roles
    (
        role_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        name NVARCHAR(50) NOT NULL,
        CONSTRAINT UQ_roles_name UNIQUE (name)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'BUYER')
    INSERT INTO roles(name) VALUES (N'BUYER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'FARMER')
    INSERT INTO roles(name) VALUES (N'FARMER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'TRANSPORT')
    INSERT INTO roles(name) VALUES (N'TRANSPORT');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'ADMIN')
    INSERT INTO roles(name) VALUES (N'ADMIN');
GO

IF OBJECT_ID('user_roles', 'U') IS NULL
BEGIN
    CREATE TABLE user_roles
    (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id),
        CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id)
            REFERENCES users(user_id) ON DELETE CASCADE,
        CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id)
            REFERENCES roles(role_id) ON DELETE CASCADE
    );
END
GO

-- Backfill the role previously stored in users.role.
INSERT INTO user_roles(user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.name = UPPER(LTRIM(RTRIM(u.role)))
WHERE u.role IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
  );
GO

-- Farmers and transport partners are also buyers in the new capability model.
INSERT INTO user_roles(user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.name = N'BUYER'
WHERE UPPER(LTRIM(RTRIM(u.role))) IN (N'FARMER', N'TRANSPORT')
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
  );
GO
