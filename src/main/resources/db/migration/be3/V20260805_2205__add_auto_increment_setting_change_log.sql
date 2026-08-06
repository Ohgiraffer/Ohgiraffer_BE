-- V20260805_XXXX__add_auto_increment_setting_change_log.sql

ALTER TABLE `setting_change_log`
    MODIFY COLUMN `setting_log_id` BIGINT NOT NULL AUTO_INCREMENT;