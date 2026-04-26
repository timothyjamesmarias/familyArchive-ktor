CREATE TABLE families (
    id BIGSERIAL PRIMARY KEY,
    gedcom_id VARCHAR(50) UNIQUE,
    marriage_date_string VARCHAR(255),
    marriage_date_parsed TIMESTAMP,
    marriage_place_id BIGINT,
    divorce_date_string VARCHAR(255),
    divorce_date_parsed TIMESTAMP,
    gedcom_raw_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_families_gedcom_id ON families(gedcom_id);
CREATE INDEX idx_families_marriage_place ON families(marriage_place_id);
CREATE INDEX idx_families_gedcom_data ON families USING GIN (gedcom_raw_data);

-- Trigger to auto-update updated_at
CREATE TRIGGER update_families_updated_at BEFORE UPDATE ON families
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
