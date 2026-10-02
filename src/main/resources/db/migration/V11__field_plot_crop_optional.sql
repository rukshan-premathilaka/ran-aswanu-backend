-- A field plot can exist without a harvest record (crop)
ALTER TABLE field_plots ALTER COLUMN crop_id BIGINT NULL;
