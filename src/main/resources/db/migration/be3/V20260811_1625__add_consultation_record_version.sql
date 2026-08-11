START TRANSACTION;

ALTER TABLE `consultation`
    ADD COLUMN `record_version` INT NOT NULL DEFAULT 0 COMMENT '메모 저장 버전 (AI 요약 결과 유효성 확인용)' AFTER `ai_brief`;

COMMIT;