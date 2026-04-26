CREATE TABLE individuals (
    id BIGSERIAL PRIMARY KEY,
    gedcom_id VARCHAR(50) UNIQUE,
    given_name VARCHAR(255),
    surname VARCHAR(255),
    sex CHAR(1),
    gedcom_raw_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_individuals_gedcom_id ON individuals(gedcom_id);
CREATE INDEX idx_individuals_surname ON individuals(surname);
CREATE INDEX idx_individuals_name_search ON individuals USING GIN (to_tsvector('english', COALESCE(given_name, '') || ' ' || COALESCE(surname, '')));
CREATE INDEX idx_individuals_gedcom_data ON individuals USING GIN (gedcom_raw_data);

-- Trigger to auto-update updated_at
CREATE TRIGGER update_individuals_updated_at BEFORE UPDATE ON individuals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
