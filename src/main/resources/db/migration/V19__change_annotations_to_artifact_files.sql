-- Change annotations to reference artifact_files instead of artifacts
-- This allows each image file to have its own set of annotations

-- Drop existing constraint and index
DROP INDEX IF EXISTS idx_annotations_artifact_id;
ALTER TABLE annotations DROP CONSTRAINT IF EXISTS annotations_artifact_id_fkey;

-- Remove old column
ALTER TABLE annotations DROP COLUMN artifact_id;

-- Add new column referencing artifact_files
ALTER TABLE annotations ADD COLUMN artifact_file_id BIGINT NOT NULL REFERENCES artifact_files(id) ON DELETE CASCADE;

-- Add index for lookups
CREATE INDEX idx_annotations_artifact_file_id ON annotations(artifact_file_id);