ALTER TABLE pga_value
    DROP CONSTRAINT IF EXISTS uq_pga_lat_long;

DELETE FROM pga_value duplicate
    USING pga_value original
WHERE duplicate.id > original.id
  AND duplicate.latitude = original.latitude
  AND duplicate.longitude = original.longitude;

ALTER TABLE pga_value
    ADD CONSTRAINT uq_pga_lat_long UNIQUE (latitude, longitude);
