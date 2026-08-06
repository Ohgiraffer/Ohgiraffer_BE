-- attendance_period_summary FK 해제
ALTER TABLE `attendance_period_summary` DROP FOREIGN KEY `FK_attendance_period_TO_attendance_period_summary_1`;

-- attendance_policy AUTO_INCREMENT
ALTER TABLE `attendance_policy` MODIFY COLUMN `attendance_policy_id` BIGINT NOT NULL AUTO_INCREMENT;

-- attendance_period AUTO_INCREMENT
ALTER TABLE `attendance_period` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT;

-- attendance_period_summary FK 복원
ALTER TABLE `attendance_period_summary`
    ADD CONSTRAINT `FK_attendance_period_TO_attendance_period_summary_1`
        FOREIGN KEY (`period_id`) REFERENCES `attendance_period` (`id`) ON DELETE CASCADE;