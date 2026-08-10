ALTER TABLE team
DROP INDEX uq_team_name;

ALTER TABLE team
    ADD CONSTRAINT uq_team_period_name UNIQUE (team_period_id, name);

CREATE TEMPORARY TABLE duplicate_team_channel_keep AS
SELECT
    team_id,
    MIN(chat_channel_id) AS keep_chat_channel_id
FROM chat_channel
WHERE team_id IS NOT NULL
GROUP BY team_id
HAVING COUNT(*) > 1;

CREATE TEMPORARY TABLE duplicate_team_channel_discard AS
SELECT
    cc.chat_channel_id,
    cc.team_id,
    keep.keep_chat_channel_id
FROM chat_channel cc
         JOIN duplicate_team_channel_keep keep
              ON keep.team_id = cc.team_id
WHERE cc.chat_channel_id <> keep.keep_chat_channel_id;

INSERT INTO chat_channel_member (
    chat_channel_id,
    user_id,
    joined_at,
    left_at,
    last_read_message_id
)
SELECT
    discard.keep_chat_channel_id,
    member.user_id,
    member.joined_at,
    member.left_at,
    member.last_read_message_id
FROM chat_channel_member member
         JOIN duplicate_team_channel_discard discard
              ON discard.chat_channel_id = member.chat_channel_id
WHERE NOT EXISTS (
    SELECT 1
    FROM chat_channel_member existing
    WHERE existing.chat_channel_id = discard.keep_chat_channel_id
      AND existing.user_id = member.user_id
);

DELETE cc
FROM chat_channel cc
JOIN duplicate_team_channel_discard discard
    ON discard.chat_channel_id = cc.chat_channel_id;

DROP TEMPORARY TABLE duplicate_team_channel_discard;

DROP TEMPORARY TABLE duplicate_team_channel_keep;

ALTER TABLE chat_channel
    ADD CONSTRAINT uq_chat_channel_team_id UNIQUE (team_id);