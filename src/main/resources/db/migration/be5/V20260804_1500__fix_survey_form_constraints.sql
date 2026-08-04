ALTER TABLE survey_form
    DROP FOREIGN KEY FK_users_TO_survey_form_1,

    MODIFY COLUMN survey_form_id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '설문 폼 아이디',

    MODIFY COLUMN created_by BIGINT NOT NULL
        COMMENT '생성자 아이디',

    ADD CONSTRAINT FK_users_TO_survey_form_1
        FOREIGN KEY (created_by)
        REFERENCES users (user_id);
