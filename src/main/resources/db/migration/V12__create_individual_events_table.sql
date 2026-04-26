CREATE TABLE individual_events (
    id BIGSERIAL PRIMARY KEY,
    individual_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    date_string VARCHAR(255),
    date_parsed TIMESTAMP,
    place_id BIGINT,
    description TEXT,
    gedcom_raw_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_individual_events_individual FOREIGN KEY (individual_id) REFERENCES individuals(id) ON DELETE CASCADE,
    CONSTRAINT fk_individual_events_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE SET NULL
);

-- Indexes for common queries
CREATE INDEX idx_individual_events_individual ON individual_events(individual_id);
CREATE INDEX idx_individual_events_type ON individual_events(individual_id, event_type);
CREATE INDEX idx_individual_events_date ON individual_events(date_parsed);
CREATE INDEX idx_individual_events_place ON individual_events(place_id);
CREATE INDEX idx_individual_events_gedcom_data ON individual_events USING GIN (gedcom_raw_data);

-- Trigger to auto-update updated_at
CREATE TRIGGER update_individual_events_updated_at BEFORE UPDATE ON individual_events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
