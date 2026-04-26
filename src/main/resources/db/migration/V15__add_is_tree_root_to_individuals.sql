-- Add is_tree_root column to individuals table
ALTER TABLE individuals ADD COLUMN is_tree_root BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for quick lookup of tree root individuals
CREATE INDEX idx_individuals_is_tree_root ON individuals(is_tree_root) WHERE is_tree_root = TRUE;