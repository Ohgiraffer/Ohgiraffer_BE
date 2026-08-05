-- =========================================================
-- 제출 항목 파일 형식 데이터 및 제약 조건 보정
--
-- FILE 항목:
-- allowed_file_types가 반드시 존재해야 한다.
--
-- LINK 항목:
-- allowed_file_types를 사용하지 않으므로 NULL이어야 한다.
-- =========================================================


-- ---------------------------------------------------------
-- 1. 기존 FILE 항목 데이터 보정
--
-- 기존 submission_box.allowed_file_types 값이 NULL 또는
-- 공백이었던 경우 FILE 항목이 잘못 생성되었을 수 있다.
--
-- '*'는 모든 파일 형식을 허용한다는 의미로 사용한다.
-- ---------------------------------------------------------

UPDATE submission_box_item
SET allowed_file_types = '*'
WHERE item_type = 'FILE'
  AND (
    allowed_file_types IS NULL
        OR TRIM(allowed_file_types) = ''
    );


-- ---------------------------------------------------------
-- 2. 기존 LINK 항목 데이터 보정
--
-- LINK 항목은 파일 형식을 사용하지 않으므로
-- allowed_file_types 값을 NULL로 통일한다.
-- ---------------------------------------------------------

UPDATE submission_box_item
SET allowed_file_types = NULL
WHERE item_type = 'LINK';


-- ---------------------------------------------------------
-- 3. 기존 CHECK 제약 제거
--
-- 기존 제약:
-- item_type = 'FILE' OR allowed_file_types IS NULL
--
-- 이 조건은 FILE 항목에 allowed_file_types가 없어도
-- 통과시키는 문제가 있다.
-- ---------------------------------------------------------

ALTER TABLE submission_box_item
DROP CHECK CK_SUBMISSION_BOX_ITEM_FILE_TYPE;


-- ---------------------------------------------------------
-- 4. 올바른 CHECK 제약 생성
--
-- FILE:
-- allowed_file_types가 NULL 또는 공백이면 안 된다.
--
-- LINK:
-- allowed_file_types가 반드시 NULL이어야 한다.
-- ---------------------------------------------------------

ALTER TABLE submission_box_item
    ADD CONSTRAINT CK_SUBMISSION_BOX_ITEM_FILE_TYPE
        CHECK (
            (
                item_type = 'FILE'
                    AND allowed_file_types IS NOT NULL
                    AND TRIM(allowed_file_types) <> ''
                )
                OR
            (
                item_type = 'LINK'
                    AND allowed_file_types IS NULL
                )
            );