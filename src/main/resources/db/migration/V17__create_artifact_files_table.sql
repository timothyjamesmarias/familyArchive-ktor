CREATE TABLE artifact_files (
    id BIGSERIAL PRIMARY KEY,
    artifact_id BIGINT NOT NULL,
    file_sequence INTEGER NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    thumbnail_path VARCHAR(1000),
    thumbnail_size VARCHAR(50),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_artifact_files_artifact
        FOREIGN KEY (artifact_id) REFERENCES artifacts(id)
        ON DELETE CASCADE
);

-- Trigger to auto-update updated_at column
CREATE TRIGGER update_artifact_files_updated_at BEFORE UPDATE ON artifact_files
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Index on artifact_id for lookups
CREATE INDEX idx_artifact_files_artifact_id ON artifact_files(artifact_id);

-- Index on artifact_id + file_sequence for ordering
CREATE INDEX idx_artifact_files_sequence ON artifact_files(artifact_id, file_sequence);

-- Unique constraint to prevent duplicate sequences per artifact
CREATE UNIQUE INDEX idx_artifact_files_unique_sequence ON artifact_files(artifact_id, file_sequence);

-- Add comments for documentation
COMMENT ON COLUMN artifact_files.thumbnail_path IS 'Storage path to the generated thumbnail image for this file';
COMMENT ON COLUMN artifact_files.thumbnail_size IS 'Size of the thumbnail in format WIDTHxHEIGHT (e.g., 300x300)';
