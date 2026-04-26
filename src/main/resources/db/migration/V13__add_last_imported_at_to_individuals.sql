-- Add last_imported_at column to individuals table for import tracking
ALTER TABLE individuals
ADD COLUMN last_imported_at TIMESTAMP;

-- Create index for querying orphaned records
CREATE INDEX idx_individuals_last_imported_at ON individuals(last_imported_at);

-- Comment explaining the column
COMMENT ON COLUMN individuals.last_imported_at IS 'Timestamp of the most recent GEDCOM import that included this individual';
