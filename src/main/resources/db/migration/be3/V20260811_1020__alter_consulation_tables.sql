START TRANSACTION;

UPDATE `consultation` SET `status` = 'PENDING' WHERE `status` IN ('CHECKED', 'APPROVED');

ALTER TABLE `consultation`
    MODIFY COLUMN `status` ENUM('PENDING','CANCELLED','COMPLETED') NOT NULL DEFAULT 'PENDING' COMMENT '상태';

ALTER TABLE `consultation` DROP COLUMN `cancelled_by`;
ALTER TABLE `consultation` DROP COLUMN `cancel_reason`;

COMMIT;