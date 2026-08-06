ALTER TABLE `setting_change_log`
    ADD COLUMN `bootcamp_id` BIGINT NULL COMMENT '변경 대상 부트캠프 아이디' AFTER `setting_log_id`;

UPDATE `setting_change_log` SET `bootcamp_id` = 1 WHERE `bootcamp_id` IS NULL;

ALTER TABLE `setting_change_log`
    MODIFY COLUMN `bootcamp_id` BIGINT NOT NULL;

ALTER TABLE `setting_change_log`
    ADD CONSTRAINT `FK_bootcamp_info_TO_setting_change_log_1`
        FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);
