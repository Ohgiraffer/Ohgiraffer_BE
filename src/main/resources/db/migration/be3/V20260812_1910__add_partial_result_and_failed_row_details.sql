START TRANSACTION;

-- result에 PARTIAL(일부 성공) 추가
ALTER TABLE `sheet_sync_log`
    MODIFY COLUMN `result` ENUM ('SUCCESS', 'PARTIAL', 'FAIL') NULL COMMENT '동기화 결과';

-- 실패 행 상세(행 번호 + 사유) 저장용 컬럼
ALTER TABLE `sheet_sync_log`
    ADD COLUMN `failed_row_details` JSON NULL COMMENT '실패 행 상세 (행 번호 + 사유)' AFTER `diff_summary`;

COMMIT;