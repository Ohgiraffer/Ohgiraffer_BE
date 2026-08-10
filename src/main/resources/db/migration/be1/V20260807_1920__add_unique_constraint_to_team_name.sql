ALTER TABLE team
    ADD CONSTRAINT uq_team_name UNIQUE (name);