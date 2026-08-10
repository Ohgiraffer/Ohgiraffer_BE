UPDATE space_reservation
SET max_capacity = 1
WHERE max_capacity IS NULL
   OR max_capacity < 1;

ALTER TABLE space_reservation
    MODIFY COLUMN max_capacity INT NOT NULL
    COMMENT '최대 수용 인원';

ALTER TABLE space_reservation
    ADD CONSTRAINT uq_space_reservation_space_name
        UNIQUE (space_name);