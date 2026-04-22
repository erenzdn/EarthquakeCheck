CREATE TABLE IF NOT EXISTS building (
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(255),
    year_built INTEGER,
    building_type VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS evaluation_result (
    id BIGSERIAL PRIMARY KEY,
    risk_class VARCHAR(10) NOT NULL,
    message VARCHAR(500) NOT NULL,
    safety_grade_percentage INTEGER NOT NULL,
    evaluated_at TIMESTAMP NOT NULL,
    building_id BIGINT NOT NULL,
    CONSTRAINT fk_evaluation_building
        FOREIGN KEY (building_id)
        REFERENCES building (id)
        ON DELETE CASCADE
);
