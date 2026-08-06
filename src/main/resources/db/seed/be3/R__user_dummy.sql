-- CampFlow 더미 사용자 데이터 (users)
-- 운영진(매니저) 5명

USE campflow;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `users`;

INSERT INTO `users`
(`user_id`, `bootcamp_id`, `name`, `phone`, `email`, `role`, `profile_img`, `password`, `need_reset_pw`, `notification_on`, `join_date`, `leave_date`, `status`)
VALUES
    (1, null, '고은해', '010-7482-8517', 'manager01@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (2, null, '김아름', '010-3340-5339', 'manager02@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (3, null, '유수정', '010-3287-5040', 'manager03@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (4, null, '이정기', '010-9830-5304', 'manager04@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (5, null, '최자경', '010-8019-7543', 'manager05@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE');

SET FOREIGN_KEY_CHECKS = 1;
