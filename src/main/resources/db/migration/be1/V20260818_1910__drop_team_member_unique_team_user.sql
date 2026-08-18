ALTER TABLE team_member
DROP INDEX UQ_TEAM_MEMBER;

CREATE INDEX idx_team_member_team_user
    ON team_member (team_id, user_id);