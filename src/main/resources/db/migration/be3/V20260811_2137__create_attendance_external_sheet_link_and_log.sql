START TRANSACTION;

-- 1. sheet_sync_log.sheet_link_id FK 해제 (nullable로 바꾸기 위해)
ALTER TABLE `sheet_sync_log`
DROP FOREIGN KEY `FK_external_sheet_link_TO_sheet_sync_log_1`;

-- 2. sheet_link_id nullable로 변경 (다른 도메인 전용 컬럼, 출결은 사용 안 함)
ALTER TABLE `sheet_sync_log`
    MODIFY COLUMN `sheet_link_id` BIGINT NULL COMMENT '시트연동아이디(비출결 도메인용)';

-- 3. FK 복원
ALTER TABLE `sheet_sync_log`
    ADD CONSTRAINT `FK_external_sheet_link_TO_sheet_sync_log_1`
        FOREIGN KEY (`sheet_link_id`) REFERENCES `external_sheet_link` (`sheet_link_id`)
            ON DELETE CASCADE;

-- 4. 출결 전용 시트 연동 테이블
CREATE TABLE `attendance_external_sheet_link`
(
    `attendance_sheet_link_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '출결시트연동아이디',
    `sheet_url`                VARCHAR(500) NOT NULL COMMENT '시트URL',
    `tab_name`                 VARCHAR(100) NOT NULL COMMENT '탭명',
    `column_mapping`           JSON         NOT NULL COMMENT '컬럼매핑',
    `last_synced_at`           DATETIME     NULL COMMENT '마지막동기화일시',
    `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    `updated_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`attendance_sheet_link_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '출결 도메인 전용 시트 연동 설정';

-- 5. sheet_sync_log에 출결 전용 FK 컬럼 + 실행자/결과 컬럼 추가
ALTER TABLE `sheet_sync_log`
    ADD COLUMN `attendance_sheet_link_id` BIGINT       NULL COMMENT '출결시트연동아이디' AFTER `sheet_link_id`,
    ADD COLUMN `executor_id`              BIGINT       NULL COMMENT '실행자아이디(스케줄러 실행 시 NULL)' AFTER `synced_at`,
    ADD COLUMN `executor_name`            VARCHAR(100) NULL COMMENT '실행자 이름 스냅샷' AFTER `executor_id`,
    ADD COLUMN `result`                   ENUM ('SUCCESS', 'FAIL') NULL COMMENT '동기화 결과' AFTER `executor_name`;

ALTER TABLE `sheet_sync_log`
    ADD CONSTRAINT `FK_attendance_external_sheet_link_TO_sheet_sync_log_1`
        FOREIGN KEY (`attendance_sheet_link_id`) REFERENCES `attendance_external_sheet_link` (`attendance_sheet_link_id`)
            ON DELETE SET NULL;


COMMIT;