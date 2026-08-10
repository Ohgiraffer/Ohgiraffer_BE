ALTER TABLE team
DROP INDEX uq_team_name;

ALTER TABLE team
    ADD CONSTRAINT uq_team_period_name UNIQUE (team_period_id, name);

ALTER TABLE chat_channel
    ADD CONSTRAINT uq_chat_channel_team_id UNIQUE (team_id);