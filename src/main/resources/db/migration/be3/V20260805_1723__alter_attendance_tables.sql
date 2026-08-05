-- 1. attendance_policy FK 해제
ALTER TABLE `attendance_policy` DROP FOREIGN KEY `FK_bootcamp_info_TO_attendance_policy_1`;

-- 2. attendance_policy 컬럼 삭제
ALTER TABLE `attendance_policy` DROP COLUMN `late_early_leave_conversion_count`;
ALTER TABLE `attendance_policy` DROP COLUMN `total_expulsion_pct`;

-- 3. attendance_policy FK 복원
ALTER TABLE `attendance_policy`
    ADD CONSTRAINT `FK_bootcamp_info_TO_attendance_policy_1`
        FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);

-- 4. attendance_period FK 해제
ALTER TABLE `attendance_period` DROP FOREIGN KEY `FK_bootcamp_info_TO_attendance_period_1`;

-- 5. attendance_period 컬럼 삭제
ALTER TABLE `attendance_period` DROP COLUMN `scheduled_class_days`;

-- 6. attendance_period FK 복원
ALTER TABLE `attendance_period`
    ADD CONSTRAINT `FK_bootcamp_info_TO_attendance_period_1`
        FOREIGN KEY (`bootcamp_id`) REFERENCES `bootcamp_info` (`id`);