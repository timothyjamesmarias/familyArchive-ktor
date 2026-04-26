CREATE TABLE places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    normalized_name VARCHAR(500),
    city VARCHAR(255),
    state_province VARCHAR(255),
    country VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    gedcom_raw_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_places_normalized_name ON places(normalized_name);
CREATE INDEX idx_places_search ON places USING GIN (to_tsvector('english', name));
CREATE INDEX idx_places_city ON places(city);
CREATE INDEX idx_places_country ON places(country);
CREATE INDEX idx_places_gedcom_data ON places USING GIN (gedcom_raw_data);

-- Trigger to auto-update updated_at
CREATE TRIGGER update_places_updated_at BEFORE UPDATE ON places
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add foreign key to families table
ALTER TABLE families
    ADD CONSTRAINT fk_families_marriage_place FOREIGN KEY (marriage_place_id) REFERENCES places(id) ON DELETE SET NULL;
