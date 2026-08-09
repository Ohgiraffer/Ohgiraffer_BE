ALTER TABLE team_member
    ADD COLUMN active_user_id BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN left_at IS NULL THEN user_id
                ELSE NULL
                END
            ) STORED;

ALTER TABLE team_member
    ADD CONSTRAINT uq_team_member_active_user UNIQUE (active_user_id);