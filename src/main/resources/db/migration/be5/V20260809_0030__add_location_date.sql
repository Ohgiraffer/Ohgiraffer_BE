ALTER TABLE trainee_location
    ADD COLUMN location_date DATE NULL
        COMMENT '위치 등록 기준 일자';

UPDATE trainee_location
SET space_id = NULL,
    location_date = NULL;

CREATE INDEX idx_trainee_location_space_date
    ON trainee_location (space_id, location_date);