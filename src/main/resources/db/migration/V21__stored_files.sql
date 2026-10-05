-- Heroku has no permanent disk, so uploaded images (profile pictures, product images)
-- are stored in the database instead of the uploads folder.
CREATE TABLE IF NOT EXISTS stored_files
(
    file_path    VARCHAR(255) NOT NULL PRIMARY KEY,
    content_type VARCHAR(100) NOT NULL,
    data         BYTEA        NOT NULL
);
