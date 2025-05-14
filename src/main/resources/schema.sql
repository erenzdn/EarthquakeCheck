-- Buildings tablosu
CREATE TABLE IF NOT EXISTS buildings (
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(255),
    building_age INTEGER,
    floor_count INTEGER,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    safety_grade VARCHAR(10),
    nearest_assembly_area VARCHAR(255)
);

-- Evaluation Results tablosu
CREATE TABLE IF NOT EXISTS evaluation_results (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT REFERENCES buildings(id),
    safety_grade VARCHAR(10),
    nearest_assembly_area VARCHAR(255),
    evaluation_date TIMESTAMP,
    evaluation_notes TEXT
); 