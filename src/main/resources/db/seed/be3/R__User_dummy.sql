-- CampFlow 더미 사용자 데이터 (users)
-- 훈련생 40명 / 강사 2명 / 운영진(매니저) 5명 = 총 47명

USE campflow;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `users`;

INSERT INTO `users`
(`user_id`, `name`, `phone`, `email`, `role`, `profile_img`, `password`, `need_reset_pw`, `notification_on`, `join_date`, `leave_date`, `status`)
VALUES
    (1, '곽시윤', '010-2824-1409', 'student01@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (2, '김가영', '010-5506-5012', 'student02@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (3, '김민지', '010-4657-3286', 'student03@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (4, '김시은', '010-2679-9935', 'student04@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (5, '김태완', '010-2424-7912', 'student05@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (6, '서민지', '010-1520-1488', 'student06@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (7, '손윤서', '010-2535-4582', 'student07@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (8, '안현', '010-4811-9279', 'student08@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (9, '윤영지', '010-1434-4257', 'student09@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (10, '이주은', '010-9928-7873', 'student10@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (11, '이홍근', '010-4611-8359', 'student11@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (12, '정지훈', '010-5557-1106', 'student12@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (13, '고성민', '010-3615-7924', 'student13@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (14, '김동현', '010-6574-5552', 'student14@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (15, '김동훈', '010-3547-4527', 'student15@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (16, '김민섭', '010-6514-2674', 'student16@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (17, '김용준', '010-2519-7224', 'student17@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (18, '김재원', '010-2584-6881', 'student18@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (19, '김진도', '010-6635-5333', 'student19@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (20, '김채린', '010-1711-8527', 'student20@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (21, '김현지', '010-9785-3045', 'student21@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (22, '모성진', '010-7201-2291', 'student22@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (23, '박민서', '010-5803-6925', 'student23@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (24, '박정민', '010-4150-2139', 'student24@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (25, '박종준', '010-1750-4733', 'student25@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (26, '배정현', '010-5741-2307', 'student26@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (27, '백승재', '010-4814-2654', 'student27@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (28, '서정림', '010-7227-5554', 'student28@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (29, '서주원', '010-8428-6977', 'student29@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (30, '안정수', '010-3664-7065', 'student30@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (31, '유강현', '010-6820-4432', 'student31@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (32, '윤종호', '010-5374-2169', 'student32@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (33, '이강욱', '010-3803-9751', 'student33@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (34, '이채연', '010-5010-3677', 'student34@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (35, '이태연', '010-8573-7216', 'student35@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (36, '이하연', '010-5422-4598', 'student36@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (37, '임수영', '010-6313-1916', 'student37@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (38, '전지원', '010-4752-1525', 'student38@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (39, '정유지', '010-6168-7572', 'student39@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (40, '지석범', '010-5386-2084', 'student40@campflow.test', 'STUDENT', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-23', NULL, 'ACTIVE'),
    (41, '조평훈', '010-4456-6155', 'instructor01@campflow.test', 'INSTRUCTOR', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-21', NULL, 'ACTIVE'),
    (42, '남효정', '010-4483-9179', 'instructor02@campflow.test', 'INSTRUCTOR', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-02-21', NULL, 'ACTIVE'),
    (43, '고은해', '010-7482-8517', 'manager01@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (44, '김아름', '010-3340-5339', 'manager02@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (45, '유수정', '010-3287-5040', 'manager03@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (46, '이정기', '010-9830-5304', 'manager04@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE'),
    (47, '최자경', '010-8019-7543', 'manager05@campflow.test', 'MANAGER', NULL, '$2a$12$.UXrD41avDmOR85e3Sm7e.IYGNSej6NVekeaEvGHalA2Cy4NDyTj6', TRUE, TRUE, '2026-01-01', NULL, 'ACTIVE');

SET FOREIGN_KEY_CHECKS = 1;