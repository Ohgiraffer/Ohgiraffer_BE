ALTER TABLE team_outbox
    ADD COLUMN processing_token VARCHAR(36) NULL;