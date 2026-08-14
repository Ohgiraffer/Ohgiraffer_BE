CREATE TABLE `approval_applicant_profile` (
                                              `approval_applicant_profile_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재신청자프로필아이디',
                                              `user_id` BIGINT NOT NULL COMMENT '사용자아이디',
                                              `birth_date` DATE NOT NULL COMMENT '생년월일',
                                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                                              `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

                                              CONSTRAINT `PK_APPROVAL_APPLICANT_PROFILE`
                                                  PRIMARY KEY (`approval_applicant_profile_id`),

                                              CONSTRAINT `UQ_APPROVAL_APPLICANT_PROFILE_USER_ID`
                                                  UNIQUE (`user_id`),

                                              CONSTRAINT `FK_USER_TO_APPROVAL_APPLICANT_PROFILE_1`
                                                  FOREIGN KEY (`user_id`)
                                                      REFERENCES `users` (`user_id`)
);