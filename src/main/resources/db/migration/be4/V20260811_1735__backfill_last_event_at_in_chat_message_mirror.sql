
UPDATE `chat_message_mirror`
SET `last_event_at` = `sent_at`
WHERE `last_event_at` IS NULL;