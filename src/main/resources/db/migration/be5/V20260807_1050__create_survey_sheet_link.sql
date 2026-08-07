CREATE TABLE survey_sheet_link
(
    survey_sheet_link_id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '설문 시트 연결 ID',

    survey_form_id BIGINT NOT NULL
        COMMENT '설문 폼 ID',

    spreadsheet_url VARCHAR(500) NOT NULL
        COMMENT 'Google Spreadsheet URL',

    spreadsheet_id VARCHAR(255) NOT NULL
        COMMENT 'Google Spreadsheet ID',

    spreadsheet_title VARCHAR(255) NOT NULL
        COMMENT 'Google Spreadsheet 문서 제목',

    sheet_gid BIGINT NOT NULL
        COMMENT 'Google Sheet gid',

    sheet_name VARCHAR(255) NOT NULL
        COMMENT '분석 대상 시트 이름',

    respondent_column VARCHAR(255) NOT NULL
        COMMENT '응답자 식별 컬럼명',

    submitted_at_column VARCHAR(255) NOT NULL
        COMMENT '응답 일시 컬럼명',

    linked_by BIGINT NOT NULL
        COMMENT '시트를 연결한 사용자 ID',

    created_at DATETIME(6) NOT NULL
        COMMENT '최초 연결 일시',

    updated_at DATETIME(6) NOT NULL
        COMMENT '마지막 연결 변경 일시',

    CONSTRAINT pk_survey_sheet_link
        PRIMARY KEY (survey_sheet_link_id),

    CONSTRAINT uq_survey_sheet_link_survey_form
        UNIQUE (survey_form_id),

    CONSTRAINT fk_survey_sheet_link_survey_form
        FOREIGN KEY (survey_form_id)
            REFERENCES survey_form (survey_form_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_survey_sheet_link_user
        FOREIGN KEY (linked_by)
            REFERENCES users (user_id)
);