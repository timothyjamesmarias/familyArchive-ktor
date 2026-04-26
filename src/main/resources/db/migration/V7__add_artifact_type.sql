-- Add artifact_type to artifacts table
ALTER TABLE artifacts ADD COLUMN artifact_type VARCHAR(50) NOT NULL DEFAULT 'document';

-- Create index for querying by type
CREATE INDEX idx_artifacts_type ON artifacts(artifact_type);
