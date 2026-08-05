-- ============================================================
-- 예산 시트 동기화를 위한 예산 테이블 제약 및 컬럼 보강
-- - Google Sheet의 잔여 예산 값을 저장하기 위해 remaining_amount 컬럼을 추가한다.
-- - 카테고리명 기준으로 저장/갱신하기 위해 budget_category.name에 UNIQUE 제약을 추가한다.
-- - 카테고리별 예산 배정은 1건만 유지하기 위해 budget_allocation.budget_category_id에 UNIQUE 제약을 추가한다.
-- - external_sheet_link는 설정 저장 시 신규 row 생성을 위해 AUTO_INCREMENT를 적용한다.
-- ============================================================

ALTER TABLE `budget_allocation`
    ADD COLUMN `remaining_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '잔여예산' AFTER `used_amount`;

ALTER TABLE `budget_category`
    ADD CONSTRAINT `UQ_BUDGET_CATEGORY_NAME` UNIQUE (`name`);

ALTER TABLE `budget_allocation`
    ADD CONSTRAINT `UQ_BUDGET_ALLOCATION_CATEGORY` UNIQUE (`budget_category_id`);

ALTER TABLE `external_sheet_link`
    MODIFY COLUMN `sheet_link_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '시트연동아이디';