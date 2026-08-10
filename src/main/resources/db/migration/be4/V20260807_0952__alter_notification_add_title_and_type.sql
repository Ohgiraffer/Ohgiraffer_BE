-- ============================================================
-- notification 보강
-- 1. title 컬럼 추가 (Figma 확인 후 확정, 알림 제목 표시용)
-- 2. notification_type ENUM 값 변경 (CONSULTATION_CANCEL -> CONSULTATION)
-- 작성일: 2026-08-07
-- ============================================================

ALTER TABLE `notification`
    ADD COLUMN `title` VARCHAR(100) NOT NULL COMMENT '알림제목' AFTER `notification_type`;

ALTER TABLE `notification`
    MODIFY COLUMN `notification_type`
    ENUM('APPROVAL_REQUEST','NOTICE_CONFIRMATION','APPROVAL_RESULT','ATTENDANCE_RISK','CHAT_MENTION','NOTICE','CALENDAR_EVENT','CONSULTATION','SUBMISSION_DEADLINE')
    NOT NULL
    COMMENT '알림유형';