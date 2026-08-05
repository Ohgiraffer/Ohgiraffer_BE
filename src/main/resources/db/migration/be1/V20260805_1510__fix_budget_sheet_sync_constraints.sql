-- ============================================================
-- 예산 시트 동기화 제약 보강
-- ============================================================
-- - 기존 budget_allocation 행의 remaining_amount 값을 total_amount - used_amount로 백필한다.
-- - external_sheet_link.domain 단건 조회 계약을 보장하기 위해 UNIQUE 제약을 추가한다.
-- ============================================================

UPDATE `budget_allocation`
SET `remaining_amount` = `total_amount` - `used_amount`
WHERE `remaining_amount` = 0;

DELETE t1
FROM `external_sheet_link` t1
JOIN `external_sheet_link` t2
  ON t1.`domain` = t2.`domain`
 AND t1.`sheet_link_id` > t2.`sheet_link_id`;

ALTER TABLE `external_sheet_link`
    ADD CONSTRAINT `UQ_EXTERNAL_SHEET_LINK_DOMAIN` UNIQUE (`domain`);