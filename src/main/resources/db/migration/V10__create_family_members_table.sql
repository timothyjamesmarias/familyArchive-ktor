CREATE TABLE family_members (
    family_id BIGINT NOT NULL,
    individual_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    child_order INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (family_id, individual_id, role),
    CONSTRAINT fk_family_members_family FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE CASCADE,
    CONSTRAINT fk_family_members_individual FOREIGN KEY (individual_id) REFERENCES individuals(id) ON DELETE CASCADE,
    CONSTRAINT chk_role CHECK (role IN ('FATHER', 'MOTHER', 'CHILD'))
);

-- Indexes for common queries
CREATE INDEX idx_family_members_family ON family_members(family_id);
CREATE INDEX idx_family_members_individual ON family_members(individual_id);
CREATE INDEX idx_family_members_role ON family_members(family_id, role);
CREATE INDEX idx_family_members_child_order ON family_members(family_id, child_order) WHERE role = 'CHILD';
