-- =========================================================
-- 제출함 도메인 구조 개선
--
-- 1. submission_box 개선
-- 2. submission_box_item 생성
-- 3. submission 개선
-- 4. submission_item_value 생성
-- =========================================================

-- submission_box 컬럼을 변경하기 전에
-- 해당 컬럼을 참조하는 외래 키를 먼저 제거
ALTER TABLE submission
DROP FOREIGN KEY FK_submission_box_TO_submission_1;

-- submission_box.created_by 컬럼 변경을 위해 기존 외래 키 제거
ALTER TABLE submission_box
DROP FOREIGN KEY FK_users_TO_submission_box_1;

-- =========================================================
-- 1. submission_box 수정
-- =========================================================

ALTER TABLE submission_box
    MODIFY COLUMN submission_box_id BIGINT NOT NULL AUTO_INCREMENT
    COMMENT '제출함 아이디',

    CHANGE COLUMN start_date start_at DATETIME NULL
    COMMENT '제출 시작 일시',

    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '생성 일시',

    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
    COMMENT '수정 일시';


-- 기존 데이터의 시작 시간이 NULL이면 현재 시각으로 보정
UPDATE submission_box
SET start_at = CURRENT_TIMESTAMP
WHERE start_at IS NULL;


-- 시작 시간과 생성자를 필수 값으로 변경
-- 기존 created_by가 NULL인 데이터가 있으면 이 구문에서 실패하므로
-- 실행 전 NULL 데이터가 없는지 확인해야 함
ALTER TABLE submission_box
    MODIFY COLUMN start_at DATETIME NOT NULL
    COMMENT '제출 시작 일시',

    MODIFY COLUMN created_by BIGINT NOT NULL
    COMMENT '생성자 아이디';

-- submission_box 생성자 외래 키 복구
ALTER TABLE submission_box
    ADD CONSTRAINT FK_users_TO_submission_box_1
        FOREIGN KEY (created_by)
            REFERENCES users (user_id);

-- 제출 대상, 지각 정책 값 검증
ALTER TABLE submission_box
    ADD CONSTRAINT CK_SUBMISSION_BOX_TARGET_SCOPE
        CHECK (target_scope IN ('INDIVIDUAL', 'TEAM')),

    ADD CONSTRAINT CK_SUBMISSION_BOX_LATE_POLICY
        CHECK (late_policy IN ('BLOCK', 'ALLOW')),

    ADD CONSTRAINT CK_SUBMISSION_BOX_PERIOD
        CHECK (start_at <= due_at);


CREATE INDEX IDX_SUBMISSION_BOX_PERIOD
    ON submission_box (start_at, due_at);

CREATE INDEX IDX_SUBMISSION_BOX_CREATED_BY
    ON submission_box (created_by);


-- =========================================================
-- 2. 제출 항목 테이블 생성
--
-- 예:
-- 발표자료  FILE  pdf,pptx
-- 소스코드  LINK
-- 시연영상  FILE  mp4,mov
-- =========================================================

CREATE TABLE submission_box_item
(
    submission_box_item_id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '제출 항목 아이디',

    submission_box_id BIGINT NOT NULL
        COMMENT '제출함 아이디',

    item_name VARCHAR(100) NOT NULL
        COMMENT '제출 항목명',

    item_type VARCHAR(20) NOT NULL
        COMMENT '제출 항목 유형(FILE, LINK)',

    allowed_file_types VARCHAR(255) NULL
        COMMENT '허용 파일 확장자 목록',

    is_required BOOLEAN NOT NULL DEFAULT TRUE
        COMMENT '필수 제출 여부',

    sort_order INT NOT NULL
        COMMENT '항목 표시 순서',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '생성 일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '수정 일시',

    CONSTRAINT PK_SUBMISSION_BOX_ITEM
        PRIMARY KEY (submission_box_item_id),

    CONSTRAINT FK_SUBMISSION_BOX_TO_SUBMISSION_BOX_ITEM
        FOREIGN KEY (submission_box_id)
            REFERENCES submission_box (submission_box_id)
            ON DELETE CASCADE,

    CONSTRAINT UQ_SUBMISSION_BOX_ITEM_ORDER
        UNIQUE (submission_box_id, sort_order),

    CONSTRAINT CK_SUBMISSION_BOX_ITEM_TYPE
        CHECK (item_type IN ('FILE', 'LINK')),

    CONSTRAINT CK_SUBMISSION_BOX_ITEM_FILE_TYPE
        CHECK (
            item_type = 'FILE'
                OR allowed_file_types IS NULL
            ),

    CONSTRAINT CK_SUBMISSION_BOX_ITEM_SORT_ORDER
        CHECK (sort_order > 0)
);


CREATE INDEX IDX_SUBMISSION_BOX_ITEM_BOX
    ON submission_box_item (submission_box_id);


-- 기존 submission_box의 allowed_file_types를
-- 기본 제출 항목 하나로 이전
INSERT INTO submission_box_item
(
    submission_box_id,
    item_name,
    item_type,
    allowed_file_types,
    is_required,
    sort_order
)
SELECT
    submission_box_id,
    '결과물',
    'FILE',
    allowed_file_types,
    TRUE,
    1
FROM submission_box;


-- 제출 항목 테이블로 이전했으므로 기존 컬럼 제거
ALTER TABLE submission_box
DROP COLUMN allowed_file_types;


-- =========================================================
-- 3. submission 수정
-- =========================================================

ALTER TABLE submission
DROP FOREIGN KEY FK_users_TO_submission_1,
    DROP FOREIGN KEY FK_team_TO_submission_1,
DROP INDEX UQ_SUBMISSION_BOX_USER;


ALTER TABLE submission
    MODIFY COLUMN submission_id BIGINT NOT NULL AUTO_INCREMENT
    COMMENT '제출물 아이디',

    CHANGE COLUMN user_id owner_user_id BIGINT NULL
    COMMENT '개인 제출 소유자 아이디',

    ADD COLUMN submitted_by BIGINT NULL
    COMMENT '실제 제출 처리 사용자 아이디'
    AFTER team_id,

    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '생성 일시',

    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
    COMMENT '수정 일시';


-- 실제 제출한 사용자는 기존 user_id 값으로 이전
UPDATE submission
SET submitted_by = owner_user_id
WHERE submitted_by IS NULL;


-- 팀 제출은 개인 소유자가 아니라 팀 소유이므로 개인 소유자 제거
UPDATE submission
SET owner_user_id = NULL
WHERE team_id IS NOT NULL;


-- submitted_by를 필수 값으로 변경
ALTER TABLE submission
    MODIFY COLUMN submitted_by BIGINT NOT NULL
    COMMENT '실제 제출 처리 사용자 아이디';


-- 새로운 외래 키 설정
ALTER TABLE submission
    ADD CONSTRAINT FK_SUBMISSION_BOX_TO_SUBMISSION
        FOREIGN KEY (submission_box_id)
            REFERENCES submission_box (submission_box_id),

    ADD CONSTRAINT FK_OWNER_USER_TO_SUBMISSION
        FOREIGN KEY (owner_user_id)
            REFERENCES users (user_id),

    ADD CONSTRAINT FK_TEAM_TO_SUBMISSION
    FOREIGN KEY (team_id)
        REFERENCES team (team_id),

    ADD CONSTRAINT FK_SUBMITTED_BY_TO_SUBMISSION
        FOREIGN KEY (submitted_by)
            REFERENCES users (user_id);


-- 개인 제출과 팀 제출 중 정확히 하나만 설정
ALTER TABLE submission
    ADD CONSTRAINT CK_SUBMISSION_OWNER
        CHECK (
            (owner_user_id IS NOT NULL AND team_id IS NULL)
                OR
            (owner_user_id IS NULL AND team_id IS NOT NULL)
            );


-- 개인별/팀별 제출 중복 방지
ALTER TABLE submission
    ADD CONSTRAINT UQ_SUBMISSION_BOX_OWNER_USER
        UNIQUE (submission_box_id, owner_user_id),

    ADD CONSTRAINT UQ_SUBMISSION_BOX_TEAM
        UNIQUE (submission_box_id, team_id);


CREATE INDEX IDX_SUBMISSION_BOX
    ON submission (submission_box_id);

CREATE INDEX IDX_SUBMISSION_SUBMITTED_BY
    ON submission (submitted_by);

CREATE INDEX IDX_SUBMISSION_SUBMITTED_AT
    ON submission (submitted_at);


-- =========================================================
-- 4. 제출 항목별 값 테이블 생성
--
-- FILE 항목:
-- file_key, original_file_name, content_type, file_size 사용
--
-- LINK 항목:
-- external_url 사용
-- =========================================================

CREATE TABLE submission_item_value
(
    submission_item_value_id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '제출 항목 값 아이디',

    submission_id BIGINT NOT NULL
        COMMENT '제출물 아이디',

    submission_box_item_id BIGINT NOT NULL
        COMMENT '제출 항목 아이디',

    file_key VARCHAR(500) NULL
        COMMENT '파일 저장 키 또는 파일 URL',

    original_file_name VARCHAR(255) NULL
        COMMENT '원본 파일명',

    content_type VARCHAR(100) NULL
        COMMENT '파일 MIME 타입',

    file_size BIGINT NULL
        COMMENT '파일 크기(byte)',

    external_url VARCHAR(1000) NULL
        COMMENT '외부 링크',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '생성 일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '수정 일시',

    CONSTRAINT PK_SUBMISSION_ITEM_VALUE
        PRIMARY KEY (submission_item_value_id),

    CONSTRAINT FK_SUBMISSION_TO_ITEM_VALUE
        FOREIGN KEY (submission_id)
            REFERENCES submission (submission_id)
            ON DELETE CASCADE,

    CONSTRAINT FK_SUBMISSION_BOX_ITEM_TO_ITEM_VALUE
        FOREIGN KEY (submission_box_item_id)
            REFERENCES submission_box_item (submission_box_item_id),

    CONSTRAINT UQ_SUBMISSION_ITEM_VALUE
        UNIQUE (submission_id, submission_box_item_id),

    CONSTRAINT CK_SUBMISSION_ITEM_VALUE_CONTENT
        CHECK (
            (file_key IS NOT NULL AND external_url IS NULL)
                OR
            (file_key IS NULL AND external_url IS NOT NULL)
            ),

    CONSTRAINT CK_SUBMISSION_ITEM_VALUE_FILE_SIZE
        CHECK (file_size IS NULL OR file_size >= 0)
);


CREATE INDEX IDX_SUBMISSION_ITEM_VALUE_SUBMISSION
    ON submission_item_value (submission_id);

CREATE INDEX IDX_SUBMISSION_ITEM_VALUE_ITEM
    ON submission_item_value (submission_box_item_id);


-- =========================================================
-- 5. 기존 submission.file_url 데이터 이전
-- =========================================================

INSERT INTO submission_item_value
(
    submission_id,
    submission_box_item_id,
    file_key,
    created_at,
    updated_at
)
SELECT
    s.submission_id,
    sbi.submission_box_item_id,
    s.file_url,
    COALESCE(s.submitted_at, CURRENT_TIMESTAMP),
    COALESCE(s.submitted_at, CURRENT_TIMESTAMP)
FROM submission s
         JOIN submission_box_item sbi
              ON sbi.submission_box_id = s.submission_box_id
                  AND sbi.sort_order = 1
WHERE s.file_url IS NOT NULL
  AND TRIM(s.file_url) <> '';


-- 기존 단일 파일 컬럼 제거
ALTER TABLE submission
DROP COLUMN file_url;