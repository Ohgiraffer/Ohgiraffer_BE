-- ============================================================
-- chat_message_mirror 순서 보장 컬럼 추가
-- 목적: update/delete 웹훅이 순서가 뒤바뀌어 도착했을 때, 오래된 이벤트가
--       최신 상태를 덮어쓰는 것을 막기 위한 비교 기준 컬럼 (Sendbird 이벤트 발생 시각)
--       BaseTimeEntity의 updated_at(JPA 자동 갱신 시각)과는 다른 목적의 컬럼임에 유의
-- 작성일: 2026-08-11
-- ============================================================

ALTER TABLE `chat_message_mirror`
    ADD COLUMN `last_event_at` DATETIME(6) NULL COMMENT '가장 최근 반영된 웹훅 이벤트 시각 (순서 검증용)' AFTER `is_edited`;