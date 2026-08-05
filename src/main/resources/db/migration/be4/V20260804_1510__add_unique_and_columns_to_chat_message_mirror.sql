-- ============================================================
-- chat_message_mirror 보강
-- 1. sendbird_message_id UNIQUE 제약 추가 (웹훅 멱등성 처리)
-- 2. deleted_at 컬럼 추가 (CHAT-015, CHAT-017 메시지/답글 소프트 삭제)
-- 3. is_edited 컬럼 추가 (CHAT-014, CHAT-016 메시지/답글 수정 표시)
-- 작성일: 2026-08-04
-- ============================================================

ALTER TABLE `chat_message_mirror`
    ADD CONSTRAINT `UQ_CHAT_MESSAGE_MIRROR_SENDBIRD_MESSAGE_ID`
        UNIQUE (`sendbird_message_id`);

ALTER TABLE `chat_message_mirror`
    ADD COLUMN `deleted_at` DATETIME NULL COMMENT '삭제일시' AFTER `sent_at`;

ALTER TABLE `chat_message_mirror`
    ADD COLUMN `is_edited` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '수정여부' AFTER `deleted_at`;