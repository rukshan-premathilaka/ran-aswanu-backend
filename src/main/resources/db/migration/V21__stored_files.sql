-- Heroku has no permanent disk, so uploaded images (profile pictures, product images)
-- are stored in the database instead of the uploads folder.
IF OBJECT_ID('stored_files', 'U') IS NULL
BEGIN
    CREATE TABLE stored_files
    (
        file_path    NVARCHAR(255)  NOT NULL PRIMARY KEY,
        content_type NVARCHAR(100)  NOT NULL,
        data         VARBINARY(MAX) NOT NULL
    );
END
GO
