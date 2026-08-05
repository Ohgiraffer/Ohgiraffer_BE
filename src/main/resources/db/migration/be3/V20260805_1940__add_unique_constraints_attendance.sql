ALTER TABLE `attendance_policy`
    ADD CONSTRAINT `UQ_attendance_policy_bootcamp_id` UNIQUE (`bootcamp_id`);

ALTER TABLE `attendance_period`
    ADD CONSTRAINT `UQ_attendance_period_bootcamp_id_period_no` UNIQUE (`bootcamp_id`, `period_no`);