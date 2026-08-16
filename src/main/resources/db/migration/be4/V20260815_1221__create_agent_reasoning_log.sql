-- ============================================================
-- agent_reasoning_log 신설
-- AIOps 에이전트(aiops-relay)가 실제 함수 호출을 트리거한 시점의
-- AI 판단 근거(reasoning_summary)를 사후 추적 가능하도록 감사 기록으로 남긴다.
-- 매 턴이 아니라 "함수 호출이 실제로 트리거된 턴"만 저장 (노이즈/비용 방지).
-- 작성일: 2026-08-15
-- ============================================================

CREATE TABLE `agent_reasoning_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` VARCHAR(100) NOT NULL COMMENT '대화/처리 세션 식별자',
    `turn_id` VARCHAR(100) NOT NULL COMMENT '세션 내 턴 식별자',
    `function_name` VARCHAR(200) NOT NULL COMMENT '트리거된 함수/액션 이름',
    `reasoning_summary` TEXT NULL COMMENT 'AI가 이 함수를 호출하기로 판단한 근거 요약',
    `function_call_id` VARCHAR(100) NULL COMMENT '함수 호출 고유 ID',
    `success` BOOLEAN NOT NULL COMMENT '함수 호출 성공 여부',
    `latency_ms` BIGINT NULL COMMENT '함수 호출 소요 시간(ms)',
    `created_at` DATETIME NOT NULL COMMENT '기록 생성 시각',
    `updated_at` DATETIME NOT NULL COMMENT '기록 수정 시각 (BaseTimeEntity 공통 감사 컬럼)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `IDX_AGENT_REASONING_LOG_SESSION_ID`
    ON `agent_reasoning_log` (`session_id`);

CREATE INDEX `IDX_AGENT_REASONING_LOG_FUNCTION_CALL_ID`
    ON `agent_reasoning_log` (`function_call_id`);

CREATE INDEX `IDX_AGENT_REASONING_LOG_CREATED_AT`
    ON `agent_reasoning_log` (`created_at`);