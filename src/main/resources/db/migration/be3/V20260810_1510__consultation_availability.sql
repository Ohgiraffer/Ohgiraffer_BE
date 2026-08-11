-- V20260810_1000__consultation_availability.sql

START TRANSACTION;

-- 상담 기록(운영진이 상담 후 남기는 메모) 컬럼 추가
ALTER TABLE `consultation`
    ADD COLUMN `counselor_note` TEXT NULL COMMENT '상담 기록' AFTER `content`;

-- 상담자가 설정한 상담 가능 "일자"
CREATE TABLE `counselor_available_date` (
                                            `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '상담가능일아이디',
                                            `counselor_id`   BIGINT   NOT NULL COMMENT '상담자아이디',
                                            `available_date` DATE     NOT NULL COMMENT '가능일자',
                                            `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `UQ_COUNSELOR_AVAILABLE_DATE` (`counselor_id`, `available_date`)
);

-- 해당 일자의 30분 단위 가능 "시간"
CREATE TABLE `counselor_available_time` (
                                            `id`                BIGINT NOT NULL AUTO_INCREMENT COMMENT '상담가능시간아이디',
                                            `available_date_id` BIGINT NOT NULL COMMENT '상담가능일아이디',
                                            `start_time`        TIME   NOT NULL COMMENT '시작시간',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `UQ_COUNSELOR_AVAILABLE_TIME` (`available_date_id`, `start_time`)
);

ALTER TABLE `counselor_available_date`
    ADD CONSTRAINT `FK_users_TO_counselor_available_date_1`
        FOREIGN KEY (`counselor_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

ALTER TABLE `counselor_available_time`
    ADD CONSTRAINT `FK_counselor_available_date_TO_counselor_available_time_1`
        FOREIGN KEY (`available_date_id`) REFERENCES `counselor_available_date` (`id`) ON DELETE CASCADE;

-- 동일 상담자의 동일 시각 중복 예약 방지 (write-time integrity)
-- 주의: 취소된 상담도 unique 대상에 포함됨. 취소 시 해당 슬롯을 다시 열어주려면
--       cancel() 처리에서 scheduled_at을 null로 비우는 정책을 팀에서 정해야 함.
ALTER TABLE `consultation`
    ADD CONSTRAINT `UQ_CONSULTATION_COUNSELOR_SCHEDULED`
        UNIQUE (`counselor_id`, `scheduled_at`);

CREATE INDEX `IDX_CONSULTATION_REQUESTER` ON `consultation` (`requester_id`);
CREATE INDEX `IDX_CONSULTATION_COUNSELOR_STATUS` ON `consultation` (`counselor_id`, `status`);

COMMIT;