-- ============================================================
-- chat_message_mirror에 created_at, updated_at 추가
-- 목적: ChatMessageMirrorJpaEntity가 BaseTimeEntity를 상속하면서 필수가 됨
-- 작성일: 2026-08-04
-- ============================================================

ALTER TABLE `chat_message_mirror`
    ADD COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시' AFTER `is_edited`;

ALTER TABLE `chat_message_mirror`
    ADD COLUMN `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시' AFTER `created_at`;