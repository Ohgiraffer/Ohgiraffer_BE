-- ============================================================
-- 전자결재/예산 테이블 PK AUTO_INCREMENT 적용
-- - FK로 참조 중인 컬럼은 FK를 잠시 제거한 뒤 변경하고 다시 추가한다.
-- ============================================================

ALTER TABLE `approval_history`
DROP FOREIGN KEY `FK_approval_request_TO_approval_history_1`;

ALTER TABLE `approval_attachment`
DROP FOREIGN KEY `FK_approval_request_TO_approval_attachment_1`;

ALTER TABLE `approval_leave_detail`
DROP FOREIGN KEY `FK_approval_request_TO_approval_leave_detail_1`;

ALTER TABLE `approval_purchase_detail`
DROP FOREIGN KEY `FK_approval_request_TO_approval_purchase_detail_1`;

ALTER TABLE `budget_allocation`
DROP FOREIGN KEY `FK_budget_category_TO_budget_allocation_1`;

ALTER TABLE `approval_purchase_detail`
DROP FOREIGN KEY `FK_budget_category_TO_approval_purchase_detail_1`;

ALTER TABLE `approval_request`
    MODIFY COLUMN `approval_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재요청아이디';

ALTER TABLE `approval_leave_detail`
    MODIFY COLUMN `leave_detail_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '휴가상세아이디';

ALTER TABLE `approval_purchase_detail`
    MODIFY COLUMN `purchase_detail_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '구매상세아이디';

ALTER TABLE `approval_history`
    MODIFY COLUMN `approval_history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재이력아이디';

ALTER TABLE `approval_attachment`
    MODIFY COLUMN `attachment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '첨부파일아이디';

ALTER TABLE `budget_category`
    MODIFY COLUMN `budget_category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '예산카테고리아이디';

ALTER TABLE `budget_allocation`
    MODIFY COLUMN `budget_allocation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '예산배정아이디';

ALTER TABLE `approval_history`
    ADD CONSTRAINT `FK_approval_request_TO_approval_history_1`
        FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`)
            ON DELETE CASCADE;

ALTER TABLE `approval_attachment`
    ADD CONSTRAINT `FK_approval_request_TO_approval_attachment_1`
        FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`)
            ON DELETE CASCADE;

ALTER TABLE `approval_leave_detail`
    ADD CONSTRAINT `FK_approval_request_TO_approval_leave_detail_1`
        FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`)
            ON DELETE CASCADE;

ALTER TABLE `approval_purchase_detail`
    ADD CONSTRAINT `FK_approval_request_TO_approval_purchase_detail_1`
        FOREIGN KEY (`approval_id`) REFERENCES `approval_request` (`approval_id`)
            ON DELETE CASCADE;

ALTER TABLE `budget_allocation`
    ADD CONSTRAINT `FK_budget_category_TO_budget_allocation_1`
        FOREIGN KEY (`budget_category_id`) REFERENCES `budget_category` (`budget_category_id`);

ALTER TABLE `approval_purchase_detail`
    ADD CONSTRAINT `FK_budget_category_TO_approval_purchase_detail_1`
        FOREIGN KEY (`budget_category_id`) REFERENCES `budget_category` (`budget_category_id`);