CREATE TABLE team_outbox
(
    team_outbox_id     BIGINT        NOT NULL AUTO_INCREMENT,
    type               VARCHAR(50)   NOT NULL,
    status             VARCHAR(30)   NOT NULL,
    payload            TEXT          NOT NULL,
    retry_count        INT           NOT NULL DEFAULT 0,
    last_error_message VARCHAR(1000) NULL,
    next_retry_at      DATETIME      NULL,
    created_at         DATETIME      NOT NULL,
    updated_at         DATETIME      NOT NULL,
    CONSTRAINT pk_team_outbox PRIMARY KEY (team_outbox_id)
);

CREATE INDEX idx_team_outbox_retry
    ON team_outbox (status, next_retry_at, team_outbox_id);