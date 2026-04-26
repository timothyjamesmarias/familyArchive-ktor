-- Add last_imported_at column to families table for import tracking
ALTER TABLE families
ADD COLUMN last_imported_at TIMESTAMP;

-- Create index for querying orphaned records
CREATE INDEX idx_families_last_imported_at ON families(last_imported_at);

-- Comment explaining the column
COMMENT ON COLUMN families.last_imported_at IS 'Timestamp of the most recent GEDCOM import that included this family';
