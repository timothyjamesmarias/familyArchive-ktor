-- Add soft delete support to individuals, families, and family_members tables
-- This allows records to be marked as deleted without physically removing them from the database

-- Add deleted_at column to individuals table
ALTER TABLE individuals
    ADD COLUMN deleted_at TIMESTAMP;

-- Add index for efficient querying of non-deleted records
CREATE INDEX idx_individuals_deleted_at ON individuals(deleted_at)
    WHERE deleted_at IS NULL;

-- Add deleted_at column to families table
ALTER TABLE families
    ADD COLUMN deleted_at TIMESTAMP;

-- Add index for efficient querying of non-deleted records
CREATE INDEX idx_families_deleted_at ON families(deleted_at)
    WHERE deleted_at IS NULL;

-- Add deleted_at column to family_members table
ALTER TABLE family_members
    ADD COLUMN deleted_at TIMESTAMP;

-- Add index for efficient querying of non-deleted records
CREATE INDEX idx_family_members_deleted_at ON family_members(deleted_at)
    WHERE deleted_at IS NULL;

-- Note: By default, all existing records have deleted_at = NULL (not deleted)
-- Queries should filter WHERE deleted_at IS NULL to get active records