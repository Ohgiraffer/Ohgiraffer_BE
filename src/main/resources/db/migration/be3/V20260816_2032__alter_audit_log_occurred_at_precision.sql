ALTER TABLE `audit_log`
    MODIFY COLUMN `occurred_at` DATETIME(6) NOT NULL COMMENT '발생일시';