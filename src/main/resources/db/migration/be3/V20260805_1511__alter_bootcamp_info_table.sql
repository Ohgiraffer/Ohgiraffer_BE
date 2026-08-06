-- 1. FK 해제
ALTER TABLE `attendance_policy` DROP FOREIGN KEY `FK_bootcamp_info_TO_attendance_policy_1`;
ALTER TABLE `attendance_period` DROP FOREIGN KEY `FK_bootcamp_info_TO_attendance_period_1`;

-- 2. bootcamp_info 컬럼 수정
ALTER TABLE `bootcamp_info` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE `bootcamp_info` MODIFY COLUMN `org_name` VARCHAR(255) NOT NULL COMMENT '기관명';
ALTER TABLE `bootcamp_info` MODIFY COLUMN `pro_name` VARCHAR(255) NOT NULL COMMENT '과정명';
ALTER TABLE `bootcamp_info` MODIFY COLUMN `start_date` DATE NOT NULL;
ALTER TABLE `bootcamp_info` MODIFY COLUMN `end_date` DATE NOT NULL;

-- 3. FK 복원
ALTER TABLE `attendance_policy` ADD CONSTRAINT `FK_bootcamp_info_TO_attendance_policy_1` FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);
ALTER TABLE `attendance_period` ADD CONSTRAINT `FK_bootcamp_info_TO_attendance_period_1` FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);