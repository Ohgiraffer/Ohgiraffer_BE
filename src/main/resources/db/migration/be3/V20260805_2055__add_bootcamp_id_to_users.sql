-- V20260805_1941__add_bootcamp_id_to_users.sql

ALTER TABLE `users`
    ADD COLUMN `bootcamp_id` BIGINT NULL COMMENT '소속부트캠프아이디' AFTER `status`;

ALTER TABLE `users`
    ADD CONSTRAINT `FK_bootcamp_info_TO_users_1`
        FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);