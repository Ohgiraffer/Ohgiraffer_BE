CREATE TABLE team_period (
                             team_period_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             start_date DATE NOT NULL,
                             end_date DATE NOT NULL,
                             archived_at DATETIME(6) NULL,
                             deleted_at DATETIME(6) NULL,
                             created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                             CONSTRAINT chk_team_period_date CHECK (start_date <= end_date)
);

ALTER TABLE team
    ADD COLUMN team_period_id BIGINT NULL;

ALTER TABLE team
    ADD CONSTRAINT fk_team_team_period
        FOREIGN KEY (team_period_id)
            REFERENCES team_period(team_period_id);